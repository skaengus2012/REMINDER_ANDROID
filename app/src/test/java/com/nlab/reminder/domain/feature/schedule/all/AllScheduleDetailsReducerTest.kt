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



/**
 * @author thalys
 */
internal class AllScheduleDetailsReducerTest {
    /**
    @Test
    fun `Load schedules, when empty`() {
        val expectedState = genAllScheduleUiStateLoaded(
            scheduleElements = genScheduleElements().toImmutableList(),
            isSelectionMode = false,
            isSelectedActionInvoked = false,
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
    fun `Update selection mode, when selectionMode toggle clicked`() {
        fun testTemplate(expectedSelectionMode: Boolean) {
            AllScheduleReducer().scenario()
                .initState(
                    genAllScheduleUiStateLoaded(
                        isSelectionMode = expectedSelectionMode.not(),
                        isSelectedActionInvoked = expectedSelectionMode
                    )
                )
                .action(AllScheduleAction.OnSelectionModeToggleClicked)
                .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { initState ->
                    initState.copy(
                        isSelectionMode = expectedSelectionMode,
                        isSelectedActionInvoked = expectedSelectionMode.not()
                    )
                }
                .verify()
        }

        testTemplate(expectedSelectionMode = true)
        testTemplate(expectedSelectionMode = false)
    }

    @Test
    fun `When selectedAction invoked, Then selection mode changed to false`() {
        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(isSelectionMode = true, isSelectedActionInvoked = false))
            .action(genAllScheduleSelectedAction())
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { it.copy(isSelectionMode = false, isSelectedActionInvoked = true) }
            .verify()
    }

    @Test
    fun `When appliedSelectedActionWithSchedules, Then isSelectedActionInvoked changed to false`() = runTest {
        AllScheduleReducer().scenario()
            .initState(genAllScheduleUiStateLoaded(isSelectedActionInvoked = true))
            .action(AllScheduleAction.AppliedSelectedActionWithSchedules)
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { it.copy(isSelectedActionInvoked = false) }
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
    */
}

/**
private fun AllScheduleUiState.Loaded.toLoadedAction(): AllScheduleAction.ScheduleElementsLoaded =
    AllScheduleAction.ScheduleElementsLoaded(
        scheduleElements = scheduleElements,
        isCompletedScheduleShown = isCompletedScheduleShown
    )*/