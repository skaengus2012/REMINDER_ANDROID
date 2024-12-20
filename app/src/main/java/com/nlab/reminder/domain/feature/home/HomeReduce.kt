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

import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.core.component.text.UiText
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.domain.feature.home.HomeAction.*
import com.nlab.reminder.domain.feature.home.HomeUiState.*

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
                interaction = HomeInteraction.Empty,
                userMessages = emptyList()
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
        fun HomeInteraction.isTagEditStateUnsupported(): Boolean =
            this !is HomeInteraction.Empty && this !is HomeInteraction.TagEdit
        transition<Success> {
            if (current.interaction.isTagEditStateUnsupported()) current
            else current.copy(
                interaction = action.state
                    ?.let(HomeInteraction::TagEdit)
                    ?: HomeInteraction.Empty
            )
        }
        scope(isMatch = { action.state != null }) {
            effect<Loading> { environment.tagEditDelegate.clearState() }
            effect<Success> {
                if (current.interaction.isTagEditStateUnsupported()) {
                    environment.tagEditDelegate.clearState()
                }
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
                environment.tagEditDelegate
                    .startEditing(tag = action.tag)
                    .onFailure { dispatch(UserMessagePosted(UiText(StringIds.tag_not_found))) }
            }
        }
        scope(isMatch = { current.interaction is HomeInteraction.TagEdit }) {
            effect<OnTagRenameRequestClicked> { environment.tagEditDelegate.startRename() }
            effect<OnTagRenameInputReady> { environment.tagEditDelegate.readyRenameInput() }
            effect<OnTagRenameInputted> { environment.tagEditDelegate.changeRenameText(action.text) }
            suspendEffect<OnTagRenameConfirmClicked> {
                environment.tagEditDelegate
                    .tryUpdateTagName(current.tags)
                    .onFailure { dispatch(UserMessagePosted(UiText(StringIds.unknown_error))) }
            }
            suspendEffect<OnTagReplaceConfirmClicked> {
                environment.tagEditDelegate
                    .mergeTag()
                    .onFailure { dispatch(UserMessagePosted(UiText(StringIds.unknown_error))) }
            }
            effect<OnTagReplaceCancelClicked> { environment.tagEditDelegate.cancelMergeTag() }
            effect<OnTagDeleteRequestClicked> { environment.tagEditDelegate.startDelete() }
            suspendEffect<OnTagDeleteConfirmClicked> {
                environment.tagEditDelegate
                    .deleteTag()
                    .onFailure { dispatch(UserMessagePosted(UiText(StringIds.unknown_error))) }
            }
        }
        transition<UserMessagePosted> { current.copy(userMessages = current.userMessages + action.message) }
        transition<UserMessageShown> { current.copy(userMessages = current.userMessages - action.message) }
        transition<Interacted> { current.copy(interaction = HomeInteraction.Empty) }
        effect<Interacted> {
            if (current.interaction is HomeInteraction.TagEdit) {
                environment.tagEditDelegate.clearState()
            }
        }
    }
}