/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.component.tag.edit.TagEditStateTransition
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.feature.home.HomeAction.*
import com.nlab.reminder.feature.home.HomeUiState.*
import kotlinx.coroutines.flow.Flow

internal typealias HomeReduce = Reduce<HomeAction, HomeUiState>

/**
 * @author Doohyun
 */
internal fun HomeReduce(environment: HomeEnvironment): HomeReduce = DslReduce {
    actionScope<StateSynced> {
        transition<Loading> {
            Success(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.sortedTags,
                tagEditState = TagEditState.None
            )
        }
        transition<Success> {
            current.copy(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.sortedTags,
            )
        }
    }
    stateScope<Success> {
        transition<CompareAndSetTagEditState> {
            if (current.tagEditState == action.expectedState) current.copy(tagEditState = action.newState)
            else current
        }
        transition<OnTagLongClicked> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .startEditing(current = current.tagEditState, tag = action.tag)
            )
        }
        suspendEffect<OnTagRenameRequestClicked> {
            environment.tagEditTaskExecutor
                .processAsFlow(
                    task = environment
                        .tagEditStateMachine
                        .startRename(current.tagEditState),
                    current = current.tagEditState
                )
                .mapToAction()
                .collect(::dispatch)
        }
        transition<OnTagRenameInputReady> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .readyRenameInput(current.tagEditState)
            )
        }
        transition<OnTagRenameInputted> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .changeRenameText(current.tagEditState, action.text)
            )
        }
        suspendEffect<OnTagRenameConfirmClicked> {
            environment.tagEditTaskExecutor
                .processAsFlow(
                    task = environment
                        .tagEditStateMachine
                        .tryUpdateName(
                            current = current.tagEditState,
                            compareTags = current.tags
                        ),
                    current = current.tagEditState
                )
                .mapToAction()
                .collect(::dispatch)
        }
        suspendEffect<OnTagReplaceConfirmClicked> {
            environment.tagEditTaskExecutor
                .processAsFlow(
                    task = environment
                        .tagEditStateMachine
                        .merge(current = current.tagEditState),
                    current = current.tagEditState
                )
                .mapToAction()
                .collect(::dispatch)
        }
        transition<OnTagReplaceCancelClicked> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .cancelMerge(current = current.tagEditState)
            )
        }
        suspendEffect<OnTagDeleteRequestClicked> {
            environment.tagEditTaskExecutor
                .processAsFlow(
                    task = environment
                        .tagEditStateMachine
                        .startDelete(current = current.tagEditState),
                    current = current.tagEditState
                )
                .mapToAction()
                .collect(::dispatch)
        }
        suspendEffect<OnTagDeleteConfirmClicked> {
            environment.tagEditTaskExecutor
                .processAsFlow(
                    task = environment
                        .tagEditStateMachine
                        .delete(current = current.tagEditState),
                    current = current.tagEditState,
                )
                .mapToAction()
                .collect(::dispatch)
        }
        transition<OnTagEditCancelClicked> {
            current.copy(tagEditState = TagEditState.None)
        }
    }
}

private fun Flow<TagEditStateTransition>.mapToAction(): Flow<CompareAndSetTagEditState> = map { transition ->
    CompareAndSetTagEditState(
        expectedState = transition.previousState,
        newState = transition.updatedState
    )
}