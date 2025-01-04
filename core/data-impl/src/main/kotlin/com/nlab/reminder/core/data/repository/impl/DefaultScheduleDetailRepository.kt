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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleDetail
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.ScheduleDetailRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleTagListRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.collections.toSetNotNull
import com.nlab.reminder.core.kotlinx.coroutine.flow.combine
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import kotlinx.coroutines.flow.Flow

/**
 * @author Thalys
 */
class DefaultScheduleDetailRepository(
    private val scheduleRepository: ScheduleRepository,
    private val tagRepository: TagRepository,
    private val scheduleTagListRepository: ScheduleTagListRepository,
    private val linkMetadataRepository: LinkMetadataRepository
) : ScheduleDetailRepository {
    override suspend fun getScheduleDetailsAsStream(
        query: GetScheduleQuery
    ): Flow<Set<ScheduleDetail>> = scheduleRepository
        .getSchedulesAsStream(query)
        .flatMapLatest(::createScheduleDetailsFlow)

    private fun createScheduleDetailsFlow(schedules: Set<Schedule>): Flow<Set<ScheduleDetail>> {
        val scheduleIds = mutableSetOf<ScheduleId>()
        val links = mutableSetOf<Link>()
        schedules.forEach { schedule ->
            schedule.id.let(scheduleIds::add)
            schedule.content.link?.let(links::add)
        }
        return combine(
            createScheduleIdsToTagListTableFlow(scheduleIds = scheduleIds),
            linkMetadataRepository.getAsStream(links = links),
        ) { scheduleIdToTagsTable, linkToMetadataTable ->
            createScheduleDetails(
                schedules = schedules,
                scheduleIdToTagsTable = scheduleIdToTagsTable,
                linkToMetadataTable = linkToMetadataTable
            )
        }
    }

    private fun createScheduleIdsToTagListTableFlow(
        scheduleIds: Set<ScheduleId>,
    ): Flow<Map<ScheduleId, Set<Tag>>> = scheduleTagListRepository
        .getScheduleTagListAsStream(scheduleIds)
        .flatMapLatest { scheduleIdToTagIds ->
            tagRepository
                .getTagsAsStream(GetTagQuery.ByIds(tagIds = scheduleIdToTagIds.values.flatten().toSet()))
                .map { tags ->
                    val idToTable = tags.associateBy { it.id }
                    scheduleIdToTagIds.mapValues { entry -> entry.value.toSetNotNull { idToTable[it] } }
                }
        }

    private fun createScheduleDetails(
        schedules: Set<Schedule>,
        scheduleIdToTagsTable: Map<ScheduleId, Set<Tag>>,
        linkToMetadataTable: Map<Link, LinkMetadata>
    ): Set<ScheduleDetail> = schedules.toSet { schedule ->
        ScheduleDetail(
            schedule = schedule,
            tags = scheduleIdToTagsTable[schedule.id] ?: emptySet(),
            linkMetadata = linkToMetadataTable[schedule.content.link]
        )
    }
}