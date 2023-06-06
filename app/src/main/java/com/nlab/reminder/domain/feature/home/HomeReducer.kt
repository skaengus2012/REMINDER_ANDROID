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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.R
import com.nlab.reminder.core.state.UserMessage
import com.nlab.reminder.core.state.userMessageShown
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.util.test.annotation.TestComplete
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.statekit.Reducer
import com.nlab.statekit.util.buildDslReducer
import kotlinx.collections.immutable.*
import javax.inject.Inject

private typealias DomainReducer = Reducer<HomeAction, HomeUiState>

/**
 * @author Doohyun
 */
internal class HomeReducer @Inject constructor() : DomainReducer by buildDslReducer(defineDSL = {
    state<HomeUiState.Success> {
        action<HomeAction.PageShown> { (_, before) -> before.withPageShown() }
        action<HomeAction.UserMessageShown> { (action, before) ->
            before.copy(
                userMessages = before
                    .userMessages
                    .userMessageShown(action.shownMessage)
            )
        }
        action<HomeAction.OnTodayCategoryClicked> { (_, before) ->
            before.withPageShown(showTodaySchedule = true)
        }
        action<HomeAction.OnTimetableCategoryClicked> { (_, before) ->
            before.withPageShown(showTimetableSchedule = true)
        }
        action<HomeAction.OnAllCategoryClicked> { (_, before) ->
            before.withPageShown(showAllSchedule = true)
        }
        action<HomeAction.TagConfigMetadataLoaded> { (action, before) ->
            before.updateIfTagExists(
                target = action.tag,
                getUiState = {
                    before.withPageShown(
                        tagConfigTarget = TagConfig(action.tag, action.usageCount)
                    )
                }
            )
        }
        action<HomeAction.TagRenameMetadataLoaded> { (action, before) ->
            before.updateIfTagExists(
                target = action.tag,
                getUiState = {
                    before.withPageShown(
                        tagRenameTarget = TagRenameConfig(
                            action.tag,
                            action.usageCount,
                            renameText = ""
                        )
                    )
                }
            )
        }
        action<HomeAction.OnTagRenameInputted> { (action, before) ->
            before.copy(tagRenameTarget = before.tagRenameTarget?.copy(renameText = action.text))
        }
        action<HomeAction.TagDeleteMetadataLoaded> { (action, before) ->
            before.updateIfTagExists(
                target = action.tag,
                getUiState = {
                    before.withPageShown(
                        tagDeleteTarget = TagDeleteConfig(action.tag, action.usageCount)
                    )
                }
            )
        }
    }

    action<HomeAction.SummaryLoaded> {
        state<HomeUiState.Loading> { (action) ->
            HomeUiState.Success(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.tags.toImmutableList()
            )
        }
        state<HomeUiState.Success> { (action, before) ->
            before.copy(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.tags.toImmutableList()
            )
        }
    }
})

private fun HomeUiState.Success.withPageShown(
    showTodaySchedule: Boolean = false,
    showTimetableSchedule: Boolean = false,
    showAllSchedule: Boolean = false,
    tagConfigTarget: TagConfig? = null,
    tagRenameTarget: TagRenameConfig? = null,
    tagDeleteTarget: TagDeleteConfig? = null
): HomeUiState.Success =
    copy(
        todayScheduleShown = showTodaySchedule,
        timetableScheduleShown = showTimetableSchedule,
        allScheduleShown = showAllSchedule,
        tagConfigTarget = tagConfigTarget,
        tagRenameTarget = tagRenameTarget,
        tagDeleteTarget = tagDeleteTarget
    )

@TestComplete
@ExcludeFromGeneratedTestReport
private inline fun HomeUiState.Success.updateIfTagExists(
    target: Tag,
    getUiState: () -> HomeUiState.Success
): HomeUiState.Success =
    if (target in tags) getUiState()
    else copy(
        tagConfigTarget = null,
        tagRenameTarget = null,
        userMessages = userMessages + UserMessage(R.string.tag_not_exist)
    )