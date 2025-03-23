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

package com.nlab.reminder.core.local.database.model

import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong
import kotlinx.datetime.Instant

/**
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
    @ColumnInfo(name = "repeat_frequency") @RepeatFrequency val repeatFrequency: String?,
    @ColumnInfo(name = "repeat_frequency_value") @IntRange(from = 1, to = 999) val repeatFrequencyValue: Long?,
)

data class ScheduleContentDTO(
    val title: NonBlankString,
    val description: NonBlankString?,
    val link: NonBlankString?,
    val triggerTimeDTO: TriggerTimeDTO?,
    val frequencyDTO: RepeatFrequencyDTO?
)

data class TriggerTimeDTO(
    val utcTime: Instant,
    val isDateOnly: Boolean
)

data class RepeatFrequencyDTO(
    @RepeatFrequency val code: String,
    val value: Long
)

internal fun ScheduleEntity(
    contentDTO: ScheduleContentDTO,
    visiblePriority: NonNegativeLong,
): ScheduleEntity = ScheduleEntity(
    title = contentDTO.title.value,
    description = contentDTO.description?.value,
    link = contentDTO.link?.value,
    triggerTimeUtc = contentDTO.triggerTimeDTO?.utcTime,
    isTriggerTimeDateOnly = contentDTO.triggerTimeDTO?.isDateOnly,
    repeatFrequency = contentDTO.frequencyDTO?.code,
    repeatFrequencyValue = contentDTO.frequencyDTO?.value,
    visiblePriority = visiblePriority.value,
    isComplete = false
)

internal fun ScheduleEntity(
    baseEntity: ScheduleEntity,
    contentDTO: ScheduleContentDTO
): ScheduleEntity = baseEntity.copy(
    title = contentDTO.title.value,
    description = contentDTO.description?.value,
    link = contentDTO.link?.value,
    triggerTimeUtc = contentDTO.triggerTimeDTO?.utcTime,
    isTriggerTimeDateOnly = contentDTO.triggerTimeDTO?.isDateOnly
)

internal fun ScheduleEntity.equalsContent(contentDTO: ScheduleContentDTO): Boolean =
    title == contentDTO.title.value
            && description == contentDTO.description?.value
            && link == contentDTO.link?.value
            && triggerTimeUtc == contentDTO.triggerTimeDTO?.utcTime
            && isTriggerTimeDateOnly == contentDTO.triggerTimeDTO?.isDateOnly