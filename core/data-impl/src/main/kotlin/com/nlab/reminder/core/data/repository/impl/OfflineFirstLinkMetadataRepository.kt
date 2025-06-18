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
import com.nlab.reminder.core.data.model.toSaveInput
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.concurrent.atomics.updateAndGet
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlinx.coroutines.flow.channelFlow
import com.nlab.reminder.core.kotlinx.coroutines.flow.combine
import com.nlab.reminder.core.kotlinx.coroutines.flow.filter
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference

/**
 * @author Doohyun
 */
class OfflineFirstLinkMetadataRepository(
    private val linkMetadataDAO: LinkMetadataDAO,
    private val remoteDataSource: LinkThumbnailDataSource,
    private val remoteCache: LinkMetadataRemoteCache
) : LinkMetadataRepository {
    override fun getLinkToMetadataTableAsStream(links: Set<Link>): Flow<Map<Link, LinkMetadata>> = flow {
        val currentSnapshot = remoteCache.snapshot().filterKeys { it in links }
        emit(currentSnapshot)

        val missingLinks = links - currentSnapshot.keys
        if (missingLinks.isNotEmpty()) {
            combine(
                findOnLocal(missingLinks),
                findOnRemote(missingLinks).onStart { emit(emptyMap()) }
            ) { local, remote -> local + remote }
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .collect { result -> emit(currentSnapshot + result) }
        }
    }

    private fun findOnLocal(links: Set<Link>): Flow<Map<Link, LinkMetadata>> = flow {
        val rawLinkValueToLink = links.associateBy(keySelector = { it.rawLink.value })
        val table = linkMetadataDAO
            .findByLinks(links = links.toSet { it.rawLink })
            .associateBy(keySelector = { rawLinkValueToLink.getValue(it.link) }, valueTransform = ::LinkMetadata)
        emit(table)
    }

    private fun findOnRemote(links: Set<Link>): Flow<Map<Link, LinkMetadata>> = channelFlow {
        val acc = AtomicReference(persistentHashMapOf<Link, LinkMetadata>())
        links.forEach { link ->
            launch {
                remoteDataSource.getLinkThumbnail(link.rawLink)
                    .map(::LinkMetadata)
                    .onSuccess { remoteCache.put(link, it) }
                    .onSuccess { metadata -> send(acc.updateAndGet { it.put(link, metadata) }) }
                    .onSuccess { metadata -> linkMetadataDAO.insertAndGet(metadata.toSaveInput(link)) }
            }
        }
    }
}