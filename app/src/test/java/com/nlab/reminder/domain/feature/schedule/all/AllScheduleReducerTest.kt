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

import com.nlab.statekit.test.expectedState
import com.nlab.statekit.test.tester
import com.nlab.testkit.genBoolean
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

/**
 * @author thalys
 */
internal class AllScheduleReducerTest {
    @Test
    fun `Load schedules, when idle`() {
        val expectedState = genAllScheduleUiStateLoaded(
            isSelectionMode = false
        )

        AllScheduleReducer().tester()
            .dispatchAction(expectedState.toLoadedAction())
            .initState(AllScheduleUiState.Idle)
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `Load schedule, when after loaded`() {
        val expectedState = genAllScheduleUiStateLoaded()

        AllScheduleReducer().tester()
            .dispatchAction(expectedState.toLoadedAction())
            .initState(
                expectedState.copy(
                    schedules = persistentListOf(),
                    isCompletedScheduleShown = expectedState.isCompletedScheduleShown.not()
                )
            )
            .expectedState(expectedState)
            .verify()
    }

    @Test
    fun `Update selection mode, when selectionMode enabled clicked`() {
        val expectedSelectionMode: Boolean = genBoolean()

        AllScheduleReducer().tester()
            .dispatchAction(AllScheduleAction.OnSelectionModeUpdateClicked(expectedSelectionMode))
            .initState(genAllScheduleUiStateLoaded(isSelectionMode = expectedSelectionMode.not()))
            .expectedStateFromInitTypeOf<AllScheduleUiState.Loaded> { it.copy(isSelectionMode = expectedSelectionMode) }
            .verify()
    }
}

private fun AllScheduleUiState.Loaded.toLoadedAction(): AllScheduleAction.ScheduleLoaded =
    AllScheduleAction.ScheduleLoaded(
        schedules = schedules,
        isCompletedScheduleShown = isCompletedScheduleShown
    )