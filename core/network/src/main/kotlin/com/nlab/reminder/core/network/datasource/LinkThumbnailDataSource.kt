/*
 * Copyright (C) 2025 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.core.network.datasource

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.parser.StreamParser
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author Doohyun
 */
private const val OG_TITLE = "og:title"
private const val OG_IMAGE = "og:image"
// Same user agent Jsoup.connect used, so sites keep serving OG tags as before.
private const val BROWSER_USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
private val TAGS_REQUIRED: Set<String> = setOf(OG_TITLE, OG_IMAGE)

private fun Element.toProperty(): String = attr("property")
private fun Element.toContent(): String = attr("content")

data class LinkThumbnailResponse(val title: String?, val image: String?)

fun interface LinkThumbnailDataSource {
    suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse>
}

internal class LinkThumbnailDataSourceImpl(
    private val dispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient
) : LinkThumbnailDataSource {
    override suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse> = catching {
        withContext(dispatcher) {
            val request = Request.Builder()
                .url(url.value)
                .header("User-Agent", BROWSER_USER_AGENT)
                .build()
            val metaTagToValues = okHttpClient.newCall(request).await().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected response code: ${response.code}")
                val body = response.body ?: throw IOException("Response body is empty")
                parseHead(body, baseUri = url.value)
                    ?.let(::parseMetaTagToValues)
                    ?: emptyMap()
            }
            LinkThumbnailResponse(
                title = metaTagToValues[OG_TITLE],
                image = metaTagToValues[OG_IMAGE]
            )
        }
    }

    private fun parseHead(body: ResponseBody, baseUri: String): Element? =
        if (body.contentType()?.charset() != null) {
            // Charset is declared by the server, so stream-parse and stop early.
            StreamParser(Parser.htmlParser()).use { parser ->
                parser.parse(body.charStream(), baseUri)
                parser.selectFirst("head")
            }
        } else {
            // No declared charset: let Jsoup sniff the BOM / <meta charset>, as Jsoup.connect did.
            Jsoup.parse(body.byteStream(), null, baseUri).head()
        }

    private fun parseMetaTagToValues(headElement: Element): Map<String, String> = buildMap {
        headElement.select("meta[property^=og:]").forEach { element ->
            if (element.toProperty() in TAGS_REQUIRED) {
                put(element.toProperty(), element.toContent())
            }
        }
    }
}

/**
 * Suspends until the response arrives and cancels the underlying [Call] when the coroutine is cancelled.
 */
private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response) else response.close()
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }
    })
}
