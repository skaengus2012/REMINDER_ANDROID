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

import com.nlab.reminder.core.schedule.state.SelectedAction
import com.nlab.statekit.Reducer
import com.nlab.statekit.util.buildDslReducer
import kotlinx.collections.immutable.*
import javax.inject.Inject

private typealias DomainReducer = Reducer<AllScheduleAction, AllScheduleUiState>

/**
 * @author Doohyun
 */
/**
class AllScheduleReducer @Inject constructor() : DomainReducer by buildDslReducer(defineDSL = {
    action<AllScheduleAction.ScheduleElementsLoaded> {
        state<AllScheduleUiState.Empty> { (action) ->
            AllScheduleUiState.Loaded(
                scheduleElements = action.scheduleElements.toImmutableList(),
                isCompletedScheduleShown = action.isCompletedScheduleShown,
                isSelectionMode = false,
                isSelectedActionInvoked = false,
                workflows = persistentListOf()
            )
        }
        state<AllScheduleUiState.Loaded> { (action, before) ->
            before.copy(
                scheduleElements = action.scheduleElements.toImmutableList(),
                isCompletedScheduleShown = action.isCompletedScheduleShown
            )
        }
    }
    state<AllScheduleUiState.Loaded> {
        filteredAction(predicate = { it is SelectedAction }) { (_, before) ->
            before.copy(isSelectionMode = false, isSelectedActionInvoked = true)
        }
        action<AllScheduleAction.OnSelectionModeToggleClicked> { (_, before) ->
            val nextSelectionMode = before.isSelectionMode.not()
            before.copy(
                isSelectionMode = nextSelectionMode,
                isSelectedActionInvoked = nextSelectionMode.not()
            )
        }
        action<AllScheduleAction.AppliedSelectedActionWithSchedules> { (_, before) ->
            before.copy(isSelectedActionInvoked = false)
        }
        action<AllScheduleAction.OnScheduleLinkClicked> { (action, before) ->
            val link = before.scheduleElements.getOrNull(action.position)?.link.orEmpty()
            if (link.isEmpty()) before
            else before.copy(workflows = before.workflows.toPersistentList() + AllScheduleWorkflow.LinkPage(link))
        }
        action<AllScheduleAction.CompleteWorkflow> { (action, before) ->
            before.copy(workflows = before.workflows.toPersistentList() - action.workflow)
        }
    }
})*/