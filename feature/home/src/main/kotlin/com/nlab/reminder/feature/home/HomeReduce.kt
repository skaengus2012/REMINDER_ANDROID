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
import com.nlab.reminder.core.component.tag.edit.processAsFlow
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.getOrThrowMessage
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
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
        transition<TagLongClicked> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .startEditing(current = current.tagEditState, tag = action.tag)
            )
        }
        suspendEffect<TagRenameRequestClicked> {
            environment.tagEditStateMachine
                .startRename(current.tagEditState)
                .processAsFlow()
                .mapOrThrowCompareAndSetTagEditState(environment.userMessageFactory)
                .collect(::dispatch)
        }
        transition<TagRenameInputReady> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .readyRenameInput(current.tagEditState)
            )
        }
        transition<TagRenameInputted> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .changeRenameText(current.tagEditState, action.text)
            )
        }
        suspendEffect<TagRenameConfirmClicked> {
            environment.tagEditStateMachine
                .tryUpdateName(current = current.tagEditState, compareTags = current.tags)
                .processAsFlow()
                .mapOrThrowCompareAndSetTagEditState(environment.userMessageFactory)
                .collect(::dispatch)
        }
        suspendEffect<TagReplaceConfirmClicked> {
            environment.tagEditStateMachine
                .merge(current = current.tagEditState)
                .processAsFlow()
                .mapOrThrowCompareAndSetTagEditState(environment.userMessageFactory)
                .collect(::dispatch)
        }
        transition<TagReplaceCancelClicked> {
            current.copy(
                tagEditState = environment
                    .tagEditStateMachine
                    .cancelMerge(current = current.tagEditState)
            )
        }
        suspendEffect<TagDeleteRequestClicked> {
            environment.tagEditStateMachine
                .startDelete(current = current.tagEditState)
                .processAsFlow()
                .mapOrThrowCompareAndSetTagEditState(environment.userMessageFactory)
                .collect(::dispatch)
        }
        suspendEffect<TagDeleteConfirmClicked> {
            environment.tagEditStateMachine
                .delete(current = current.tagEditState)
                .processAsFlow()
                .mapOrThrowCompareAndSetTagEditState(environment.userMessageFactory)
                .collect(::dispatch)
        }
        transition<TagEditCancelClicked> {
            current.copy(tagEditState = TagEditState.None)
        }
    }
}

private fun Flow<Result<TagEditStateTransition>>.mapOrThrowCompareAndSetTagEditState(
    userMessageFactory: UserMessageFactory
): Flow<CompareAndSetTagEditState> = map { transitionResult ->
    val transition = transitionResult.getOrThrowMessage { userMessageFactory.createExceptionSource() }
    CompareAndSetTagEditState(
        expectedState = transition.previous,
        newState = transition.updated
    )
}