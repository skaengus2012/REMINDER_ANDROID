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

package com.nlab.reminder.core.data.repository.infra

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * @author thalys
 */
class JsoupLinkMetadataRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LinkMetadataRepository {
    override suspend fun get(link: Link): Result<LinkMetadata> = withContext(dispatcher) {
        catching {
            val tagNameToValues: Map<String, String> = buildMap {
                Jsoup.connect(link.value)
                    .get()
                    .select("meta[property^=og:]")
                    .asSequence()
                    .filter { element -> element.toProperty() in TAGS_REQUIRED }
                    .forEach { element -> put(element.toProperty(), element.toContent()) }
            }

            LinkMetadata(
                title = tagNameToValues[OG_TITLE] ?: "",
                imageUrl = tagNameToValues[OG_IMAGE] ?: ""
            )
        }
    }
}

// TODO 임시적 허용.
internal const val OG_TITLE = "og:title"
internal const val OG_IMAGE = "og:image"
internal val TAGS_REQUIRED: Set<String> = setOf(OG_TITLE, OG_IMAGE)

internal fun Element.toProperty(): String = attr("property")
internal fun Element.toContent(): String = attr("content")