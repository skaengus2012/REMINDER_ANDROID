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

package com.nlab.reminder.core.component.schedule.ui.view.list

import com.nlab.reminder.core.data.model.ScheduleId

/**
 * @author Doohyun
 */
interface ScheduleListSelectionSource {
    fun findScheduleId(absolutePosition: Int): ScheduleId?
    fun findSelected(scheduleId: ScheduleId): Boolean
}

fun ScheduleListSelectionSource(
    adapter: ScheduleListAdapter
): ScheduleListSelectionSource = object : ScheduleListSelectionSource {
    override fun findScheduleId(absolutePosition: Int): ScheduleId? {
        return (adapter.getCurrentList().getOrNull(absolutePosition) as? ScheduleAdapterItem.Content)
            ?.scheduleDetail
            ?.schedule
            ?.id
    }

    override fun findSelected(scheduleId: ScheduleId): Boolean {
        return scheduleId in adapter.getCurrentSelected()
    }
}