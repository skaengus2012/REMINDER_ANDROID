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

package com.nlab.reminder.domain.common.schedule

import com.nlab.reminder.core.util.test.annotation.Generated
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
@Generated
data class Schedule(
    private val scheduleId: Long,
    val title: String,
    val note: String?,
    val url: String?,
    val tags: List<Tag>,
    val visiblePriority: Int,
    val isComplete: Boolean
) {
    fun id(): ScheduleId = ScheduleId(scheduleId)

    companion object {
        @Generated
        fun empty(): Schedule = Schedule(
            scheduleId = Long.MIN_VALUE,
            title = "",
            note = null,
            url = null,
            tags = emptyList(),
            visiblePriority = Int.MIN_VALUE,
            isComplete = false
        )
    }
}