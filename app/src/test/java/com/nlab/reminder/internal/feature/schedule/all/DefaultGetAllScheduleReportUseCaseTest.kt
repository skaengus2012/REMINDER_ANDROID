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

package com.nlab.reminder.internal.feature.schedule.all

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleReportUseCase
import com.nlab.reminder.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGetAllScheduleReportUseCaseTest {
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(genFlowExecutionDispatcher(testScheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `flow data bring to scheduleRepository`() = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val pagingConfig = PagingConfig(pageSize = genInt())
        val doingSchedules = genSchedules(isComplete = false)
        val doneSchedules = genSchedules(isComplete = true)
        val scheduleRepository: ScheduleRepository = mock {
            whenever(
                mock.get(ScheduleItemRequest.FindByComplete(isComplete = false))
            ) doReturn flow { emit(doingSchedules) }

            whenever(
                mock.getAsPagingData(
                    ScheduleItemPagingRequest.FindByComplete(isComplete = true),
                    pagingConfig
                )
            ) doReturn flow { emit(PagingData.from(doneSchedules)) }
        }
        val getAllScheduleReport: GetAllScheduleReportUseCase = DefaultGetAllScheduleReportUseCase(
            scheduleRepository,
            pagingConfig,
            executeDispatcher
        )
        var actualDoingSchedules: List<Schedule> = emptyList()
        val actualDoneSchedulesDiffer = AsyncPagingDataDiffer(
            diffCallback = IdentityItemCallback<Schedule>(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )

        getAllScheduleReport(coroutineScope = this)
            .onEach { report ->
                actualDoingSchedules = report.doingSchedules
                actualDoneSchedulesDiffer.submitData(report.doneSchedules)
            }
            .launchIn(genFlowObserveDispatcher())

        advanceUntilIdle()
        assertThat(actualDoingSchedules, equalTo(doingSchedules))
        assertThat(actualDoneSchedulesDiffer.snapshot().items, equalTo(doneSchedules))
    }
}