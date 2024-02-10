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
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTagUsageCount
import com.nlab.reminder.core.data.model.genTags
import com.nlab.statekit.expectedState
import com.nlab.statekit.expectedStateToInit
import com.nlab.statekit.scenario
import com.nlab.testkit.genBothify
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
import kotlinx.collections.immutable.*
import org.junit.Test

/**
 * @author Doohyun
 */
internal class HomeReducerTest {
    @Test
    fun testCompleteWorkflow() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = genHomeWorkflowExcludeEmpty()))
            .action(HomeAction.CompleteWorkflow)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { it.copy(workflow = HomeWorkflow.Empty) }
            .verify()
    }

    @Test
    fun testUserMessageShown() {
        val shownMessage = UserMessage(genInt())
        val expectedState = genHomeUiStateSuccess(userMessages = emptyList())

        HomeReducer().scenario()
            .initState(expectedState.copy(userMessages = persistentListOf(shownMessage)))
            .action(HomeAction.UserMessageShown(shownMessage))
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun testErrorOccurred() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(userMessages = emptyList()))
            .action(HomeAction.ErrorOccurred(Throwable()))
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(userMessages = persistentListOf(UserMessage(R.string.unknown_error)))
            }
            .verify()
    }

    @Test
    fun `Workflow not changed, when workflow exists`() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = genHomeWorkflowExcludeEmpty()))
            .action(HomeAction.OnTodayCategoryClicked)
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Today's schedule was shown, when today category clicked`() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = HomeWorkflow.Empty))
            .action(HomeAction.OnTodayCategoryClicked)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { it.copy(workflow = HomeWorkflow.TodaySchedule) }
            .verify()
    }

    @Test
    fun `Timetable's schedule was shown, when timetable category clicked`() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = HomeWorkflow.Empty))
            .action(HomeAction.OnTimetableCategoryClicked)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { it.copy(workflow = HomeWorkflow.TimetableSchedule) }
            .verify()
    }

    @Test
    fun `All's schedule was shown, when all category clicked`() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = HomeWorkflow.Empty))
            .action(HomeAction.OnAllCategoryClicked)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { it.copy(workflow = HomeWorkflow.AllSchedule) }
            .verify()
    }

    @Test
    fun `Fetched, when summary loaded`() {
        val expectedState = genHomeUiStateSuccess(
            todayScheduleCount = genLong(min = 1),
            timetableScheduleCount = genLong(min = 1),
            allScheduleCount = genLong(min = 1),
            tags = genTags(count = genInt(min = 10)),
            workflow = HomeWorkflow.Empty
        )

        HomeReducer().scenario()
            .initState(HomeUiState.Loading)
            .action(expectedState.toSummaryLoadedAction())
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `State was changed, when summary loaded`() {
        val expectedState = genHomeUiStateSuccess(
            todayScheduleCount = genLong(min = 1),
            timetableScheduleCount = genLong(min = 1),
            allScheduleCount = genLong(min = 1),
            tags = genTags(count = genInt(min = 10))
        )

        HomeReducer().scenario()
            .initState(
                expectedState.copy(
                    todayScheduleCount = 0,
                    timetableScheduleCount = 0,
                    allScheduleCount = 0,
                    tags = persistentListOf()
                )
            )
            .action(expectedState.toSummaryLoadedAction())
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `Tag config workflow set, when tag metadata loaded`() {
        val tag = genTag()
        val usageCount = genTagUsageCount()

        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(tags = listOf(tag), workflow = HomeWorkflow.Empty))
            .action(HomeAction.TagConfigMetadataLoaded(tag, usageCount))
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(workflow = HomeWorkflow.TagConfig(tag, usageCount))
            }
            .verify()
    }

    @Test
    fun `Show not exists tag message, when no tag used for TagConfig`() {
        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    tags = emptyList(),
                    userMessages = emptyList(),
                    workflow = HomeWorkflow.Empty
                )
            )
            .action(HomeAction.TagConfigMetadataLoaded(genTag(), genTagUsageCount()))
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(userMessages = persistentListOf(UserMessage(R.string.tag_not_exist)))
            }
            .verify()
    }

    @Test
    fun `Tag rename workflow set, when tag rename request clicked`() {
        val tag = genTag()
        val usageCount = genTagUsageCount()

        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    tags = emptyList(),
                    workflow = HomeWorkflow.TagConfig(tag, usageCount)
                )
            )
            .action(HomeAction.OnTagRenameRequestClicked)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(
                    workflow = HomeWorkflow.TagRename(
                        tag = tag,
                        usageCount = usageCount,
                        renameText = tag.name,
                        shouldKeyboardShown = true
                    )
                )
            }
            .verify()
    }

    @Test
    fun `Nothing happened, when tag rename request clicked, but tagConfig workflow not set`() {
        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagConfig::class))
                )
            )
            .action(HomeAction.OnTagRenameRequestClicked)
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Keyboard shown by Tag rename input box`() {
        val tagRenameWorkflow = genHomeTagRenameWorkflow(shouldKeyboardShown = true)

        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = tagRenameWorkflow))
            .action(HomeAction.OnTagRenameInputKeyboardShown)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(workflow = tagRenameWorkflow.copy(shouldKeyboardShown = false))
            }
            .verify()
    }

    @Test
    fun `Nothing happened, when keyboard shown, but tagRename workflow not set`() {
        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagRename::class))
                )
            )
            .action(HomeAction.OnTagRenameInputKeyboardShown)
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Tag rename inputted`() {
        val input = genBothify("rename-????")
        val tagRename = genHomeTagRenameWorkflow(renameText = "")

        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(workflow = tagRename))
            .action(HomeAction.OnTagRenameInputted(input))
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(workflow = tagRename.copy(renameText = input))
            }
            .verify()
    }

    @Test
    fun `Nothing happened, when tag rename text inputted, but tagRename workflow not set`() {
        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagRename::class))
                )
            )
            .action(HomeAction.OnTagRenameInputKeyboardShown)
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Tag delete workflow set, when Tag delete request was Clicked`() {
        val tag = genTag()
        val usageCount = genTagUsageCount()

        HomeReducer().scenario()
            .initState(
                genHomeUiStateSuccess(
                    tags = emptyList(),
                    workflow = HomeWorkflow.TagConfig(tag, usageCount)
                )
            )
            .action(HomeAction.OnTagDeleteRequestClicked)
            .expectedStateFromInitTypeOf<HomeUiState.Success> { initState ->
                initState.copy(workflow = HomeWorkflow.TagDelete(tag = tag, usageCount = usageCount))
            }
            .verify()
    }

    @Test
    fun `Nothing happened, when tag delete request clicked, but tagConfig workflow not set`() {
        HomeReducer().scenario()
            .initState(genHomeUiStateSuccess(
                workflow = genHomeWorkflowExcludeEmpty(ignoreCases = setOf(HomeWorkflow.TagConfig::class))
            ))
            .action(HomeAction.OnTagDeleteRequestClicked)
            .expectedStateToInit()
            .verify()
    }
}

private fun HomeUiState.Success.toSummaryLoadedAction() = HomeAction.SummaryLoaded(
    todaySchedulesCount = todayScheduleCount,
    timetableSchedulesCount = timetableScheduleCount,
    allSchedulesCount = allScheduleCount,
    tags = tags
)