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

package com.nlab.reminder.internal.data.repository

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.isNotEmpty
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.TimestampRepository
import com.nlab.reminder.core.kotlin.collection.filter
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.kotlin.util.onSuccess
import com.nlab.reminder.domain.common.kotlin.coroutine.inject.GlobalScope
import com.nlab.reminder.internal.common.android.database.LinkMetadataDao
import com.nlab.reminder.internal.data.model.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TODO 캐시층이 있어야 한다.
 * 로컬 최대갯수를 넘어갈 경우 버그가 있을 것 같다.
 * https://github.com/skaengus2012/REMINDER_ANDROID/issues/237
 * @author thalys
 */
internal class LocalLinkMetadataTableRepository @Inject constructor(
    private val linkMetadataDao: LinkMetadataDao,
    private val linkMetadataRepository: LinkMetadataRepository,
    private val timestampRepository: TimestampRepository,
    @GlobalScope private val coroutineScope: CoroutineScope,
) : LinkMetadataTableRepository {
    override suspend fun fetch(links: Set<Link>) {
        links.filter(Link::isNotEmpty).forEach { link ->
            coroutineScope.launch { cachingLink(link) }
        }
    }

    private suspend fun cachingLink(link: Link) {
        linkMetadataRepository.get(link).onSuccess { linkMetadata ->
            if (linkMetadata.isCacheable()) {
                linkMetadataDao.insertAndClearOldData(
                    linkMetadata.toEntity(link, timestampRepository.get())
                )
            }
        }
    }

    override fun get(): Flow<LinkMetadataTable> =
        linkMetadataDao.findAsStream().map { entities ->
            LinkMetadataTable(entities.associateBy(
                keySelector = { Link(it.link) },
                valueTransform = { LinkMetadata(it.title, it.imageUrl) }
            ))
        }
}

private fun LinkMetadata.isCacheable(): Boolean = title.isNotBlank() || imageUrl.isNotBlank()