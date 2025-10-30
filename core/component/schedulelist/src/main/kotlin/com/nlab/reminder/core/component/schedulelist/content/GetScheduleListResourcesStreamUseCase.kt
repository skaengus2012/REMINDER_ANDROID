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
import com.nlab.reminder.core.kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * @author Thalys
 */
class GetScheduleListResourcesStreamUseCase(
    private val tagRepository: TagRepository,
    private val linkMetadataRepository: LinkMetadataRepository
) {
    operator fun invoke(
        schedulesStream: Flow<List<Schedule>>
    ): Flow<List<ScheduleListResource>> = schedulesStream.flatMapLatest { schedules ->
        val tagIds = mutableSetOf<TagId>()
        val links = mutableSetOf<Link>()
        schedules.forEach { schedule ->
            val content = schedule.content
            content.link?.let(links::add)
            content.tagIds.let(tagIds::addAll)
        }
        combine(
            tagRepository.getTagsAsStream(GetTagQuery.ByIds(tagIds))
                .map { tags -> tags.associateBy { it.id } },
            linkMetadataRepository.getLinkToMetadataTableAsStream(links)
        ) { tagIdToTagTable, linkToMetadataTable ->
            fun transformToResource(schedule: Schedule): ScheduleListResource = ScheduleListResource(
                id = schedule.id,
                title = schedule.content.title,
                note = schedule.content.note,
                link = schedule.content.link,
                linkMetadata = schedule.content.link?.let { linkToMetadataTable[it] },
                timing = schedule.content.timing,
                isComplete = schedule.isComplete,
                tags = schedule.content.tagIds.asSequence()
                    .mapNotNull { tagIdToTagTable[it] }
                    .sortedBy { it.name.value }
                    .toList()
            )
            schedules.map(::transformToResource)
        }
    }
}