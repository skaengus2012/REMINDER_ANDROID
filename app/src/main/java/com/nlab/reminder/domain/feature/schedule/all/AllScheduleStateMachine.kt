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

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.kotlin.collection.minOf
import com.nlab.reminder.core.kotlin.collection.maxOf
import com.nlab.reminder.core.kotlin.coroutine.flow.*
import com.nlab.reminder.core.kotlin.util.onFailure
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.schedule.SelectionModeRepository
import com.nlab.reminder.domain.common.schedule.util.asSelectedSchedules
import com.nlab.reminder.domain.common.schedule.visibleconfig.*

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun AllScheduleStateMachine(
    sideEffectHandle: SideEffectHandle<AllScheduleSideEffect>,
    getAllScheduleSnapshot: GetAllScheduleSnapshotUseCase,
    updateComplete: UpdateCompleteUseCase,
    bulkUpdateComplete: BulkUpdateCompleteUseCase,
    completedScheduleShownRepository: CompletedScheduleShownRepository,
    scheduleRepository: ScheduleRepository,
    selectionModeRepository: SelectionModeRepository,
    selectionRepository: SelectionRepository
): StateMachine<AllScheduleEvent, AllScheduleState> = StateMachine {
    reduce {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> { AllScheduleState.Loading }
        }
        event<AllScheduleEvent.StateLoaded> {
            stateNot<AllScheduleState.Init> { (event) ->
                AllScheduleState.Loaded(
                    event.scheduleSnapshot.scheduleUiStates,
                    isCompletedScheduleShown = event.scheduleSnapshot.isCompletedScheduleShown,
                    isSelectionMode = event.isSelectionEnabled
                )
            }
        }
    }

    handle {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> {
                combine(
                    getAllScheduleSnapshot(),
                    selectionModeRepository.enabledStream(),
                    transform = { allScheduleSnapshot, isSelectionEnabled ->
                        AllScheduleEvent.StateLoaded(allScheduleSnapshot, isSelectionEnabled)
                    })
                    .collectWhileSubscribed { send(it) }
            }

            state<AllScheduleState.Init> {
                selectionModeRepository.enabledStream()
                    .collect { isEnabled ->
                        if (isEnabled.not()) {
                            selectionRepository.clearSelected()
                        }
                    }
            }
        }

        state<AllScheduleState.Loaded> {
            event<AllScheduleEvent.OnScheduleCompleteClicked> { (event) ->
                updateComplete(event.scheduleId, event.isComplete)
            }

            event<AllScheduleEvent.OnToggleCompletedScheduleShownClicked> { (_, state) ->
                completedScheduleShownRepository
                    .setShown(isShown = state.isCompletedScheduleShown.not())
                    .onFailure { sideEffectHandle.post(AllScheduleSideEffect.ShowErrorPopup) }
            }

            event<AllScheduleEvent.OnToggleSelectionModeEnableClicked> { (_, state) ->
                selectionModeRepository.setEnabled(state.isSelectionMode.not())
            }

            event<AllScheduleEvent.OnDeleteCompletedScheduleClicked> {
                scheduleRepository.delete(DeleteRequest.ByComplete(isComplete = true))
            }

            filteredEvent(predicate = { event ->
                event is AllScheduleEvent.OnDragScheduleEnded && event.draggedSnapshot.isNotEmpty()
            }) { (event) ->
                val items: List<ScheduleUiState> = (event as AllScheduleEvent.OnDragScheduleEnded).draggedSnapshot
                val minVisiblePriority: Long = items.minOf { it.visiblePriority }
                val maxVisiblePriority: Long = items.maxOf { it.visiblePriority }
                val request: UpdateRequest =
                    items
                        .mapIndexed { index, uiState ->
                            Pair(
                                ModifyVisiblePriorityRequest(
                                    uiState.id,
                                    minOf(minVisiblePriority + index, maxVisiblePriority)
                                ),
                                uiState.visiblePriority
                            )
                        }
                        .filter { (request, visiblePriority) -> visiblePriority != request.visiblePriority }
                        .map { (request) -> request }
                        .let(UpdateRequest::VisiblePriorities)
                scheduleRepository.update(request)
            }

            event<AllScheduleEvent.OnDeleteScheduleClicked> { (event) ->
                scheduleRepository.delete(DeleteRequest.ById(event.scheduleId))
            }

            event<AllScheduleEvent.OnScheduleLinkClicked> { (event, before) ->
                val uiState: ScheduleUiState? =
                    before.scheduleUiStates.find { it.id == event.scheduleId }
                if (uiState != null && uiState.link.isNotBlank()) {
                    sideEffectHandle.post(AllScheduleSideEffect.NavigateScheduleLink(uiState.link))
                }
            }

            event<AllScheduleEvent.OnScheduleSelected> { (event, before) ->
                val uiState: ScheduleUiState? =
                    before.scheduleUiStates.find { uiState -> uiState.id == event.scheduleId }
                if (uiState != null) {
                    selectionRepository.setSelected(uiState.id, isSelect = event.isSelected)
                }
            }

            event<AllScheduleEvent.OnSelectedScheduleDeleteClicked> { (_, state) ->
                val selectedUiStateIds: List<ScheduleId> =
                    state.scheduleUiStates.filter { it.isSelected }.map { it.id }
                if (selectedUiStateIds.isNotEmpty()) {
                    scheduleRepository.delete(DeleteRequest.ByIds(selectedUiStateIds))
                }
            }

            event<AllScheduleEvent.OnSelectedScheduleCompleteClicked> { (event, state) ->
                bulkUpdateComplete(
                    schedules = state.scheduleUiStates.asSelectedSchedules(),
                    isComplete = event.isComplete
                )
            }

            filteredEvent(predicate = { event -> event is SelectionDisable }) {
                selectionModeRepository.setEnabled(false)
            }
        }
    }
}