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
import com.nlab.reminder.core.kotlin.util.isSuccess
import com.nlab.reminder.domain.common.util.transaction.TransactionId
import com.nlab.reminder.domain.common.util.link.transaction.genTransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.util.link.transaction.genTransactionId
import com.nlab.reminder.test.*
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genBothify
import com.nlab.testkit.genLong
import com.nlab.testkit.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUpdateCompleteUseCaseTest {
    @Test
    fun `commit complete`() = runTest {
        val insertedSet = ScheduleId(0) to true
        val inputSet = ScheduleId(1) to false
        val scheduleRepository: ScheduleRepository = mock()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(
                CompleteMarkTable(insertedSet.first to genCompleteMark(insertedSet.second))
            )
        }
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock()
        )

        updateCompleteUseCase(inputSet.first, inputSet.second)

        verify(scheduleRepository, once()).update(
            UpdateRequest.Completes(
                listOf(
                    ModifyCompleteRequest(insertedSet.first, insertedSet.second),
                )
            )
        )
    }

    @Test
    fun `completeMark inserted`() = runTest {
        val fixedTxId = genBothify()
        val scheduleId = ScheduleId(genLong())
        val isComplete = genBoolean()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(emptyMap())
        }
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(expected = fixedTxId),
            scheduleRepository = mock(),
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock()
        )

        updateCompleteUseCase(scheduleId, isComplete)

        verify(completeMarkRepository, once()).insert(
            mapOf(
                scheduleId to genCompleteMark(isComplete, TransactionId(fixedTxId))
            )
        )
    }

    @Test
    fun `loaded snapshot after insert with delayed`() = runTest {
        val scheduleId = ScheduleId(genLong())
        val isComplete = genBoolean()
        val txId = genBothify()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(CompleteMarkTable())
        }
        val delayUntilTransactionPeriod: Delay = mock()
        val beforeDelayedOrder = inOrder(completeMarkRepository, delayUntilTransactionPeriod)
        val afterDelayedOrder = inOrder(delayUntilTransactionPeriod, completeMarkRepository)
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(txId),
            scheduleRepository = mock(),
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = delayUntilTransactionPeriod
        )

        updateCompleteUseCase(scheduleId, isComplete)

        beforeDelayedOrder.verify(completeMarkRepository, once()).insert(
            CompleteMarkTable(
                scheduleId to CompleteMark(isComplete, transactionId = genTransactionId(txId))
            )
        )
        beforeDelayedOrder.verify(delayUntilTransactionPeriod, once())()
        afterDelayedOrder.verify(delayUntilTransactionPeriod, once())()
        afterDelayedOrder.verify(completeMarkRepository, once()).get()
    }

    @Test
    fun `skipped commit when requestCount still have it`() = runTest {
        val scheduleRepository: ScheduleRepository = mock()
        val firstInfinityDelay = object : Delay {
            private val dispatcher = genFlowExecutionDispatcher(testScheduler)
            private var count: Int = 0
            override suspend fun invoke() {
                withContext(dispatcher) {
                    val delayTime = if (count > 0) 0 else Long.MAX_VALUE
                    ++count
                    println(delayTime)
                    delay(delayTime)
                }
            }
        }
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = mock(),
            delayUntilTransactionPeriod = firstInfinityDelay
        )
        val delayTime = 1_000L
        val job = launch {
            updateCompleteUseCase(genSchedule().id, genBoolean())
        }
        delay(delayTime)
        advanceTimeBy(delayTime)

        val result = updateCompleteUseCase(genSchedule().id, genBoolean())
        verify(scheduleRepository, never()).update(any())
        assert(result.isSuccess)

        job.cancelAndJoin()
    }
}