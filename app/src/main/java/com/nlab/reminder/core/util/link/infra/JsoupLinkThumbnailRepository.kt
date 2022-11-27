/*
 * Copyright (C) 2022 The N's lab Open Source Project
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
package com.nlab.reminder.core.util.link.infra

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.util.link.LinkThumbnail
import com.nlab.reminder.core.util.link.LinkThumbnailRepository
import com.nlab.reminder.core.util.test.annotation.Generated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * @author thalys
 */
@Generated
class JsoupLinkThumbnailRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LinkThumbnailRepository {
    override suspend fun get(link: String): Result<LinkThumbnail> = withContext(dispatcher) {
        catching {
            LinkThumbnail(
                value = Jsoup.connect(link)
                    .get()
                    .select("meta[property^=og:]")
                    ?.find { element -> element.attr("property") == "og:image" }
                    ?.attr("content")
                    ?: ""
            )
        }
    }
}