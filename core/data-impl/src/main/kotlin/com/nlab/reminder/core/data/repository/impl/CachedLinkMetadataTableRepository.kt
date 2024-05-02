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
import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan

/**
 * @author thalys
 */
class CachedLinkMetadataTableRepository(
    private val internalRepository: LinkMetadataTableRepository,
    dispatcher: CoroutineDispatcher
) : LinkMetadataTableRepository {
    private val requestedLinks = mutableSetOf<Link>()
    private val caches = internalRepository.getStream()
        .scan(LinkMetadataTable(persistentHashMapOf()), ::mergeLinkMetadataTable)
        .distinctUntilChanged()
        .flowOn(dispatcher)

    private fun mergeLinkMetadataTable(
        acc: LinkMetadataTable,
        value: LinkMetadataTable
    ): LinkMetadataTable = LinkMetadataTable(acc.value.toPersistentHashMap().putAll(value.value))

    override suspend fun fetch(links: Set<Link>) {
        val notRequestedLinks = buildSet {
            links.forEach { link ->
                if (link !in requestedLinks) {
                    this += link
                    requestedLinks += link
                }
            }
        }
        internalRepository.fetch(notRequestedLinks)
    }

    override fun getStream(): Flow<LinkMetadataTable> = caches
}