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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.state.Event
import com.nlab.reminder.domain.common.schedule.ScheduleId
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import com.nlab.reminder.domain.common.schedule.SelectionDisable
import com.nlab.state.core.lifecycle.PublicEvent

/**
 * @author Doohyun
 */
sealed class AllScheduleEvent private constructor() : Event {
    object Fetch : AllScheduleEvent()

    data class StateLoaded(
        val scheduleSnapshot: AllScheduleSnapshot,
        val isSelectionEnabled: Boolean
    ) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    object OnToggleCompletedScheduleShownClicked : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    object OnToggleSelectionModeEnableClicked : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    object OnDeleteCompletedScheduleClicked : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    object OnSelectedScheduleDeleteClicked : AllScheduleEvent(), SelectionDisable

    @PublicEvent(AllScheduleViewModel::class)
    data class OnScheduleCompleteClicked(
        val scheduleId: ScheduleId,
        val isComplete: Boolean
    ) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    data class OnDragScheduleEnded(val draggedSnapshot: List<ScheduleUiState>) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    data class OnDeleteScheduleClicked(val scheduleId: ScheduleId) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    data class OnScheduleLinkClicked(val scheduleId: ScheduleId) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    data class OnScheduleSelected(
        val scheduleId: ScheduleId,
        val isSelected: Boolean
    ) : AllScheduleEvent()

    @PublicEvent(AllScheduleViewModel::class)
    data class OnSelectedScheduleCompleteClicked(
        val isComplete: Boolean
    ) : AllScheduleEvent(), SelectionDisable
}