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
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLong
import com.nlab.reminder.test.once
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultModifyScheduleCompleteUseCaseTest {
    @Test
    fun `completeMark inserted`() = runTest {
        val fixedTxId = genBothify()
        val scheduleId = ScheduleId(genLong())
        val isComplete = genBoolean()
        val completeMarkRepository: CompleteMarkRepository = mock { whenever(mock.get()) doReturn emptyFlow() }
        val updateCompleteUseCase = DefaultModifyScheduleCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(expected = fixedTxId),
            scheduleRepository = mock(),
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock(),
            dispatcher = Dispatchers.Default
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
            whenever(mock.get()) doReturn flow {
                emit(
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
        }
        val scheduleRepository: ScheduleRepository = mock()
        val updateCompleteUseCase = DefaultModifyScheduleCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock(),
            dispatcher = Dispatchers.Default
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
            whenever(mock.get()) doReturn flow {
                emit(
                    mapOf(
                        scheduleId to CompleteMark(
                            isComplete,
                            isApplied = false,
                            transactionId = TransactionId(genBothify())
                        )
                    )
                )
            }
        }
        val scheduleRepository: ScheduleRepository = mock {
            whenever(
                mock.update(UpdateRequest.Completes(listOf(ModifyCompleteRequest(scheduleId, isComplete))))
            ) doReturn scheduleCompleteResult
        }
        val updateCompleteUseCase = DefaultModifyScheduleCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(),
            scheduleRepository = scheduleRepository,
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = mock(),
            dispatcher = Dispatchers.Default
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
        val completeMarkRepository: CompleteMarkRepository = mock { whenever(mock.get()) doReturn emptyFlow() }
        val delayUntilTransactionPeriod: Delay = mock()
        val beforeDelayedOrder = inOrder(completeMarkRepository, delayUntilTransactionPeriod)
        val afterDelayedOrder = inOrder(delayUntilTransactionPeriod, completeMarkRepository)
        val updateCompleteUseCase = DefaultModifyScheduleCompleteUseCase(
            transactionIdGenerator = genTransactionIdGenerator(txId),
            scheduleRepository = mock(),
            completeMarkRepository = completeMarkRepository,
            delayUntilTransactionPeriod = delayUntilTransactionPeriod,
            dispatcher = Dispatchers.Unconfined
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
}