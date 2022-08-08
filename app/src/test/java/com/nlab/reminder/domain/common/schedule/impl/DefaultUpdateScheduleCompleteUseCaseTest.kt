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

import com.nlab.reminder.core.kotlin.coroutine.Delay
import com.nlab.reminder.core.util.transaction.TransactionId
import com.nlab.reminder.core.util.transaction.TransactionIdGenerator
import com.nlab.reminder.core.util.transaction.genTransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.lang.RuntimeException

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUpdateScheduleCompleteUseCaseTest {
    private lateinit var schedule: Schedule
    private lateinit var transactionIdGenerator: TransactionIdGenerator
    private lateinit var completeMarkRepository: CompleteMarkRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var delay: Delay
    private var transactionId: TransactionId = TransactionId("")
    private var isCompleteMarked: Boolean = false

    @Before
    fun setup() = runTest {
        schedule = genSchedule()
        isCompleteMarked = genBoolean()
        transactionId = TransactionId(genBothify())
        transactionIdGenerator = genTransactionIdGenerator(transactionId.value)
        completeMarkRepository = mock {
            whenever(mock.get()) doReturn flow {
                emit(mapOf(schedule.id() to CompleteMark(transactionIdGenerator.generate(), isCompleteMarked)))
            }
        }
        scheduleRepository = mock()
        delay = mock()
    }

    @Test
    fun `update complete when useCase invoked and txId not changed`() = runTest {
        val updateScheduleCompleteUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            transactionIdGenerator,
            scheduleRepository,
            completeMarkRepository,
            delay
        )
        updateScheduleCompleteUseCase(schedule.id(), isCompleteMarked)

        verify(completeMarkRepository, once()).insert(schedule.id(), CompleteMark(transactionId, isCompleteMarked))
        verify(scheduleRepository, once()).updateComplete(schedule.id(), isCompleteMarked)
    }

    @Test
    fun `complete will update after marking complete with delay when useCase invoked`() = runTest {
        val updateScheduleCompleteUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            transactionIdGenerator,
            scheduleRepository,
            completeMarkRepository,
            delay
        )
        val beforeDelayedOrder = inOrder(completeMarkRepository, delay)
        val afterDelayedOrder = inOrder(delay, scheduleRepository)
        updateScheduleCompleteUseCase(schedule.id(), isCompleteMarked)

        beforeDelayedOrder.verify(completeMarkRepository, once())
            .insert(schedule.id(), CompleteMark(transactionId, isCompleteMarked))
        beforeDelayedOrder.verify(delay, once())()
        afterDelayedOrder.verify(delay, once())()
        afterDelayedOrder.verify(scheduleRepository, once()).updateComplete(schedule.id(), isCompleteMarked)
    }

    @Test
    fun `complete will not update when completeMarkRepository has not right element`() = runTest {
        val wrongSnapshot: List<Map<ScheduleId, CompleteMark>> = listOf(
            emptyMap(),
            mapOf(
                schedule.id() to CompleteMark(txId = TransactionId(""), genBoolean())
            )
        )
        wrongSnapshot
            .map { snapshot ->
                DefaultUpdateScheduleCompleteUseCase(
                    transactionIdGenerator,
                    scheduleRepository,
                    mock { whenever(mock.get()) doReturn flow { emit(snapshot) } },
                    delay
                )
            }
            .forEach { useCase -> useCase.invoke(schedule.id(), isCompleteMarked) }

        verify(scheduleRepository, never()).updateComplete(schedule.id(), isCompleteMarked)
    }

    @Test
    fun `rollback complete mark when update failed`() = runTest {
        val scheduleRepositoryErrorUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            transactionIdGenerator,
            mock { whenever(mock.updateComplete(schedule.id(), isCompleteMarked)) doThrow RuntimeException() },
            completeMarkRepository,
            delay
        )
        val insertErrorCompleteMarkRepository: CompleteMarkRepository = mock {
            whenever(
                mock.insert(schedule.id(), CompleteMark(transactionId, isCompleteMarked))
            ) doThrow RuntimeException()
        }
        val insertErrorUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            transactionIdGenerator,
            scheduleRepository,
            insertErrorCompleteMarkRepository,
            delay
        )
        val findErrorCompleteMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doThrow RuntimeException()
        }
        val findErrorUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            transactionIdGenerator,
            scheduleRepository,
            findErrorCompleteMarkRepository,
            delay
        )


        scheduleRepositoryErrorUseCase(schedule.id(), isCompleteMarked)
        insertErrorUseCase(schedule.id(), isCompleteMarked)
        findErrorUseCase(schedule.id(), isCompleteMarked)
        verify(completeMarkRepository, once()).delete(schedule.id(), transactionId)
        verify(insertErrorCompleteMarkRepository, once()).delete(schedule.id(), transactionId)
        verify(findErrorCompleteMarkRepository, once()).delete(schedule.id(), transactionId)
    }
}