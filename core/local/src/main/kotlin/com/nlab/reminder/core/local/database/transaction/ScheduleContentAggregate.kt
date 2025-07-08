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

package com.nlab.reminder.core.local.database.transaction

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.local.database.dao.ScheduleHeadlineSaveInput
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatSaveInput
import com.nlab.reminder.core.local.database.dao.ScheduleTimingSaveInput
import com.nlab.reminder.core.local.database.entity.RepeatSettingProperty
import com.nlab.reminder.core.local.database.entity.RepeatType
import kotlin.time.Instant

/**
 * @author Doohyun
 */
data class ScheduleContentAggregate(
    val headline: ScheduleHeadlineSaveInput,
    val timing: ScheduleTimingAggregate?,
    val tagIds: Set<Long>,
)

data class ScheduleTimingAggregate(
    val triggerAt: Instant,
    val isTriggerAtDateOnly: Boolean,
    val repeat: ScheduleRepeatAggregate?
)

data class ScheduleRepeatAggregate(
    @RepeatType val type: String,
    val interval: PositiveInt,
    val details: Set<ScheduleRepeatDetailAggregate>
)

data class ScheduleRepeatDetailAggregate(
    @RepeatSettingProperty val propertyCode: String,
    val value: String
)

internal fun ScheduleTimingAggregate.toScheduleTimingSaveInput() = ScheduleTimingSaveInput(
    triggerAt = triggerAt,
    isTriggerAtDateOnly = isTriggerAtDateOnly,
    repeatInput = repeat?.let { ScheduleRepeatSaveInput(type = it.type, interval = it.interval) }
)