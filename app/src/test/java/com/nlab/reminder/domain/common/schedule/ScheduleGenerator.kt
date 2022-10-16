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

import androidx.paging.PagingData
import androidx.paging.map
import com.nlab.reminder.core.util.transaction.TransactionId
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genInt
import com.nlab.reminder.test.genLong

/**
 * @author Doohyun
 */
fun genSchedule(
    scheduleId: Long = genLong(),
    title: String = genBothify(),
    note: String = genBothify(),
    url: String = genBothify(),
    tags: List<Tag> = genTags(),
    visiblePriority: Int = genInt(),
    isComplete: Boolean = genBoolean()
): Schedule = Schedule(scheduleId, title, note, url, tags, visiblePriority, isComplete)

fun genSchedules(
    isComplete: Boolean = genBoolean()
): List<Schedule> = List(genInt("1#")) { index ->
    genSchedule(scheduleId = index.toLong(), isComplete = isComplete)
}

fun genScheduleUiState(
    schedule: Schedule = genSchedule(),
    isCompleteMarked: Boolean? = null
): ScheduleUiState = ScheduleUiState(schedule, isCompleteMarked = isCompleteMarked ?: schedule.isComplete)

fun genScheduleUiStates(
    schedules: List<Schedule> = genSchedules(),
    isCompleteMarked: Boolean? = null
): List<ScheduleUiState> = schedules.map { schedule ->
    ScheduleUiState(schedule, isCompleteMarked = isCompleteMarked ?: schedule.isComplete)
}

fun genPagingScheduleUiStates(
    schedules: PagingData<Schedule>,
    isCompleteMarked: Boolean? = null
): PagingData<ScheduleUiState> = schedules.map { schedule ->
    ScheduleUiState(schedule, isCompleteMarked = isCompleteMarked ?: schedule.isComplete)
}

fun genCompleteMark(
    isComplete: Boolean = genBoolean(),
    isApplied: Boolean = genBoolean(),
    transactionId: TransactionId = TransactionId(genBothify())
): CompleteMark = CompleteMark(isComplete, isApplied, transactionId)