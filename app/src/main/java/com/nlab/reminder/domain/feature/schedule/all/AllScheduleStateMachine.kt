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
import com.nlab.reminder.core.kotlin.util.onFailure
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.domain.common.schedule.*

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun AllScheduleStateMachine(
    sideEffectHandle: SideEffectHandle<AllScheduleSideEffect>,
    getAllScheduleSnapshot: GetAllScheduleSnapshotUseCase,
    modifyScheduleComplete: ModifyScheduleCompleteUseCase,
    completedScheduleShownRepository: CompletedScheduleShownRepository,
    scheduleRepository: ScheduleRepository
): StateMachine<AllScheduleEvent, AllScheduleState> = StateMachine {
    reduce {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> { AllScheduleState.Loading }
        }
        event<AllScheduleEvent.OnAllScheduleSnapshotLoaded> {
            stateNot<AllScheduleState.Init> { (event) -> AllScheduleState.Loaded(event.allSchedulesReport) }
        }
    }

    handle {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> {
                getAllScheduleSnapshot()
                    .collectWhileSubscribed { send(AllScheduleEvent.OnAllScheduleSnapshotLoaded(it)) }
            }
        }

        state<AllScheduleState.Loaded> {
            event<AllScheduleEvent.OnModifyScheduleCompleteClicked> { (event) ->
                modifyScheduleComplete(event.scheduleId, event.isComplete)
            }

            event<AllScheduleEvent.OnToggleCompletedScheduleShownClicked> { (_, state) ->
                completedScheduleShownRepository
                    .setShown(isShown = state.snapshot.isCompletedScheduleShown.not())
                    .onFailure { sideEffectHandle.post(AllScheduleSideEffect.ShowErrorPopup) }
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

            event<AllScheduleEvent.OnScheduleLinkClicked> { (event) ->
                val link = event.scheduleUiState.link
                if (link.isNotEmpty()) {
                    sideEffectHandle.post(AllScheduleSideEffect.NavigateScheduleLink(link))
                }
            }
        }
    }
}