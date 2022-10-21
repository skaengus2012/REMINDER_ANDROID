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

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
        testFindTemplate(
            isDoneScheduleShown = true,
            setupMock = { scheduleRepository, pagingConfig, expectSchedules ->
                whenever(
                    scheduleRepository
                        .getAsPagingData(ScheduleItemRequest.Find, pagingConfig)
                ) doReturn flowOf(PagingData.from(expectSchedules))

                whenever(
                    scheduleRepository
                        .getAsPagingData(ScheduleItemRequest.FindByComplete(isComplete = false), pagingConfig)
                ) doReturn emptyFlow()
            }
        )
    }

    @Test
    fun `find not complete schedules when doneScheduleShown was false`() = runTest {
        testFindTemplate(
            isDoneScheduleShown = false,
            setupMock = { scheduleRepository, pagingConfig, expectSchedules ->
                whenever(
                    scheduleRepository
                        .getAsPagingData(ScheduleItemRequest.Find, pagingConfig)
                ) doReturn emptyFlow()

                whenever(
                    scheduleRepository
                        .getAsPagingData(ScheduleItemRequest.FindByComplete(isComplete = false), pagingConfig)
                ) doReturn flowOf(PagingData.from(expectSchedules))
            }
        )
    }

    private suspend fun TestScope.testFindTemplate(
        isDoneScheduleShown: Boolean,
        setupMock: (ScheduleRepository, PagingConfig, schedules: List<Schedule>) -> Unit,
    ) {
        val expectSchedules: List<Schedule> = genSchedules()
        val pagingConfig = PagingConfig(pageSize = expectSchedules.size)
        val fakeCompleteMark: Boolean = genBoolean()
        val fakeScheduleUiStatePagingFlowFactory = object : ScheduleUiStatePagingFlowFactory {
            override fun with(schedules: Flow<PagingData<Schedule>>): Flow<PagingData<ScheduleUiState>> =
                schedules.map { genPagingScheduleUiStates(it, fakeCompleteMark) }
        }
        val scheduleRepository: ScheduleRepository = mock { setupMock(mock, pagingConfig, expectSchedules) }
        val getAllScheduleSnapshotUseCase: GetAllScheduleSnapshotUseCase = DefaultGetAllScheduleSnapshotUseCase(
            coroutineScope = CoroutineScope(genFlowExecutionDispatcher(testScheduler)),
            pagingConfig = pagingConfig,
            scheduleRepository = scheduleRepository,
            completedScheduleShownRepository = mock { whenever(mock.get()) doReturn flowOf(isDoneScheduleShown) },
            scheduleUiStatePagingFlowFactory = fakeScheduleUiStatePagingFlowFactory
        )
        val snapshot: AllScheduleSnapshot =
            getAllScheduleSnapshotUseCase().take(1).first()
        val differ = AsyncPagingDataDiffer(
            diffCallback = IdentityItemCallback<ScheduleUiState>(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(snapshot.pagingScheduled)

        advanceUntilIdle()
        assertThat(
            snapshot.copy(pagingScheduled = PagingData.empty()),
            equalTo(
                genAllScheduleSnapshot(
                    pagingScheduled = PagingData.empty(),
                    isCompletedScheduleShown = isDoneScheduleShown
                )
            )
        )
        assertThat(
            differ.snapshot().items,
            equalTo(genScheduleUiStates(expectSchedules, fakeCompleteMark))
        )
    }
}