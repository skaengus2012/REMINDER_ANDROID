/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

import com.nlab.reminder.core.state.StateContainer
import com.nlab.reminder.core.state.verifyStateSendExtension
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
class AllScheduleViewModelsKtTest {
    @Test
    fun testExtensions() {
        val stateContainer: StateContainer<AllScheduleEvent, AllScheduleState> = mock()
        val schedule: Schedule = genSchedule()
        val scheduleUiStates: List<ScheduleUiState> = genScheduleUiStates()
        val isComplete: Boolean = genBoolean()
        val viewModel = AllScheduleViewModel(
            stateContainerFactory = mock {
                whenever(mock.create(any(), any())) doReturn stateContainer
            }
        )

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnToggleCompletedScheduleShownClicked
        ) { viewModel.onToggleCompletedScheduleShownClicked() }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnToggleSelectionModeEnableClicked
        ) { viewModel.onToggleSelectionModeEnableClicked() }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnToggleCompletedScheduleShownClicked
        ) { viewModel.onDeleteCompletedScheduleClicked() }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnModifyScheduleCompleteClicked(schedule.id, isComplete)
        ) { viewModel.onModifyScheduleCompleteClicked(schedule.id, isComplete) }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnDragScheduleEnded(scheduleUiStates)
        ) { viewModel.onDragScheduleEnded(scheduleUiStates) }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnDeleteScheduleClicked(schedule.id)
        ) { viewModel.onDeleteScheduleClicked(schedule.id) }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnScheduleLinkClicked(schedule.id)
        ) { viewModel.onScheduleLinkClicked(schedule.id) }

        verifyStateSendExtension(
            stateContainer,
            AllScheduleEvent.OnScheduleSelectionClicked(schedule.id)
        ) { viewModel.onScheduleSelectionClicked(schedule.id) }
    }
}