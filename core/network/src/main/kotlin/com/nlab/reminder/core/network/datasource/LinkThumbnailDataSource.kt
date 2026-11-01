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
import com.nlab.reminder.core.network.NetworkConstants
import com.nlab.reminder.core.network.infra.RetrofitFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.parser.StreamParser
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset

/**
 * @author Doohyun
 */
private const val OG_TITLE = "og:title"
private const val OG_IMAGE = "og:image"
private val TAGS_REQUIRED: Set<String> = setOf(OG_TITLE, OG_IMAGE)

private fun Element.toProperty(): String = attr("property")
private fun Element.toContent(): String = attr("content")

data class LinkThumbnailResponse(val title: String?, val image: String?)

fun interface LinkThumbnailDataSource {
    suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse>
}

internal class LinkThumbnailDataSourceImpl(
    private val dispatcher: CoroutineDispatcher,
    retrofitFactory: RetrofitFactory
) : LinkThumbnailDataSource,
    LinkThumbnailApiService by retrofitFactory
        .create(NetworkConstants.UNUSED_BASE_URL)
        .create(LinkThumbnailApiService::class.java) {

    override suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse> = catching {
        withContext(dispatcher) {
            // The body is not read yet at this point. StreamParser reads it lazily and stops after <head>.
            val metaTagToValues = getHtml(url.value).use { body ->
                StreamParser(Parser.htmlParser()).use { parser ->
                    parser.parse(body.openReader(), url.value)
                    parser.selectFirst("head")
                        ?.let(::parseMetaTagToValues)
                        ?: emptyMap()
                }
            }
            LinkThumbnailResponse(
                title = metaTagToValues[OG_TITLE],
                image = metaTagToValues[OG_IMAGE]
            )
        }
    }

    private fun parseMetaTagToValues(headElement: Element): Map<String, String> = buildMap {
        headElement.select("meta[property^=og:]").forEach { element ->
            if (element.toProperty() in TAGS_REQUIRED) {
                put(element.toProperty(), element.toContent())
            }
        }
    }
}

private interface LinkThumbnailApiService {
    /**
     * Fetches the page at the absolute [url]. [Streaming] is required: without it Retrofit buffers the whole body in
     * memory, which defeats the stream parsing of the head element.
     */
    @Streaming
    @GET
    suspend fun getHtml(@Url url: String): ResponseBody
}

private const val CHARSET_SNIFF_BYTES = 1024L
private val META_CHARSET_REGEX = Regex("""<meta[^>]+charset\s*=\s*["']?\s*([A-Za-z0-9_.:\-]+)""", RegexOption.IGNORE_CASE)

/**
 * Opens a reader for the body without consuming it.
 * Uses the charset declared by the server, otherwise the one in `<meta>` found in the first bytes, otherwise UTF-8.
 */
private fun ResponseBody.openReader(): Reader {
    if (contentType()?.charset() != null) return charStream()

    val source = source()
    source.request(CHARSET_SNIFF_BYTES)
    val head = source.buffer.snapshot(minOf(source.buffer.size, CHARSET_SNIFF_BYTES).toInt()).string(Charsets.ISO_8859_1)
    val sniffed: Charset? = META_CHARSET_REGEX.find(head)
        ?.groupValues?.get(1)
        ?.let { name -> runCatching { Charset.forName(name) }.getOrNull() }
    return if (sniffed != null) InputStreamReader(source.inputStream(), sniffed) else charStream()
}
