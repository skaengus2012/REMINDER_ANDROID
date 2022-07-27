/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.schedule.view

import com.nlab.reminder.domain.common.android.view.recyclerview.ItemModel
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
@ItemModel
data class ScheduleItem(
    private val schedule: Schedule,
    val onCompleteToggleClicked: () -> Unit
) {
    val scheduleId: Long
        get() = schedule.scheduleId

    val title: String
        get() = schedule.title

    val note: String?
        get() = schedule.note

    val url: String?
        get() = schedule.url

    val tags: List<Tag>
        get() = schedule.tags

    val visiblePriority: Int
        get() = schedule.visiblePriority

    val isComplete: Boolean
        get() = schedule.isComplete
}