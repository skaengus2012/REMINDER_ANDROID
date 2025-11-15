/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * @author Thalys
 */
class GetScheduleListResourcesStreamUseCase(
    private val tagRepository: TagRepository,
    private val linkMetadataRepository: LinkMetadataRepository
) {
    operator fun invoke(schedulesFlow: Flow<Set<Schedule>>): Flow<Set<ScheduleListResource>> = channelFlow {
        val resultFlow = MutableStateFlow<Set<ScheduleListResource>?>(null).also { flow ->
            flow.filterNotNull()
                .onEach { send(it) }
                .launchIn(scope = this)
        }
        val chunkFlow = schedulesFlow
            .map(transform = ::chunkOf)
            .stateIn(scope = this, started = SharingStarted.Lazily, initialValue = null)
        combine(
            chunkFlow.filterNotNull(),
            totalTagsResponseFlowOf(chunkFlow = chunkFlow),
            totalLinkToMetadataTableResponseFlowOf(chunkFlow = chunkFlow)
        ) { chunk, totalTagsResponse, totalLinkMetadataTableResponse ->
            fun transformToScheduleListResource(schedule: Schedule) = ScheduleListResource(
                id = schedule.id,
                title = schedule.content.title,
                note = schedule.content.note,
                link = schedule.content.link,
                linkMetadata = totalLinkMetadataTableResponse.data[schedule.content.link],
                timing = schedule.content.timing,
                isComplete = schedule.isComplete,
                tags = schedule.content.tagIds.let { tagIds ->
                    if (tagIds.isEmpty()) emptyList()
                    else totalTagsResponse.data.filter { it.id in tagIds }
                }
            )

            if (chunk.totalTagIds != totalTagsResponse.request) return@combine null
            if (chunk.totalLinks != totalLinkMetadataTableResponse.request) return@combine null

            chunk.schedules.toSet(transform = ::transformToScheduleListResource)
        }.filterNotNull()
            .onEach { resultFlow.value = it }
            .launchIn(scope = this)
    }

    private fun totalTagsResponseFlowOf(
        chunkFlow: StateFlow<Chunk?>
    ) = chunkFlow.mapNotNull { it?.totalTagIds }.distinctUntilChanged().flatMapLatest { totalTagIds ->
        tagRepository
            .getTagsAsStream(query = GetTagQuery.ByIds(totalTagIds))
            .map { tags -> Response(request = totalTagIds, data = tags) }
    }

    private fun totalLinkToMetadataTableResponseFlowOf(
        chunkFlow: StateFlow<Chunk?>
    ) = chunkFlow.mapNotNull { it?.totalLinks }.distinctUntilChanged().flatMapLatest { totalLinks ->
        linkMetadataRepository
            .getLinkToMetadataTableAsStream(totalLinks)
            .map { linkToMetadataTable -> Response(request = totalLinks, data = linkToMetadataTable) }
    }

    companion object {
        private fun chunkOf(schedules: Set<Schedule>): Chunk {
            val totalTags = mutableSetOf<TagId>()
            val totalLinks = mutableSetOf<Link>()
            schedules.forEach { schedule ->
                totalTags += schedule.content.tagIds
                schedule.content.link?.let(totalLinks::add)
            }
            return Chunk(
                schedules = schedules,
                totalTagIds = totalTags,
                totalLinks = totalLinks
            )
        }
    }

    private data class Chunk(
        val schedules: Set<Schedule>,
        val totalTagIds: Set<TagId>,
        val totalLinks: Set<Link>
    )

    private data class Response<T, U>(
        val request: T,
        val data: U
    )
}