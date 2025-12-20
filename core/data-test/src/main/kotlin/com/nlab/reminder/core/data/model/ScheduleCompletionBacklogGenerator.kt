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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeInt
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genIntGreaterThanZero
import com.nlab.testkit.faker.genLong

/**
 * @author Doohyun
 */
fun genScheduleCompletionBacklogId(): ScheduleCompletionBacklogId = ScheduleCompletionBacklogId(rawId = genLong())

fun genScheduleCompletionBacklog(
    id: ScheduleCompletionBacklogId = genScheduleCompletionBacklogId(),
    scheduleId: ScheduleId = genScheduleId(),
    targetCompleted: Boolean = genBoolean(),
    priority: NonNegativeInt = genNonNegativeInt()
): ScheduleCompletionBacklog = ScheduleCompletionBacklog(
    id = id,
    scheduleId = scheduleId,
    targetCompleted = targetCompleted,
    priority = priority
)

fun genScheduleCompletionBacklogs(
    count: PositiveInt = genIntGreaterThanZero(max = 10).toPositiveInt()
): Set<ScheduleCompletionBacklog> = (1..count.value).toSet { index ->
    genScheduleCompletionBacklog(
        id = ScheduleCompletionBacklogId(rawId = index.toLong())
    )
}