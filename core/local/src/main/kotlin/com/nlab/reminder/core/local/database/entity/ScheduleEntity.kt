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

import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * ScheduleEntity Contracts
 *
 * #### Contract 1
 * The set of the following values has the same life cycle.
 * - [triggerTimeUtc], [isTriggerTimeDateOnly]
 * - [repeatType], [repeatInterval]
 *
 * #### Contract 2
 * [repeatType] can exist when [triggerTimeUtc] exists.
 *
 * #### Contract 3
 * If [RepeatType] is [REPEAT_WEEKLY], [REPEAT_MONTHLY], [REPEAT_YEARLY],
 * There must be a value in [RepeatDetailEntity].
 *
 * @author Doohyun
 */
@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "schedule_id") val scheduleId: Long = EMPTY_GENERATED_ID,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "link") val link: String? = null,
    @ColumnInfo(name = "visible_priority") val visiblePriority: Long,
    @ColumnInfo(name = "is_complete") val isComplete: Boolean,
    @ColumnInfo(name = "trigger_time_utc") val triggerTimeUtc: Instant?,
    @ColumnInfo(name = "is_trigger_time_date_only") val isTriggerTimeDateOnly: Boolean?,
    @ColumnInfo(name = "repeat_type") @RepeatType val repeatType: String?,
    @ColumnInfo(name = "repeat_interval") @IntRange(from = 1) val repeatInterval: Int?,
)