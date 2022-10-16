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

package com.nlab.reminder.domain.common.schedule.impl

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.GetAllScheduleSnapshotUseCase
import com.nlab.reminder.domain.feature.schedule.all.genAllScheduleReport
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGetAllScheduleSnapshotUseCaseTest {
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(genFlowExecutionDispatcher(testScheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `find all schedules when doneScheduleShown was true`() = runTest {
        val isDoneScheduleShown = true // TODO implements with doneScheduleShownRepository
        val expectSchedules: List<Schedule> = genSchedules()
        val pagingConfig = PagingConfig(pageSize = expectSchedules.size)
        val fakeCompleteMark: Boolean = genBoolean()
        val fakeScheduleUiStatePagingFlowFactory = object : ScheduleUiStatePagingFlowFactory {
            override fun with(schedules: Flow<PagingData<Schedule>>): Flow<PagingData<ScheduleUiState>> =
                schedules.map { genPagingScheduleUiStates(it, fakeCompleteMark) }
        }
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.getAsPagingData(ScheduleItemRequest.Find, pagingConfig)) doReturn flowOf(
                PagingData.from(expectSchedules)
            )
        }
        val getAllScheduleSnapshotUseCase: GetAllScheduleSnapshotUseCase = DefaultGetAllScheduleSnapshotUseCase(
            coroutineScope = CoroutineScope(genFlowExecutionDispatcher(testScheduler)),
            pagingConfig = pagingConfig,
            scheduleRepository = scheduleRepository,
            scheduleUiStatePagingFlowFactory = fakeScheduleUiStatePagingFlowFactory
        )
        val differ = AsyncPagingDataDiffer(
            diffCallback = IdentityItemCallback<ScheduleUiState>(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )
        val snapshot: AllScheduleSnapshot =
            getAllScheduleSnapshotUseCase().take(1).first()
        differ.submitData(snapshot.pagingScheduled)

        advanceUntilIdle()
        assertThat(
            snapshot.copy(pagingScheduled = PagingData.empty()),
            equalTo(
                genAllScheduleReport(
                    emptyList(), // TODO remove
                    isDoneScheduleShown = isDoneScheduleShown,
                    pagingScheduled = PagingData.empty()
                )
            )
        )
        assertThat(
            differ.snapshot().items,
            equalTo(genScheduleUiStates(expectSchedules, fakeCompleteMark),)
        )
    }
}