/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleUpdateRequest
import com.nlab.reminder.core.kotlinx.coroutine.delay.Delay
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
class CompleteScheduleWithMarkUseCaseTest {

    @Test
    fun `When invoked, Then completeMark add`() = runTest {
        val scheduleId = genScheduleId()
        val isComplete = false // When flag put a value other than false, Jacoco coverage did not work normally. 😭
        val completeMarkRepository = genCompleteMarkRepositoryMockWithEmptyTable()
        val useCase = genUpdateScheduleCompletionUseCase(
            completeMarkRepository = completeMarkRepository
        )

        useCase.invoke(scheduleId, isComplete)
        verify(completeMarkRepository, once()).add(scheduleId, isComplete)
    }

    @Test
    fun `Given completed mark added, Then scheduleRepository update complete from completeMark tables`() = runTest {
        val expectedCompleteTable: Map<ScheduleId, Boolean> =
            List(genInt(min = 1, max = 10)) { index -> genScheduleId(index.toLong()) }.associateWith { true }
        val scheduleRepository: ScheduleRepository = mock()
        val completeMarkRepository: ScheduleCompleteMarkRepository = mock {
            whenever(mock.getStream()) doReturn MutableStateFlow(
                expectedCompleteTable
            )
        }
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository
        )

        useCase.invoke(genScheduleId(), genBoolean())
        verify(scheduleRepository, once()).update(ScheduleUpdateRequest.Completes(expectedCompleteTable))
    }

    @Test
    fun `Given completed mark not existed, Then scheduleRepository never updated`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        val useCase = genUpdateScheduleCompletionUseCase(
            completeMarkRepository = genCompleteMarkRepositoryMockWithEmptyTable(),
            scheduleRepository = scheduleRepository
        )

        useCase.invoke(genScheduleId(), genBoolean())
        verify(scheduleRepository, never()).update(any())
    }

    @Test
    fun `AggregateDelay called between adding completeMark and updating scheduleRepository`() = runTest {
        val completeMarkRepository: ScheduleCompleteMarkRepository = mock {
            whenever(mock.getStream()) doReturn MutableStateFlow(mapOf(genScheduleId() to genBoolean()))
        }
        val aggregateDelay: Delay = mock()
        val scheduleRepository: ScheduleRepository = mock()
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            aggregateDelay = aggregateDelay
        )
        val scheduleId = genScheduleId()
        val isComplete = genBoolean()

        useCase.invoke(scheduleId, isComplete)
        inOrder(scheduleRepository, completeMarkRepository, aggregateDelay) {
            verify(completeMarkRepository).add(scheduleId, isComplete)
            verify(aggregateDelay).invoke()
            verify(scheduleRepository).update(any())
        }
    }
}

private fun genUpdateScheduleCompletionUseCase(
    scheduleRepository: ScheduleRepository = mock(),
    completeMarkRepository: ScheduleCompleteMarkRepository = mock(),
    aggregateDelay: Delay = mock(),
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
): CompleteScheduleWithMarkUseCase = CompleteScheduleWithMarkUseCase(
    scheduleRepository = scheduleRepository,
    completeMarkRepository = completeMarkRepository,
    aggregateDelay = aggregateDelay,
    dispatcher = dispatcher
)

private fun genCompleteMarkRepositoryMockWithEmptyTable(): ScheduleCompleteMarkRepository = mock {
    whenever(mock.getStream()) doReturn MutableStateFlow(mapOf())
}