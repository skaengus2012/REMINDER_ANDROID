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

package com.nlab.reminder.internal.data.model

import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.internal.common.android.database.ScheduleEntity
import com.nlab.reminder.internal.common.android.database.ScheduleEntityWithTagEntities
import com.nlab.reminder.internal.common.android.database.TagEntity
import kotlinx.collections.immutable.toImmutableList

/**
 * @author thalys
 */
internal fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
    scheduleId = scheduleId.value,
    title = title,
    description = note,
    link = link,
    visiblePriority = visiblePriority,
    isComplete = isComplete
)

internal fun ScheduleEntityWithTagEntities.toModel(): Schedule = Schedule(
    scheduleId = ScheduleId(scheduleEntity.scheduleId),
    title = scheduleEntity.title,
    note = scheduleEntity.description.orEmpty(),
    link = scheduleEntity.link.orEmpty(),
    visiblePriority = scheduleEntity.visiblePriority,
    isComplete = scheduleEntity.isComplete,
    tags = tagEntities.map(TagEntity::toModel).toImmutableList()
)