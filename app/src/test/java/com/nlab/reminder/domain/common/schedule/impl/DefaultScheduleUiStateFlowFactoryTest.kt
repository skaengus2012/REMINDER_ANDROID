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

import com.nlab.reminder.core.util.transaction.TransactionId
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genFlowExecutionDispatcher
import com.nlab.reminder.test.genFlowObserveDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultScheduleUiStateFlowFactoryTest {
    @Test
    fun `schedule combined with complete mark after 1000ms`() = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val isComplete: Boolean = genBoolean()
        val schedule: Schedule = genSchedule(isComplete = isComplete)
        val completeMark = CompleteMark(
            isComplete.not(),
            isApplied = genBoolean(),
            transactionId = TransactionId(genBothify())
        )
        val schedulesFlow: Flow<List<Schedule>> = flowOf(listOf(schedule))
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn flow {
                delay(1_000)
                emit(mapOf(schedule.id() to completeMark))
            }.flowOn(executeDispatcher)
        }
        val scheduleUiStateFlowFactory = DefaultScheduleUiStateFlowFactory(completeMarkRepository)
        val acc: MutableList<ScheduleUiState> = mutableListOf()
        scheduleUiStateFlowFactory
            .combineWith(schedulesFlow)
            .onEach { acc += it.first() }
            .launchIn(genFlowObserveDispatcher())

        advanceTimeBy(1_500)
        assertThat(
            acc,
            equalTo(
                listOf(
                    genScheduleUiState(schedule),
                    genScheduleUiState(schedule, isCompleteMarked = isComplete.not())
                )
            )
        )
    }
}