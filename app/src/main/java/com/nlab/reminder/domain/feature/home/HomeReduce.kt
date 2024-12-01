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

import com.nlab.reminder.core.component.tag.edit.LazyTagEditResult
import com.nlab.reminder.core.component.tag.edit.TagEditStep
import com.nlab.reminder.core.kotlin.getOrElse
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.reminder.domain.feature.home.HomeAction.*
import com.nlab.reminder.domain.feature.home.HomeUiState.*

/**
 * @author Doohyun
 */
internal fun HomeReduce(dependency: HomeDependency): Reduce<HomeAction, HomeUiState> = DslReduce {
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
            effect<OnTagLongClicked> {
                val nextAction = dependency.tagEditDelegate
                    .startEditing(tag = action.tag)
                    .map { TagEditStarted(it) }
                    .getOrElse { UserMessagePosted(UserMessage(StringIds.tag_not_found)) }
                dispatch(nextAction)
            }
            transition<TagEditStarted> {
                current.copy(interaction = HomeInteraction.TagEdit(action.intro))
            }
            transition<TagEditChangedLazily> {
                current.copy(
                    interaction = current.tagEditStep
                        .let(action.toNextStepInteraction::invoke)
                        ?.toTagEdit()
                        ?: HomeInteraction.Empty
                )
            }
        }
        scope(isMatch = { current.interaction is HomeInteraction.TagEdit }) {
            transition<OnTagRenameRequestClicked> {
                current.copy(
                    interaction = dependency
                        .tagEditDelegate
                        .startRename(current.requireTagEditInteraction())
                        .toTagEdit()
                )
            }
            transition<OnTagRenameInputReady> {
                current.copy(
                    interaction = dependency
                        .tagEditDelegate
                        .readyRenameInput(current.requireTagEditInteraction())
                        .toTagEdit()
                )
            }
            transition<OnTagRenameInputted> {
                current.copy(
                    interaction = dependency
                        .tagEditDelegate
                        .changeRenameText(current.requireTagEditInteraction(), action.text)
                        .toTagEdit()
                )
            }
            effect<OnTagRenameConfirmClicked> {
                dependency.tagEditDelegate
                    .tryUpdateTagName(current.requireTagEditInteraction(), loadedTagsSnapshot = current.tags)
                    .map(LazyTagEditResult::toAction)
                    .collect(this::dispatch)
            }
            effect<OnTagReplaceConfirmClicked> {
                dependency.tagEditDelegate
                    .mergeTag(current.requireTagEditInteraction())
                    .map(LazyTagEditResult::toAction)
                    .collect(this::dispatch)
            }
            transition<OnTagDeleteRequestClicked> {
                current.copy(
                    interaction = dependency
                        .tagEditDelegate
                        .startDelete(current.requireTagEditInteraction())
                        .toTagEdit()
                )
            }
            effect<OnTagDeleteConfirmClicked> {
                dependency.tagEditDelegate
                    .deleteTag(current.requireTagEditInteraction())
                    .map(LazyTagEditResult::toAction)
                    .collect(this::dispatch)
            }
        }
        transition<UserMessagePosted> { current.copy(userMessages = current.userMessages + action.message) }
        transition<UserMessageShown> { current.copy(userMessages = current.userMessages - action.message) }
        transition<Interacted> { current.copy(interaction = HomeInteraction.Empty) }
    }
}

private val Success.tagEditStep: TagEditStep?
    get() = (interaction as? HomeInteraction.TagEdit)?.tagEditStep

private fun Success.requireTagEditInteraction(): TagEditStep = checkNotNull(tagEditStep)

private fun TagEditStep.toTagEdit(): HomeInteraction.TagEdit =
    HomeInteraction.TagEdit(this)

private fun LazyTagEditResult.toAction(): HomeAction = when (this) {
    is LazyTagEditResult.ToNextStep -> {
        TagEditChangedLazily(toNextStepInteraction = this)
    }
    is LazyTagEditResult.UnknownError -> {
        UserMessagePosted(UserMessage(StringIds.unknown_error))
    }
}