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

package com.nlab.reminder.core.schedule.model

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId

/**
 * @author thalys
 */
data class ScheduleElement(
    val schedule: Schedule,
    val isCompleteMarked: Boolean,
    val linkMetadata: LinkMetadata?,
    val isSelected: Boolean
) : ScheduleItem {
    val id: ScheduleId get() = schedule.id
    val title: String get() = schedule.title
    val note: String get() = schedule.note
    val link: Link get() = schedule.link
    val isComplete: Boolean get() = schedule.isComplete
    val visiblePriority: Long get() = schedule.visiblePriority
}