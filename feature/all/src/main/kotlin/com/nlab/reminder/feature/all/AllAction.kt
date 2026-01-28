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

package com.nlab.reminder.feature.all

import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.NonBlankString
import kotlin.time.Instant

/**
 * @author Thalys
 */
internal sealed interface AllAction {
    data class StateSynced(
        val entryAt: Instant,
        val userScheduleListResourceReport: UserScheduleListResourceReport
    ) : AllAction

    data class UndoScheduleResources(
        val prevScheduleResources: List<UserScheduleListResource>,
        val prevReplayStamp: Long,
    ) : AllAction

    data class CompletedScheduleVisibilityChangeClicked(val visible: Boolean) : AllAction

    data class SelectionModeClicked(val enabled: Boolean) : AllAction

    data object MenuClicked : AllAction

    data object MenuDropdownDismissed : AllAction

    data class ItemSelectionUpdated(val selectedIds: Set<ScheduleId>) : AllAction

    data class ItemCompletionUpdated(val scheduleId: ScheduleId, val targetCompleted: Boolean) : AllAction

    data class ItemPositionUpdated(val snapshot: List<UserScheduleListResource>) : AllAction

    data class AddSchedule(val title: NonBlankString, val note: String) : AllAction

    data class EditSchedule(
        val id: ScheduleId,
        val title: NonBlankString,
        val note: NonBlankString?,
        val tagNames: Set<NonBlankString>
    ) : AllAction
}