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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.foundation.time.TimestampProvider
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.local.database.LinkMetadataDao
import com.nlab.reminder.core.local.database.LinkMetadataEntity
import com.nlab.reminder.core.network.LinkThumbnailDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
class OfflineFirstLinkMetadataRepository(
    private val linkMetadataDao: LinkMetadataDao,
    private val linkThumbnailDataSource: LinkThumbnailDataSource,
    private val timestampProvider: TimestampProvider,
    initialCache: Map<Link.Present, LinkMetadata>
) : LinkMetadataRepository {
    private val inMemoryTableFlow = MutableStateFlow(initialCache)

    override suspend fun getAsStream(links: Set<Link.Present>): Flow<Map<Link.Present, LinkMetadata>> =
        suspend { sync(links) }
            .asFlow()
            .flatMapLatest { inMemoryTableFlow }

    private suspend fun sync(links: Set<Link.Present>) {
        val curCacheTable = inMemoryTableFlow.value
        val notCachedRawValueToLinks = links
            .asSequence()
            .filter { it !in curCacheTable }
            .map { it.value to it }
            .toMap()
        if (notCachedRawValueToLinks.isEmpty()) return

        val scope = CoroutineScope(currentCoroutineContext())
        syncFromRemote(
            scope,
            target = notCachedRawValueToLinks,
            localSyncJob = scope.launch { syncFromLocal(notCachedRawValueToLinks) },
        )
    }

    private suspend fun syncFromLocal(target: Map<String, Link.Present>) {
        val newCache = linkMetadataDao
            .findByLinks(target.keys.toList())
            .map { entity ->
                val link = target.getValue(entity.link)
                val metadata = LinkMetadata(title = entity.title, imageUrl = entity.imageUrl)
                link to metadata
            }
        inMemoryTableFlow.update { old -> old + newCache }
    }

    private fun syncFromRemote(
        scope: CoroutineScope,
        target: Map<String, Link.Present>,
        localSyncJob: Job
    ) {
        target.map { (rawLink, link) ->
            scope.launch {
                linkThumbnailDataSource.getLinkThumbnailResource(rawLink).onSuccess { resource ->
                    val newTable = link to LinkMetadata(
                        title = resource.title,
                        imageUrl = resource.image
                    )
                    localSyncJob.join()
                    inMemoryTableFlow.update { old -> old + newTable }
                    linkMetadataDao.insert(
                        LinkMetadataEntity(
                            link = link.value,
                            title = resource.title,
                            imageUrl = resource.image,
                            timestamp = timestampProvider.now()
                        )
                    )
                }
            }
        }
    }
}
