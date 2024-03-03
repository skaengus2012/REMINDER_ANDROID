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
import com.nlab.reminder.core.annotation.test.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.annotation.test.TestCompleted
import com.nlab.reminder.core.state.UserMessage
import com.nlab.reminder.core.state.userMessageShown
import com.nlab.reminder.core.data.model.Tag
import com.nlab.statekit.Reducer
import com.nlab.statekit.util.buildDslReducer
import kotlinx.collections.immutable.*
import javax.inject.Inject
import kotlin.reflect.cast

private typealias DomainReducer = Reducer<HomeAction, HomeUiState>

/**
 * @author Doohyun
 */
internal class HomeReducer @Inject constructor() : DomainReducer by buildDslReducer(defineDSL = {
    state<HomeUiState.Success> {
        action<HomeAction.CompleteWorkflow> { (_, before) -> before.copy(workflow = HomeWorkflow.Empty) }
        action<HomeAction.UserMessageShown> { (action, before) ->
            before.copy(
                userMessages = before
                    .userMessages
                    .userMessageShown(action.shownMessage)
            )
        }
        action<HomeAction.ErrorOccurred> { (_, before) ->
            before.copy(userMessages = before.userMessages + UserMessage(R.string.unknown_error))
        }
        action<HomeAction.OnTodayCategoryClicked> { (_, before) ->
            before.mapIfWorkflowEmpty { it.copy(workflow = HomeWorkflow.TodaySchedule) }
        }
        action<HomeAction.OnTimetableCategoryClicked> { (_, before) ->
            before.mapIfWorkflowEmpty { it.copy(workflow = HomeWorkflow.TimetableSchedule) }
        }
        action<HomeAction.OnAllCategoryClicked> { (_, before) ->
            before.mapIfWorkflowEmpty { it.copy(workflow = HomeWorkflow.AllSchedule) }
        }
        action<HomeAction.TagConfigMetadataLoaded> { (action, before) ->
            before.mapIfWorkflowEmpty { cur ->
                cur.mapIfTagExists(action.tag) {
                    it.copy(workflow = HomeWorkflow.TagConfig(action.tag, action.usageCount))
                }
            }
        }
        action<HomeAction.OnTagRenameRequestClicked> { (_, before) ->
            before.mapIfWorkflowMatches<HomeWorkflow.TagConfig> { old, workflow ->
                old.copy(
                    workflow = HomeWorkflow.TagRename(
                        workflow.tag,
                        workflow.usageCount,
                        renameText = workflow.tag.name,
                        shouldKeyboardShown = true
                    )
                )
            }
        }
        action<HomeAction.OnTagRenameInputKeyboardShown> { (_, before) ->
            before.mapIfWorkflowMatches<HomeWorkflow.TagRename> { cur, workflow ->
                cur.copy(workflow = workflow.copy(shouldKeyboardShown = false))
            }
        }
        action<HomeAction.OnTagRenameInputted> { (action, before) ->
            before.mapIfWorkflowMatches<HomeWorkflow.TagRename> { cur, tagRenameWorkflow ->
                cur.copy(workflow = tagRenameWorkflow.copy(renameText = action.text))
            }
        }
        action<HomeAction.OnTagDeleteRequestClicked> { (_, before) ->
            before.mapIfWorkflowMatches<HomeWorkflow.TagConfig> { old, workflow ->
                old.copy(workflow = HomeWorkflow.TagDelete(workflow.tag, workflow.usageCount))
            }
        }
    }

    action<HomeAction.SummaryLoaded> {
        state<HomeUiState.Loading> { (action) ->
            HomeUiState.Success(
                todayScheduleCount = action.todaySchedulesCount,
                timetableScheduleCount = action.timetableSchedulesCount,
                allScheduleCount = action.allSchedulesCount,
                tags = action.tags.toImmutableList(),
                workflow = HomeWorkflow.Empty,
                userMessages = persistentListOf()
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

@ExcludeFromGeneratedTestReport
@TestCompleted
private inline fun HomeUiState.Success.mapIfWorkflowEmpty(
    transform: (HomeUiState.Success) -> HomeUiState
): HomeUiState = if (workflow is HomeWorkflow.Empty) transform(this) else this

@ExcludeFromGeneratedTestReport
@TestCompleted
private inline fun HomeUiState.Success.mapIfTagExists(
    target: Tag,
    transform: (HomeUiState.Success) -> HomeUiState
): HomeUiState =
    if (target in tags) transform(this)
    else copy(userMessages = userMessages + UserMessage(R.string.tag_not_exist))

@ExcludeFromGeneratedTestReport
@TestCompleted
private inline fun <reified T : HomeWorkflow> HomeUiState.Success.mapIfWorkflowMatches(
    transform: (old: HomeUiState.Success, workflow: T) -> HomeUiState
): HomeUiState {
    val clazz = T::class
    return if (clazz.isInstance(workflow)) transform(this, clazz.cast(workflow)) else this
}