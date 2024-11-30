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

import com.nlab.reminder.core.kotlin.getOrElse
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.core.uistate.UserMessage
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
    stateScope<Success> {
        scope(isMatch = { current.interaction != HomeInteraction.Empty }) {
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
                val nextAction = dependency.tagRepository
                    .getUsageCount(id = action.tag.id)
                    .map { TagEditMetadataLoaded(action.tag, it) }
                    .getOrElse { PostMessage(UserMessage(StringIds.tag_not_found)) }
                dispatch(nextAction)
            }
            transition<TagEditMetadataLoaded> {
                current.copy()
            }
        }
        /**
        scope(isMatch = { current.interaction is HomeInteraction.TagEdit }) {
            fun Success.requireTagConfig() = interaction as HomeInteraction.TagEdit
            transition<OnTagRenameRequestClicked> {
                current.copy(
                    interaction = current.requireTagConfig().let { tagConfig ->
                        HomeInteraction.TagRename(
                            tag = tagConfig.tag,
                            usageCount = tagConfig.usageCount,
                            renameText = tagConfig.tag.name.value,
                            shouldUserInputReady = true
                        )
                    }
                )
            }
            transition<OnTagDeleteRequestClicked> {
                current.copy(
                    interaction = current.requireTagConfig().let { tagConfig ->
                        HomeInteraction.TagDelete(tag = tagConfig.tag, usageCount = tagConfig.usageCount)
                    }
                )
            }
        }
        scope(isMatch = { current.interaction is HomeInteraction.TagRename }) {
            fun Success.requireTagRename() = interaction as HomeInteraction.TagRename
            transition<OnTagRenameInputReady> {
                current.copy(interaction = current.requireTagRename().copy(shouldUserInputReady = false))
            }
            transition<OnTagRenameInputted> {
                current.copy(interaction = current.requireTagRename().copy(renameText = action.text))
            }
            effect<OnTagRenameConfirmClicked> {
                val tagRename = current.requireTagRename()
                val newName = tagRename.renameText.tryToNonBlankStringOrNull() ?: return@effect
                val result = dependency.tryUpdateTagName(
                    tagId = tagRename.tag.id,
                    newName = newName,
                    tagGroup = TagGroupSource.Snapshot(current.tags)
                )
                // TODO 더 구현..
            }
        }
        effect<OnTagDeleteConfirmClicked> {
            val tagDelete = current.interaction as? HomeInteraction.TagDelete ?: return@effect
            // TODO 구현
        }*/
        transition<PostMessage> { current.copy(userMessages = current.userMessages + action.message) }
        transition<ShownMessage> { current.copy(userMessages = current.userMessages - action.message) }
        transition<Interacted> { current.copy(interaction = HomeInteraction.Empty) }
    }
}