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

import com.nlab.reminder.core.component.usermessage.getOrThrowMessage
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.feature.home.HomeAction.*
import com.nlab.reminder.feature.home.HomeUiState.*

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
                interaction = HomeInteraction.Empty
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
    actionScope<TagEditStateSynced> {
        transition<Success> {
            when (current.interaction) {
                is HomeInteraction.Empty,
                is HomeInteraction.TagEdit -> current.copy(
                    interaction = action.state?.let(HomeInteraction::TagEdit) ?: HomeInteraction.Empty
                )
                // When adding an Interaction type, effect activation is required.
                // else -> current
            }
        }
        scope(isMatch = { action.state != null }) {
            effect<Loading> { environment.tagEditStateMachine.clearState() }
            // When adding an Interaction type, effect activation is required.
            // effect<Success> {
            //   when (current.interaction) {
            //      is HomeInteraction.Empty,
            //      is HomeInteraction.TagEdit -> {
            //         do nothing.
            //      }
            //      else -> environment.tagEditDelegate.clearState()
            // }
        }
    }
    stateScope<Success> {
        scope(isMatch = { current.interaction == HomeInteraction.Empty }) {
            effect<OnTagLongClicked> {
                environment.tagEditStateMachine.startEditing(tag = action.tag)
            }
        }
        scope(isMatch = { current.interaction is HomeInteraction.TagEdit }) {
            suspendEffect<OnTagRenameRequestClicked> {
                environment.tagEditStateMachine
                    .startRename()
                    .getOrThrowMessage()
            }
            effect<OnTagRenameInputReady> { environment.tagEditStateMachine.readyRenameInput() }
            effect<OnTagRenameInputted> { environment.tagEditStateMachine.changeRenameText(action.text) }
            suspendEffect<OnTagRenameConfirmClicked> {
                environment.tagEditStateMachine
                    .tryUpdateName(current.tags)
                    .getOrThrowMessage()
            }
            suspendEffect<OnTagReplaceConfirmClicked> {
                environment.tagEditStateMachine
                    .merge()
                    .getOrThrowMessage()
            }
            effect<OnTagReplaceCancelClicked> { environment.tagEditStateMachine.cancelMerge() }
            suspendEffect<OnTagDeleteRequestClicked> {
                environment.tagEditStateMachine
                    .startDelete()
                    .getOrThrowMessage()
            }
            suspendEffect<OnTagDeleteConfirmClicked> {
                environment.tagEditStateMachine
                    .delete()
                    .getOrThrowMessage()
            }
        }
        transition<Interacted> { current.copy(interaction = HomeInteraction.Empty) }
        effect<Interacted> {
            if (current.interaction is HomeInteraction.TagEdit) {
                environment.tagEditStateMachine.clearState()
            }
        }
    }
}