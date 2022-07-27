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

package com.nlab.reminder.internal.common.schedule

import androidx.paging.*
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.reminder.internal.common.android.database.*
import com.nlab.reminder.internal.common.database.genScheduleEntityWithTagEntitiesList
import com.nlab.reminder.internal.common.database.toScheduleEntityWithTagEntities
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
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
class LocalScheduleRepositoryTest {
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(genFlowExecutionDispatcher(testScheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scheduleDao found when get`() {
        val expectedIsComplete = true
        val scheduleDao: ScheduleDao = mock()
        val scheduleRepository: ScheduleRepository = LocalScheduleRepository(scheduleDao)
        val requestConfig = ScheduleItemRequest.FindByComplete(expectedIsComplete)
        scheduleRepository.get(requestConfig)
        verify(scheduleDao, times(1)).find(isComplete = expectedIsComplete)
    }

    @Test
    fun `observe 2 times schedules when ScheduleDao sent 2 times notifications`() = runTest {
        val expectedIsComplete: Boolean = genBoolean()
        observeSchedulesWhenScheduleDaoNotified2Times(
            ScheduleItemRequest.FindByComplete(expectedIsComplete),
            setupMock = { scheduleDao, mockFlow ->
                whenever(scheduleDao.find(expectedIsComplete)) doReturn mockFlow
            }
        )
    }

    private fun observeSchedulesWhenScheduleDaoNotified2Times(
        scheduleItemRequest: ScheduleItemRequest,
        setupMock: (ScheduleDao, Flow<List<ScheduleEntityWithTagEntities>>) -> Unit
    ) = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val firstSchedule = genSchedule(tags = emptyList())
        val secondSchedule = genSchedule(tags = genTags().sortedBy { it.name })
        val scheduleDao: ScheduleDao = mock {
            setupMock(mock, flow {
                firstSchedule.toScheduleEntityWithTagEntities()
                    .let(::listOf)
                    .also { emit(it) }

                delay(1_000)
                secondSchedule.toScheduleEntityWithTagEntities()
                    .let { entity -> entity.copy(tagEntities = entity.tagEntities.sortedBy { it.name }.reversed()) }
                    .let(::listOf)
                    .also { emit(it) }
            }.flowOn(executeDispatcher))
        }
        val actualSchedules = mutableListOf<Schedule>()

        LocalScheduleRepository(scheduleDao)
            .get(scheduleItemRequest)
            .onEach { actualSchedules += it.first() }
            .launchIn(genFlowObserveDispatcher())

        advanceTimeBy(2_000)
        assertThat(actualSchedules, equalTo(listOf(firstSchedule, secondSchedule)))
    }

    @Test
    fun `pagedData converted to schedule from pagingDataFlow`() = runTest {
        val expectedIsComplete: Boolean = genBoolean()
        observeSchedulePagingDataWhenPagingDataFlowSubscribed(
            ScheduleItemPagingRequest.FindByComplete(expectedIsComplete),
            setupMock = { scheduleDao, fakePagingSource ->
                whenever(scheduleDao.findAsPagingSource(expectedIsComplete)) doReturn fakePagingSource
            }
        )
    }

    private fun observeSchedulePagingDataWhenPagingDataFlowSubscribed(
        scheduleItemPagingRequest: ScheduleItemPagingRequest,
        setupMock: (ScheduleDao, PagingSource<Int, ScheduleEntityWithTagEntities>) -> Unit
    ) = runTest {
        val input = genScheduleEntityWithTagEntitiesList()
        val scheduleDao: ScheduleDao = mock {
            setupMock(
                mock,
                FakePagingSource(
                    PagingSource.LoadResult.Page(
                        data = input,
                        prevKey = null,
                        nextKey = null
                    )
                )
            )
        }
        val differ = AsyncPagingDataDiffer(
            diffCallback = IdentityItemCallback<Schedule>(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )

        LocalScheduleRepository(scheduleDao)
            .getAsPagingData(scheduleItemPagingRequest, PagingConfig(pageSize = genInt()))
            .onEach { data -> differ.submitData(data) }
            .launchIn(genFlowObserveDispatcher())

        advanceUntilIdle()
        assertThat(differ.snapshot().items, equalTo(input.toSchedules()))
    }

    @Test
    fun `scheduleDao update complete state when repository invoked update complete state`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val scheduleDao: ScheduleDao = mock()

        LocalScheduleRepository(scheduleDao).updateCompleteState(schedule, isComplete)
        verify(scheduleDao, times(1)).updateCompleteState(schedule.scheduleId, isComplete)
    }

    private class FakePagingSource(
        private val result: LoadResult<Int, ScheduleEntityWithTagEntities>
    ) : PagingSource<Int, ScheduleEntityWithTagEntities>() {
        override fun getRefreshKey(state: PagingState<Int, ScheduleEntityWithTagEntities>): Int? = null
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ScheduleEntityWithTagEntities> = result
    }
}