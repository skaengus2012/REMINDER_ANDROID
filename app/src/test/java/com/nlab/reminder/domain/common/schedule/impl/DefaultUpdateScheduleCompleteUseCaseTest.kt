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
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.util.transaction.TransactionId
import com.nlab.reminder.core.util.transaction.genTransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUpdateScheduleCompleteUseCaseTest {
    @Test
    fun `updateComplete invoked when txId changed`() = runTest {
        val txId: String = genBothify()
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val scheduleRepository: ScheduleRepository = mock()
        updateScheduleComplete(
            txId = txId,
            schedule = schedule,
            scheduleRepository = scheduleRepository,
            completeMarkSnapshot = mapOf(
                schedule.id() to CompleteMark(TransactionId(txId), isComplete)
            )
        )

        verify(scheduleRepository, once()).updateComplete(schedule.id(), isComplete)
    }

    @Test
    fun `updateComplete never invoked when txId changed`() = runTest {
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val scheduleRepository: ScheduleRepository = mock()
        updateScheduleComplete(
            schedule = schedule,
            isComplete = isComplete,
            scheduleRepository = scheduleRepository,
            completeMarkSnapshot = mapOf(
                schedule.id() to CompleteMark(TransactionId(genBothify()), isComplete)
            )
        )

        verify(scheduleRepository, never()).updateComplete(schedule.id(), isComplete)
    }

    @Test
    fun `updateComplete never invoked when completeMark was empty`() = runTest {
        val schedule: Schedule = genSchedule()
        val scheduleRepository: ScheduleRepository = mock()
        updateScheduleComplete(
            schedule = schedule,
            scheduleRepository = scheduleRepository,
            completeMarkSnapshot = emptyMap()
        )

        verify(scheduleRepository, never()).updateComplete(schedule.id(), genBoolean())
    }

    @Test
    fun `complete will update after marking complete with delay when useCase invoked`() = runTest {
        val txId: String = genBothify()
        val schedule: Schedule = genSchedule()
        val isComplete: Boolean = genBoolean()
        val completeMark = CompleteMark(TransactionId(txId), isComplete)
        val scheduleRepository: ScheduleRepository = mock()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn flowOf(mapOf(schedule.id() to completeMark))
        }
        val pendingDelay: Delay = mock()
        val beforeDelayedOrder = inOrder(completeMarkRepository, pendingDelay)
        val afterDelayedOrder = inOrder(pendingDelay, scheduleRepository)
        updateScheduleComplete(
            txId,
            schedule,
            isComplete,
            scheduleRepository,
            completeMarkRepository,
            pendingDelay
        )

        beforeDelayedOrder.verify(completeMarkRepository, once()).insert(schedule.id(), completeMark)
        beforeDelayedOrder.verify(pendingDelay, once())()
        afterDelayedOrder.verify(pendingDelay, once())()
        afterDelayedOrder.verify(scheduleRepository, once()).updateComplete(schedule.id(), isComplete)
    }

    @Test
    fun `rollback complete mark when update failed`() = runTest {
        val txId: String = genBothify()
        val schedule: Schedule = genSchedule()
        val completeMark = CompleteMark(TransactionId(txId), genBoolean())
        val scheduleRepository: ScheduleRepository = mock {
            whenever(mock.updateComplete(schedule.id(), completeMark.isComplete)) doReturn Result.Failure(Throwable())
        }
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn flowOf(mapOf(schedule.id() to completeMark))
        }
        updateScheduleComplete(
            txId,
            schedule,
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository
        )

        verify(completeMarkRepository, once()).delete(schedule.id(), completeMark.txId)
    }

    private fun updateScheduleComplete(
        txId: String = genBothify(),
        schedule: Schedule = genSchedule(),
        isComplete: Boolean = genBoolean(),
        scheduleRepository: ScheduleRepository = mock(),
        completeMarkSnapshot: Map<ScheduleId, CompleteMark> = emptyMap(),
    ) = updateScheduleComplete(
        txId,
        schedule,
        isComplete,
        scheduleRepository = scheduleRepository,
        completeMarkRepository = mock { whenever(mock.get()) doReturn flowOf(completeMarkSnapshot) },
    )

    private fun updateScheduleComplete(
        txId: String,
        schedule: Schedule,
        isComplete: Boolean = genBoolean(),
        scheduleRepository: ScheduleRepository = mock(),
        completeMarkRepository: CompleteMarkRepository = mock(),
        pendingDelay: Delay = mock()
    ) = runTest {
        val updateScheduleCompleteUseCase: UpdateScheduleCompleteUseCase = DefaultUpdateScheduleCompleteUseCase(
            genTransactionIdGenerator(txId),
            scheduleRepository,
            completeMarkRepository,
            pendingDelay
        )
        updateScheduleCompleteUseCase(schedule.id(), isComplete)
    }
}