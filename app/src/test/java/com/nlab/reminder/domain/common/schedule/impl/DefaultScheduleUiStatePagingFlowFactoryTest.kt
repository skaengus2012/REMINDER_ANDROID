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
import androidx.paging.PagingData
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultScheduleUiStatePagingFlowFactoryTest {
    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(genFlowExecutionDispatcher(testScheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `paging schedule flow combined with complete mark`() = runTest {
        val testFixture = CompleteMarkCombineTestFixture()
        val scheduleUiStatePagingFlowFactory =
            DefaultScheduleUiStatePagingFlowFactory(testFixture.completeMarkRepository)
        val differ = AsyncPagingDataDiffer(
            diffCallback = IdentityItemCallback<ScheduleUiState>(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(
            scheduleUiStatePagingFlowFactory
                .with(flowOf(PagingData.from(listOf(testFixture.schedule))))
                .take(1)
                .first()
        )
        advanceUntilIdle()
        assertThat(differ.snapshot().items, equalTo(testFixture.expectedScheduleUiStates))
    }
}