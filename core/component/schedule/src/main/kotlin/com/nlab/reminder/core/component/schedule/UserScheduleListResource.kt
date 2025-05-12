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

package com.nlab.reminder.core.component.schedule

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.kotlin.NonBlankString

/**
 * @author Thalys
 */
data class UserScheduleListResource(
    private val schedule: Schedule,
    val linkMetadata: LinkMetadata?,
    val tags: List<Tag>,
) {
    val id: ScheduleId get() = schedule.id
    val title: NonBlankString get() = schedule.content.title
    val note: NonBlankString? get() = schedule.content.note
    val link: Link? get() = schedule.content.link
    val timing: ScheduleTiming? get() = schedule.content.timing
}