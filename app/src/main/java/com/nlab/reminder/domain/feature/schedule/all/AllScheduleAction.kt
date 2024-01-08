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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.statekit.Action

/**
 * @author Doohyun
 */
internal sealed interface AllScheduleAction : Action {
    data class ScheduleLoaded(
        val schedules: List<Schedule>,
        val isCompletedScheduleShown: Boolean
    ) : AllScheduleAction

    data class LinkMetadataLoaded(val linkTables: LinkMetadataTable) : AllScheduleAction
    data class CompleteMarkLoaded(val table: Map<ScheduleId, Boolean>) : AllScheduleAction

    data class OnSelectionModeUpdateClicked(val isSelectionMode: Boolean) : AllScheduleAction
    data class OnCompletedScheduleVisibilityUpdateClicked(val isVisible: Boolean) : AllScheduleAction
    data class OnScheduleLinkClicked(val id: ScheduleId) : AllScheduleAction
    data class CompleteWorkflow(val workflow: AllScheduleWorkflow) : AllScheduleAction

    // update completion
    data class OnScheduleCompleteClicked(
        val id: ScheduleId,
        val isComplete: Boolean
    ) : AllScheduleAction

    data class OnSelectedSchedulesCompleteClicked(
        val ids: Collection<ScheduleId>,
        val isComplete: Boolean
    ) : AllScheduleAction

    // delete
    data class OnScheduleDeleteClicked(
        val id: ScheduleId
    ) : AllScheduleAction

    data class OnSelectedSchedulesDeleteClicked(val ids: Collection<ScheduleId>) : AllScheduleAction
    object OnCompletedScheduleDeleteClicked : AllScheduleAction
}