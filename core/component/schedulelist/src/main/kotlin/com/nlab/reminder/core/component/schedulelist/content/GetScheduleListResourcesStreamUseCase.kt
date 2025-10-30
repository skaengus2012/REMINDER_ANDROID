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

import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.collections.toSetNotNull
import com.nlab.reminder.core.kotlin.collections.tryToNonEmptySetOrNull
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

/**
 * @author Thalys
 */
class GetScheduleListResourcesStreamUseCase(
    private val tagRepository: TagRepository,
    private val linkMetadataRepository: LinkMetadataRepository
) {
    operator fun invoke(schedulesStream: Flow<List<Schedule>>): Flow<List<ScheduleListResource>> = combine(
        schedulesStream,
        schedulesStream
            .map { schedules ->
                buildMap {
                    schedules.forEach { schedule ->
                        schedule.content.tagIds.tryToNonEmptySetOrNull()?.let { put(schedule.id, it) }
                    }
                }
            }
            .distinctUntilChanged()
            .flatMapLatest { scheduleIdToTagIds ->
                tagRepository
                    .getTagsAsStream(
                        query = GetTagQuery.ByIds(
                            tagIds = buildSet {
                                scheduleIdToTagIds.forEach { (_, v) -> addAll(v.value) }
                            }
                        )
                    )
                    .map { tags ->
                        val tagIdToTagTable = tags.sortedBy { it.name.value }.associateBy { it.id }
                        scheduleIdToTagIds.mapValues { (_, scheduleTagIds) ->
                            tagIdToTagTable.mapNotNull { (tagId, tag) ->
                                if (tagId in scheduleTagIds.value) tag
                                else null
                            }
                        }
                    }
            },
        schedulesStream
            .map { schedules -> schedules.toSetNotNull { it.content.link } }
            .distinctUntilChanged()
            .flatMapLatest { links -> linkMetadataRepository.getLinkToMetadataTableAsStream(links) }
    ) { schedules, scheduleIdToTagsTable, linkToMetadataTable ->
        fun transformToResource(schedule: Schedule): ScheduleListResource = ScheduleListResource(
            id = schedule.id,
            title = schedule.content.title,
            note = schedule.content.note,
            link = schedule.content.link,
            linkMetadata = schedule.content.link?.let { linkToMetadataTable[it] },
            timing = schedule.content.timing,
            isComplete = schedule.isComplete,
            tags = scheduleIdToTagsTable[schedule.id] ?: emptyList()
        )
        schedules.map(::transformToResource)
    }
}