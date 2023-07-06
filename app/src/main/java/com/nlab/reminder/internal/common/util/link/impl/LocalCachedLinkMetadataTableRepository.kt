/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.util.link.impl

import com.nlab.reminder.core.kotlin.collection.filter
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.kotlin.util.onSuccess
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.domain.common.util.link.LinkMetadataTable
import com.nlab.reminder.domain.common.util.link.LinkMetadataTableRepository
import com.nlab.reminder.internal.common.android.database.LinkMetadataDao
import com.nlab.reminder.internal.common.android.database.toEntity
import com.nlab.reminder.internal.common.android.database.toLinkMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * @author thalys
 */
@Deprecated("삭제 예정")
class LocalCachedLinkMetadataTableRepository(
    private val linkMetadataRepository: LinkMetadataRepository,
    private val linkMetadataDao: LinkMetadataDao,
    private val coroutineScope: CoroutineScope,
    private val getTimestamp: () -> Long
) : LinkMetadataTableRepository {
    override fun getStream(): Flow<LinkMetadataTable> =
        linkMetadataDao.findAsStream().map { entities ->
            entities.associateBy(
                keySelector = { it.link },
                valueTransform = { it.toLinkMetadata() }
            )
        }

    override suspend fun setLinks(links: List<String>) =
        links.filter(String::isNotBlank).distinct().forEach { link ->
            coroutineScope.launch {
                linkMetadataRepository.get(link).onSuccess { linkMetadata ->
                    if (linkMetadata.isTitleVisible
                        || linkMetadata.isImageVisible) {
                        linkMetadataDao.insertAndClearOldData(linkMetadata.toEntity(link, getTimestamp()))
                    }
                }
            }
        }
}