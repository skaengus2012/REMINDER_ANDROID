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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditStep
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.domain.feature.home.HomeAction.*
import com.nlab.reminder.domain.feature.home.HomeUiState.*

internal typealias HomeReduce = Reduce<HomeAction, HomeUiState>

/**
 * @author Doohyun
 */
internal fun HomeReduce(dependency: HomeDependency): HomeReduce = DslReduce {
    actionScope<StateSynced> {
        transition<Loading> {
            Success(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.tags,
                interaction = HomeInteraction.Empty,
                userMessages = emptyList()
            )
        }
        transition<Success> {
            current.copy(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.tags,
            )
        }
    }
    actionScope<TagEditStepSynced> {
        transition<Success> {
            if (current.interaction !is HomeInteraction.TagEdit) current
            else current.copy(
                interaction = when (action.step) {
                    is TagEditStep.Empty -> HomeInteraction.Empty
                    else -> HomeInteraction.TagEdit(action.step)
                }
            )
        }
        effect {
            val isTagEditEmpty = action.step is TagEditStep.Empty
            val isTagEditInteraction = (current as? Success)?.interaction is HomeInteraction.TagEdit
            if (isTagEditEmpty.not() && isTagEditInteraction.not()) {
                dependency.tagEditDelegate.clearStep()
            }
        }
    }
    stateScope<Success> {
        scope(isMatch = { current.interaction == HomeInteraction.Empty }) {
            transition<OnTodayCategoryClicked> {
                current.copy(interaction = HomeInteraction.TodaySchedule)
            }
            transition<OnTimetableCategoryClicked> {
                current.copy(interaction = HomeInteraction.TimetableSchedule)
            }
            transition<OnAllCategoryClicked> {
                current.copy(interaction = HomeInteraction.AllSchedule)
            }
            suspendEffect<OnTagLongClicked> {
                dependency.tagEditDelegate
                    .startEditing(tag = action.tag)
                    .onFailure { dispatch(UserMessagePosted(UserMessage(StringIds.tag_not_found))) }
            }
        }
        scope(isMatch = { current.interaction is HomeInteraction.TagEdit }) {
            effect<OnTagRenameRequestClicked> { dependency.tagEditDelegate.startRename() }
            effect<OnTagRenameInputReady> { dependency.tagEditDelegate.readyRenameInput() }
            effect<OnTagRenameInputted> { dependency.tagEditDelegate.changeRenameText(action.text) }
            suspendEffect<OnTagRenameConfirmClicked> {
                dependency.tagEditDelegate
                    .tryUpdateTagName(current.tags)
                    .onFailure { dispatch(UserMessagePosted(UserMessage(StringIds.unknown_error))) }
            }
            suspendEffect<OnTagReplaceConfirmClicked> {
                dependency.tagEditDelegate
                    .mergeTag()
                    .onFailure { dispatch(UserMessagePosted(UserMessage(StringIds.unknown_error))) }
            }
            effect<OnTagDeleteRequestClicked> { dependency.tagEditDelegate.startDelete() }
            suspendEffect<OnTagDeleteConfirmClicked> {
                dependency.tagEditDelegate
                    .deleteTag()
                    .onFailure { dispatch(UserMessagePosted(UserMessage(StringIds.unknown_error))) }
            }
        }
        transition<UserMessagePosted> { current.copy(userMessages = current.userMessages + action.message) }
        transition<UserMessageShown> { current.copy(userMessages = current.userMessages - action.message) }
        transition<Interacted> { current.copy(interaction = HomeInteraction.Empty) }
    }
}