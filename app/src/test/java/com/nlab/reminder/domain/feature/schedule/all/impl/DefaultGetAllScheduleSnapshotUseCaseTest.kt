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

package com.nlab.reminder.domain.feature.schedule.all.impl

import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import com.nlab.reminder.domain.feature.schedule.all.genAllScheduleSnapshot
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGetAllScheduleSnapshotUseCaseTest {
    @Test
    fun `find all schedules when doneScheduleShown was true`() = runTest {
        testFindTemplate(
            isDoneScheduleShown = true,
            setupMock = { scheduleRepository, expectSchedules ->
                whenever(scheduleRepository.get(GetScheduleRequest.All)) doReturn flowOf(expectSchedules)
            }
        )
    }

    @Test
    fun `find not complete schedules when doneScheduleShown was false`() = runTest {
        testFindTemplate(
            isDoneScheduleShown = false,
            setupMock = { scheduleRepository, expectSchedules ->
                whenever(
                    scheduleRepository.get(GetScheduleRequest.ByComplete(isComplete = false))
                ) doReturn flowOf(expectSchedules)
            }
        )
    }

    private suspend fun testFindTemplate(
        isDoneScheduleShown: Boolean,
        setupMock: (ScheduleRepository, schedules: List<Schedule>) -> Unit,
    ) {
        val expectSchedules: List<Schedule> = genSchedules()
        val fakeCompleteMark: Boolean = genBoolean()
        val scheduleRepository: ScheduleRepository = mock { setupMock(mock, expectSchedules) }
        val getAllScheduleSnapshotUseCase: GetAllScheduleSnapshotUseCase = DefaultGetAllScheduleSnapshotUseCase(
            scheduleRepository = scheduleRepository,
            scheduleUiStateFlowFactory = object : ScheduleUiStateFlowFactory {
                override fun with(schedules: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> =
                    schedules.map { genScheduleUiStates(it, isCompleteMarked = fakeCompleteMark) }
            },
            completedScheduleShownRepository = mock { whenever(mock.get()) doReturn flowOf(isDoneScheduleShown) },
        )
        val snapshot: AllScheduleSnapshot =
            getAllScheduleSnapshotUseCase().take(1).first()
        assertThat(
            snapshot,
            equalTo(
                genAllScheduleSnapshot(
                    uiStates = genScheduleUiStates(expectSchedules, isCompleteMarked = fakeCompleteMark),
                    isCompletedScheduleShown = isDoneScheduleShown
                )
            )
        )
    }
}