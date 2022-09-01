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
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleReport
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleReportUseCase
import com.nlab.reminder.domain.feature.schedule.all.genAllScheduleReport
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
class DefaultGetAllScheduleReportUseCaseTest {
    @Test
    fun `find all schedules when doneScheduleShown was true`() {
        testSchedulesRequest(
            expectSchedules = genSchedules(),
            scheduleItemRequest = ScheduleItemRequest.Find,
            isDoneScheduleShown = true
        )
    }

    @Test
    fun `find doing schedules when doneScheduleShown was false`() {
        testSchedulesRequest(
            expectSchedules = genSchedules(isComplete = false),
            scheduleItemRequest = ScheduleItemRequest.FindByComplete(isComplete = false),
            isDoneScheduleShown = false
        )
    }

    private fun testSchedulesRequest(
        expectSchedules: List<Schedule>,
        scheduleItemRequest: ScheduleItemRequest,
        isDoneScheduleShown: Boolean
    ) {
        val fakeCompleteMark: Boolean = genBoolean()
        val fakeScheduleUiStateFlowFactory: ScheduleUiStateFlowFactory = object : ScheduleUiStateFlowFactory {
            override fun combineWith(scheduleFlow: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> {
                return scheduleFlow.map { schedules -> genScheduleUiStates(schedules, fakeCompleteMark) }
            }
        }
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.get(scheduleItemRequest)) doReturn flowOf(expectSchedules)
        }
        val getAllScheduleReport: GetAllScheduleReportUseCase = DefaultGetAllScheduleReportUseCase(
            doneScheduleShownRepository = mock { whenever(mock.get()) doReturn flowOf(isDoneScheduleShown) },
            scheduleRepository = scheduleRepository,
            scheduleUiStateFlowFactory = fakeScheduleUiStateFlowFactory,
            dispatcher = Dispatchers.Unconfined
        )
        val actualReports = mutableListOf<AllScheduleReport>()
        getAllScheduleReport()
            .onEach(actualReports::add)
            .launchIn(genFlowObserveDispatcher())
        assertThat(
            actualReports,
            equalTo(
                listOf(
                    genAllScheduleReport(
                        genScheduleUiStates(expectSchedules, fakeCompleteMark),
                        isDoneScheduleShown
                    )
                )
            )
        )
    }
}