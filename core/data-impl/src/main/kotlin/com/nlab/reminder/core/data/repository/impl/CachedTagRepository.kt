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

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.TagGetQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapConcat
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
class CachedTagRepository(
    private val internalRepository: TagRepository,
    private val coroutineScope: CoroutineScope
) : TagRepository by internalRepository {
    private val tagCacheFlow = MutableStateFlow(TagCache(cacheTable = emptyMap(), isAllDataSet = false))

    override suspend fun save(tag: Tag): Result<Tag> {
        val tagSaveResultJob = coroutineScope.async {
            internalRepository.save(tag).onSuccess { tag ->
                val pair = tag.id to tag
                tagCacheFlow.update { old -> old.copy(cacheTable = old.cacheTable + pair) }
            }
        }
        return tagSaveResultJob.await()
    }

    override suspend fun delete(id: TagId): Result<Unit> {
        val tagDeleteResultJob = coroutineScope.async {
            internalRepository.delete(id).onSuccess {
                tagCacheFlow.update { old -> old.copy(cacheTable = old.cacheTable - id) }
            }
        }
        return tagDeleteResultJob.await()
    }

    override suspend fun getTags(query: TagGetQuery): Result<List<Tag>> = catching {
        refreshCacheIfNeeded(query)
        return@catching tagCacheFlow.value.getResult(query)
    }

    override fun getTagsAsStream(query: TagGetQuery): Flow<List<Tag>> =
        suspend { refreshCacheIfNeeded(query) }
            .asFlow()
            .flatMapConcat { tagCacheFlow }
            .map { it.getResult(query) }

    private suspend fun refreshCacheIfNeeded(query: TagGetQuery) {
        val curCache = tagCacheFlow.value
        if (curCache.isAllDataSet) return
        if (query is TagGetQuery.ByIds && query.tagIds.all { it in curCache.cacheTable.keys }) return

        val tags = internalRepository.getTags(query).getOrThrow()
        val isAllDataSet = query is TagGetQuery.All
        tagCacheFlow.update { cacheData ->
            cacheData.copy(
                cacheTable = cacheData.cacheTable + tags.associateBy { it.id },
                isAllDataSet = isAllDataSet
            )
        }
    }
}

private data class TagCache(
    val cacheTable: Map<TagId, Tag>,
    val isAllDataSet: Boolean
)

private fun TagCache.getResult(query: TagGetQuery): List<Tag> {
    val tags = cacheTable.values
    return when (query) {
        is TagGetQuery.All -> tags.toList()
        is TagGetQuery.ByIds -> tags.filter { it.id in query.tagIds }
    }
}