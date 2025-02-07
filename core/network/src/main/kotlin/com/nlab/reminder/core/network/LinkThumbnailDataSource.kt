/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.network

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * @author Doohyun
 */
private const val OG_TITLE = "og:title"
private const val OG_IMAGE = "og:image"
private val TAGS_REQUIRED: Set<String> = setOf(OG_TITLE, OG_IMAGE)

private fun Element.toProperty(): String = attr("property")
private fun Element.toContent(): String = attr("content")

interface LinkThumbnailDataSource {
    suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse>
}

data class LinkThumbnailResponse(val title: String?, val image: String?)

class LinkThumbnailDataSourceImpl(private val dispatcher: CoroutineDispatcher) : LinkThumbnailDataSource {
    override suspend fun getLinkThumbnail(url: NonBlankString): Result<LinkThumbnailResponse> = catching {
        withContext(dispatcher) {
            val metaTagToValues = Jsoup.connect(url.value).execute()
                .streamParser()
                .use { parser ->
                    parser.selectFirst("head")
                        ?.let(::parseMetaTagToValues)
                        ?: emptyMap()
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