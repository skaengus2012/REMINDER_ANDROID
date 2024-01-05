/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.model

import com.nlab.testkit.genBoolean
import com.nlab.testkit.genBothify
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * @author thalys
 */
fun genScheduleId(value: Long = genLong(min = 1)): ScheduleId = ScheduleId(value)

fun genSchedule(
    scheduleId: ScheduleId = genScheduleId(),
    title: String = genBothify(),
    note: String = genBothify(),
    link: Link = genLink(),
    tags: ImmutableList<Tag> = genTags().toImmutableList(),
    visiblePriority: Long = genLong(min = 1),
    isComplete: Boolean = genBoolean()
): Schedule = Schedule(
    scheduleId, title, note, link, tags, visiblePriority, isComplete
)

fun genSchedules(count: Int = genInt(min = 5, max = 10)): List<Schedule> = List(count) {
    genSchedule(scheduleId = genScheduleId(value = it.toLong() + 1))
}