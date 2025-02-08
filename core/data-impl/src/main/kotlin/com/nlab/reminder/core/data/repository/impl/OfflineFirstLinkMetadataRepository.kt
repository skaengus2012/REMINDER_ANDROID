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
import com.nlab.reminder.core.data.model.toLocalDTO
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.getOrNull
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.LinkThumbnailDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
class OfflineFirstLinkMetadataRepository(
    private val linkMetadataDAO: LinkMetadataDAO,
    private val linkThumbnailDataSource: LinkThumbnailDataSource,
    private val inMemoryCache: InMemoryLinkMetadataCache
) : LinkMetadataRepository {

    override fun getAsStream(links: Set<Link>): Flow<Map<Link, LinkMetadata>> =
        flow { emit(sync(links)) }
            .flatMapLatest { inMemoryCache.tableFlow }
            .distinctUntilChanged()
            .map { table ->
                buildMap {
                    links.forEach { link -> table[link]?.let { put(link, it) } }
                }
            }

    private suspend fun sync(links: Set<Link>) {
        val curCacheTable = inMemoryCache.tableFlow.value
        val notCachedLinks = links
            .filter { it !in curCacheTable }
            .toSet()
        if (notCachedLinks.isEmpty()) return

        val scope = CoroutineScope(currentCoroutineContext())
        syncFromRemote(
            scope,
            links = notCachedLinks,
            localSyncJob = scope.launch { syncFromLocal(notCachedLinks) },
        )
    }

    private suspend fun syncFromLocal(links: Set<Link>) {
        val rawLinkValueToLink = links.associateBy(keySelector = { it.rawLink.value })
        inMemoryCache.putAll(
            newElements = linkMetadataDAO
                .findByLinks(links = links.toSet { it.rawLink })
                .associateBy(keySelector = { rawLinkValueToLink.getValue(it.link) }, valueTransform = ::LinkMetadata)
        )
    }

    private fun syncFromRemote(scope: CoroutineScope, links: Set<Link>, localSyncJob: Job) {
        links.forEach { link ->
            scope.launch {
                val metadata = linkThumbnailDataSource.getLinkThumbnail(link.rawLink)
                    .map(::LinkMetadata)
                    .getOrNull()
                if (metadata != null) {
                    localSyncJob.join()  // await for local cache sync
                    val newCache = inMemoryCache.put(link, metadata)
                    if (newCache[link] == metadata) {
                        // if cache flush success, insert to local db
                        linkMetadataDAO.insertAndGet(metadata.toLocalDTO(link))
                    }
                }
            }
        }
    }
}

class InMemoryLinkMetadataCache(initialCache: Map<Link, LinkMetadata>) {
    private val _tableFlow = MutableStateFlow(initialCache)
    val tableFlow: StateFlow<Map<Link, LinkMetadata>> = _tableFlow.asStateFlow()

    fun put(link: Link, linkMetadata: LinkMetadata): Map<Link, LinkMetadata> {
        val newElements = link to linkMetadata
        return _tableFlow.updateAndGet { it + newElements }
    }

    fun putAll(newElements: Map<Link, LinkMetadata>): Map<Link, LinkMetadata> {
        return _tableFlow.updateAndGet { it + newElements }
    }
}
