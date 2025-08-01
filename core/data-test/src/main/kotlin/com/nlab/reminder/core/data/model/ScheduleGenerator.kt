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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.genLong
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @author thalys
 */
fun genScheduleId(): ScheduleId = ScheduleId(rawId = genLong())

fun genScheduleContent(
    title: NonBlankString = genNonBlankString(),
    note: NonBlankString? = genNonBlankString(),
    link: Link? = genLink(),
    tagIds: Set<TagId> = genTagIds(),
    timing: ScheduleTiming? = genScheduleTiming(),
): ScheduleContent = ScheduleContent(
    title = title,
    note = note,
    link = link,
    tagIds = tagIds,
    timing = timing
)

fun genScheduleTiming(
    triggerAt: Instant = Clock.System.now(),
    isTriggerAtDateOnly: Boolean = genBoolean(),
    repeat: Repeat? = genRepeat()
): ScheduleTiming = ScheduleTiming(
    triggerAt = triggerAt,
    isTriggerAtDateOnly = isTriggerAtDateOnly,
    repeat = repeat
)

fun genSchedule(
    id: ScheduleId = genScheduleId(),
    content: ScheduleContent = genScheduleContent(),
    visiblePriority: NonNegativeLong = genNonNegativeLong(),
    isComplete: Boolean = genBoolean()
): Schedule = Schedule(
    id = id,
    content = content,
    visiblePriority = visiblePriority,
    isComplete = isComplete
)

fun genSchedules(count: Int = genInt(min = 5, max = 10)): List<Schedule> = List(count) {
    genSchedule(id = ScheduleId(rawId = it.toLong() + 1))
}