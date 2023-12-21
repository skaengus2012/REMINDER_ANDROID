/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.schedule

import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.core.data.model.TransactionId
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.reminder.domain.common.data.model.genTags
import com.nlab.testkit.*

/**
 * @author Doohyun
 */
fun genSchedule(
    scheduleId: Long = genLong(),
    title: String = genBothify(),
    note: String = genBothify(),
    link: String? = genBothify(),
    tags: List<Tag> = genTags(),
    visiblePriority: Long = genLong(),
    isComplete: Boolean = genBoolean()
): Schedule = Schedule(scheduleId, title, note, link, tags, visiblePriority, isComplete)

fun genSchedules(
    isComplete: Boolean = genBoolean(),
    link: String? = null
): List<Schedule> = List(genInt(min = 10, max = 99)) { index ->
    genSchedule(scheduleId = index.toLong(), isComplete = isComplete, link = link)
}

fun genScheduleUiState(
    schedule: Schedule = genSchedule(),
    linkMetadata: LinkMetadata = LinkMetadata.Empty,
    isCompleteMarked: Boolean = genBoolean(),
    isSelected: Boolean = genBoolean()
): ScheduleUiState = ScheduleUiState(
    schedule,
    linkMetadata,
    isCompleteMarked,
    isSelected
)

fun genScheduleUiStates(
    schedules: List<Schedule> = genSchedules(),
    linkMetadata: LinkMetadata = LinkMetadata.Empty,
    isCompleteMarked: Boolean = genBoolean(),
    isSelected: Boolean = false
): List<ScheduleUiState> = schedules.map { schedule ->
    genScheduleUiState(schedule, linkMetadata, isCompleteMarked, isSelected)
}

fun genCompleteMark(
    isComplete: Boolean = genBoolean(),
    transactionId: TransactionId = TransactionId(genBothify())
): CompleteMark = CompleteMark(isComplete, transactionId)