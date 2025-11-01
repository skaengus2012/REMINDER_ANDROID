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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * @author Thalys
 */
class GetScheduleListResourcesStreamUseCase(
    private val tagRepository: TagRepository,
    private val linkMetadataRepository: LinkMetadataRepository
) {
    operator fun invoke(schedulesStream: Flow<List<Schedule>>): Flow<List<ScheduleListResource>> = channelFlow {
        val chunkFlow = schedulesStream
            .map { schedules ->
                val totalTags = mutableSetOf<TagId>()
                val totalLinks = mutableSetOf<Link>()
                schedules.forEach { schedule ->
                    totalTags += schedule.content.tagIds
                    schedule.content.link?.let(totalLinks::add)
                }
                Chunk(
                    schedules = schedules,
                    totalTagIds = totalTags,
                    totalLinks = totalLinks
                )
            }
            .stateIn(scope = this, started = SharingStarted.Eagerly, initialValue = null)
        val totalTagsFlow = chunkFlow
            .filterNotNull()
            .map { it.totalTagIds }
            .distinctUntilChanged()
            .flatMapLatest { tagIds ->
                tagRepository
                    .getTagsAsStream(query = GetTagQuery.ByIds(tagIds))
                    .map { tags -> tags.sortedBy { it.name.value } }
            }
        val totalLinkToMetadataTableFlow = chunkFlow
            .filterNotNull()
            .map { it.totalLinks }
            .distinctUntilChanged()
            .flatMapLatest(linkMetadataRepository::getLinkToMetadataTableAsStream)
        combine(
            chunkFlow.filterNotNull().map { it.schedules },
            totalTagsFlow,
            totalLinkToMetadataTableFlow
        ) { schedules, totalTags, totalLinkMetadataTable ->
            fun transformToScheduleListResource(schedule: Schedule) = ScheduleListResource(
                id = schedule.id,
                title = schedule.content.title,
                note = schedule.content.note,
                link = schedule.content.link,
                linkMetadata = totalLinkMetadataTable[schedule.content.link],
                timing = schedule.content.timing,
                isComplete = schedule.isComplete,
                tags = schedule.content.tagIds.let { tagIds ->
                    if (tagIds.isEmpty()) emptyList()
                    else totalTags.filter { it.id in tagIds }
                }
            )
            schedules.map(::transformToScheduleListResource)
        }.onEach { send(it) }.launchIn(this)
    }

    private data class Chunk(
        val schedules: List<Schedule>,
        val totalTagIds: Set<TagId>,
        val totalLinks: Set<Link>
    )
}