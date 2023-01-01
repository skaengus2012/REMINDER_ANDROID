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
import com.nlab.reminder.domain.common.util.transaction.TransactionId
import com.nlab.reminder.domain.common.util.link.transaction.genTransactionIdGenerator
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.isSuccess
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.*
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
                scheduleId to CompleteMark(isComplete, isApplied = false, TransactionId(fixedTxId))
            )
        )
    }

    @Test
    fun `commit complete state if not applied`() = runTest {
        val testScheduleId = ScheduleId(0)
        val testComplete: Boolean = genBoolean()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(
                mapOf(
                    testScheduleId to CompleteMark(
                        isComplete = testComplete,
                        isApplied = false,
                        transactionId = TransactionId(genBothify())
                    ),
                    ScheduleId(1) to CompleteMark(
                        isComplete = genBoolean(),
                        isApplied = true,
                        transactionId = TransactionId(genBothify())
                    )
                )
            )
        }
        val scheduleRepository: ScheduleRepository = mock()
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock()
        )

        updateCompleteUseCase(ScheduleId(genLong()), genBoolean())

        verify(scheduleRepository, once()).update(
            UpdateRequest.Completes(listOf(ModifyCompleteRequest(testScheduleId, testComplete)))
        )
    }

    private suspend fun mapScheduleCompleteResultToTotalResult(scheduleCompleteResult: Result<Unit>): Result<Unit> {
        val scheduleId = ScheduleId(genLong())
        val isComplete = genBoolean()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(
                mapOf(
                    scheduleId to CompleteMark(
                        isComplete,
                        isApplied = false,
                        transactionId = TransactionId(genBothify())
                    )
                )
            )
        }
        val scheduleRepository: ScheduleRepository = mock {
            whenever(
                mock.update(UpdateRequest.Completes(listOf(ModifyCompleteRequest(scheduleId, isComplete))))
            ) doReturn scheduleCompleteResult
        }
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock()
        )

        return updateCompleteUseCase(ScheduleId(genLong()), genBoolean())
    }

    @Test
    fun `update result for commit complete to schedule was success`() = runTest {
        assertThat(
            Result.Success(Unit),
            equalTo(
                mapScheduleCompleteResultToTotalResult(
                    Result.Success(Unit)
                )
            )
        )
    }

    @Test
    fun `update result for commit complete to schedule was failed`() = runTest {
        val throwable = Throwable()
        assertThat(
            Result.Failure(throwable),
            equalTo(
                mapScheduleCompleteResultToTotalResult(
                    Result.Failure(throwable)
                )
            )
        )
    }

    @Test
    fun `loaded snapshot after insert with delayed`() = runTest {
        val scheduleId = ScheduleId(genLong())
        val isComplete = genBoolean()
        val txId = genBothify()
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(emptyMap())
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
            mapOf(
                scheduleId to CompleteMark(
                    isComplete,
                    isApplied = false,
                    transactionId = TransactionId(txId)
                )
            )
        )
        beforeDelayedOrder.verify(delayUntilTransactionPeriod, once())()
        afterDelayedOrder.verify(delayUntilTransactionPeriod, once())()
        afterDelayedOrder.verify(completeMarkRepository, once()).get()
    }

    @Test
    fun `skipped commit when requestCount still have it`() = runTest {
        val completeMarkRepository: CompleteMarkRepository = mock()
        val scheduleRepository: ScheduleRepository = mock()
        val firstInfinityDelay = object : Delay {
            private val dispatcher = genFlowExecutionDispatcher(testScheduler)
            private var count: Int = 0
            override suspend fun invoke() {
                withContext(dispatcher) {
                    val delayTime = if (count > 0) 0 else Long.MAX_VALUE
                    ++count
                    delay(delayTime)
                }
            }
        }
        val schedule: Schedule = genSchedule()
        val complete: Boolean = genBoolean()
        val updateCompleteUseCase = DefaultUpdateCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = firstInfinityDelay
        )
        val delayTime = 1_000L
        val job = launch {
            updateCompleteUseCase(schedule.id, complete)
        }
        delay(delayTime)
        advanceTimeBy(delayTime)

        val result = updateCompleteUseCase(genSchedule().id, complete)

        verify(completeMarkRepository, never()).updateToApplied(any())
        verify(scheduleRepository, never()).update(any())
        assert(result.isSuccess)

        job.cancelAndJoin()
    }
}