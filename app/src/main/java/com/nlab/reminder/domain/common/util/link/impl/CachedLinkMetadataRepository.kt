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

package com.nlab.reminder.domain.common.util.link.impl

import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.onSuccess

/**
 * @author thalys
 */
class CachedLinkMetadataRepository(
    private val internalRepository: LinkMetadataRepository
) : LinkMetadataRepository {
    private val caches= hashMapOf<String, LinkMetadata>()

    override suspend fun get(link: String): Result<LinkMetadata> {
        val result: Result<LinkMetadata>
        val curCache: LinkMetadata? = caches[link]
        if (curCache == null) {
            result = internalRepository.get(link).also { ret ->
                ret.onSuccess { linkThumbnail -> caches[link] = linkThumbnail }
            }
        } else {
            result = Result.Success(curCache)
        }
        return result
    }
}