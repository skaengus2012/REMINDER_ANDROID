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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.schedule.model.genScheduleElements
import com.nlab.reminder.core.schedule.model.mapToScheduleElementsAsImmutableList
import com.nlab.statekit.expectedState
import com.nlab.statekit.expectedStateToInit
import com.nlab.statekit.scenario
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

/**
 * @author thalys
 */
internal class AllScheduleReducerTest {
    @Test
    fun `Load schedules, when empty`() {
        val expectedState = genAllScheduleUiStateLoaded(
            scheduleElements = genScheduleElements().toImmutableList(),
            isSelectionMode = false,
            workflows = persistentListOf()
        )

        AllScheduleReducer().scenario()
            .initState(AllScheduleUiState.Empty)
            .action(expectedState.toLoadedAction())
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `Load schedule, when after loaded`() {
        val expectedState = genAllScheduleUiStateLoaded()
        AllScheduleReducer().scenario()
            .initState(
                expectedState.copy(
                    scheduleElements = persistentListOf(),
                    isCompletedScheduleShown = expectedState.isCompletedScheduleShown.not()
                )
            )
            .action(expectedState.toLoadedAction())
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `Update selection mode, when selectionMode enabled clicked`() {
        val expectedSelectionMode: Boolean = genBoolean()

        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(isSelectionMode = expectedSelectionMode.not()))
            .action(AllScheduleAction.OnSelectionModeUpdateClicked(expectedSelectionMode))
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { it.copy(isSelectionMode = expectedSelectionMode) }
            .verify()
    }

    @Test
    fun `Given schedule, When OnScheduleLinkClicked, Then link workflow added`() {
        val link = genLink()
        val schedule = genSchedule(link = link)
        val clickedPosition = 0

        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = schedule.mapToScheduleElementsAsImmutableList()))
            .action(AllScheduleAction.OnScheduleLinkClicked(clickedPosition))
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { initState ->
                initState.copy(workflows = persistentListOf(AllScheduleWorkflow.LinkPage(link)))
            }
            .verify()
    }

    @Test
    fun `Given schedule with empty link, When OnScheduleLinkClicked, Then state not changed`() {
        val schedule = genSchedule(link = Link.EMPTY)
        val clickedPosition = 0
        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = schedule.mapToScheduleElementsAsImmutableList()))
            .action(AllScheduleAction.OnScheduleLinkClicked(clickedPosition))
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Given empty schedule, When OnScheduleLinkClicked, Then state not changed`() {
        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(scheduleElements = persistentListOf()))
            .action(AllScheduleAction.OnScheduleLinkClicked(genInt()))
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Given same workflow in container, When complete workflow, Then state removed first once`() {
        val workflow = genAllScheduleWorkflowsExcludeEmpty().first()

        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(workflows = persistentListOf(workflow, workflow)))
            .action(AllScheduleAction.CompleteWorkflow(workflow))
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { initState ->
                initState.copy(workflows = persistentListOf(workflow))
            }
            .verify()
    }
}

private fun AllScheduleUiState.Loaded.toLoadedAction(): AllScheduleAction.ScheduleElementsLoaded =
    AllScheduleAction.ScheduleElementsLoaded(
        scheduleElements = scheduleElements,
        isCompletedScheduleShown = isCompletedScheduleShown
    )