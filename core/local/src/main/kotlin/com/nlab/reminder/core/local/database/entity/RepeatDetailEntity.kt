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

package com.nlab.reminder.core.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Details of schedule repetition.
 * Depending on the [propertyCode], a polymorphic value exists.
 *
 * case1. [propertyCode] is [REPEAT_WEEKLY]
 * [value] can be [RepeatWeek]
 * - [REPEAT_SETTING_PROPERTY_WEEKLY] (Multiple, At least one must exist.)
 *
 * case2. [propertyCode] is [REPEAT_MONTHLY]
 * [value] can be number range `(1 ~ 31)` or [RepeatDayOrder], [RepeatDays] together
 * - [REPEAT_SETTING_PROPERTY_MONTHLY_DAY] (At least one must exist.)
 * - [REPEAT_SETTING_PROPERTY_MONTHLY_DAY_ORDER] (It must exist at each setting.)
 * - [REPEAT_SETTING_PROPERTY_MONTHLY_DAY_OF_WEEK]
 *
 * case3. [propertyCode] is [REPEAT_YEARLY]
 * [value] can be [RepeatMonth] and [RepeatDayOrder], [RepeatDays] together optionally.
 * - [REPEAT_SETTING_PROPERTY_YEARLY_MONTH] (Multiple, At least one must exist.)
 * - [REPEAT_SETTING_PROPERTY_YEARLY_DAY_ORDER] (If existed, it must exist at each setting)
 * - [REPEAT_SETTING_PROPERTY_YEARLY_DAY_OF_WEEK]
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
data class RepeatDetailEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "repeat_id") val repeatId: Long = EMPTY_GENERATED_ID,
    @ColumnInfo(name = "schedule_id", index = true) val scheduleId: Long,
    @ColumnInfo(name = "property_code") @RepeatSettingProperty val propertyCode: String,
    @ColumnInfo(name = "value") val value: String
)