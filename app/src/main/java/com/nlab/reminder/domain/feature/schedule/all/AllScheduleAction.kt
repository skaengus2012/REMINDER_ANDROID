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

/**
 * @author Doohyun
 */
sealed interface AllScheduleAction {
    /**
    data class ScheduleElementsLoaded(
        val scheduleElements: List<ScheduleElement>,
        val isCompletedScheduleShown: Boolean
    ) : AllScheduleAction

    @ContractUiAction
    object OnSelectionModeToggleClicked : AllScheduleAction

    @ContractUiAction
    object OnCompletedScheduleVisibilityToggleClicked : AllScheduleAction

    @ContractUiAction
    data class OnScheduleLinkClicked(val position: Int) : AllScheduleAction

    @ContractUiAction
    data class CompleteWorkflow(val workflow: AllScheduleWorkflow) : AllScheduleAction

    @ContractUiAction
    data class OnScheduleSelected(val position: Int, val isSelected: Boolean) : AllScheduleAction


    // update completion
    @ContractUiAction
    data class OnScheduleCompleteClicked(val position: Int, val isComplete: Boolean) : AllScheduleAction

    @ContractUiAction
    data class OnSelectedSchedulesCompleteClicked(val isComplete: Boolean) : AllScheduleAction, SelectedAction

    // delete
    @ContractUiAction
    data class OnScheduleDeleteClicked(val position: Int) : AllScheduleAction

    @ContractUiAction
    object OnSelectedSchedulesDeleteClicked : AllScheduleAction, SelectedAction

    @ContractUiAction
    object OnCompletedScheduleDeleteClicked : AllScheduleAction

    @ContractUiAction
    data class OnScheduleItemMoved(val fromPosition: Int, val toPosition: Int) : AllScheduleAction

    @ContractUiAction
    object AppliedSelectedActionWithSchedules : AllScheduleAction
    */
}