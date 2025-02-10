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

package com.nlab.reminder.core.local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Details of schedule repetition.
 * Depending on the [frequencySetting], a polymorphic value exists.
 *
 * case1. [frequencySetting] is [REPEAT_FREQUENCY_WEEKLY]
 * [value] can be [RepeatWeek].
 *
 * case2. [frequencySetting] is [REPEAT_FREQUENCY_MONTHLY]
 * [value] can be number range (1 ~ 31) or [RepeatDayOrder], [RepeatDays] together
 *
 * case3. [frequencySetting] is [REPEAT_FREQUENCY_YEARLY]
 * [value] can be [RepeatMonth] and [RepeatDayOrder], [RepeatDays] together optionally.
 *
 * @author Thalys
 */
@Entity(
    tableName = "repeat_detail",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["schedule_id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class RepeatDetailEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "repeat_id") val repeatId: Long = EMPTY_GENERATED_ID,
    @ColumnInfo(name = "schedule_id", index = true) val scheduleId: Long,
    @ColumnInfo(name = "frequency_setting") @RepeatFrequencySetting val frequencySetting: String,
    @ColumnInfo(name = "value") val value: String
)