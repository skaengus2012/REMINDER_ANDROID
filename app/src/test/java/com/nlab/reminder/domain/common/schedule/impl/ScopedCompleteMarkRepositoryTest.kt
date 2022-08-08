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
import com.nlab.reminder.core.util.transaction.TransactionIdGenerator
import com.nlab.reminder.core.util.transaction.genTransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScopedCompleteMarkRepositoryTest {
    @Test
    fun testInsert() = runTest {
        val init = ScheduleId(genLong())
        val txId: String = genBothify()
        val isComplete: Boolean = genBoolean()
        val completeMarkRepository: CompleteMarkRepository =
            ScopedCompleteMarkRepository(genTransactionIdGenerator(txId))

        val generatedTxId = completeMarkRepository.insert(init, isComplete)
        assertThat(
            generatedTxId,
            equalTo(TransactionId(txId))
        )
        assertThat(
            completeMarkRepository.find(init),
            equalTo(CompleteMark(TransactionId(txId), isComplete))
        )
    }

    @Test
    fun testDelete() = runTest {
        val init = ScheduleId(genLong())
        val txId: String = genBothify()
        val completeMarkRepository: CompleteMarkRepository =
            ScopedCompleteMarkRepository(genTransactionIdGenerator(txId))

        completeMarkRepository.insert(init, genBoolean())
        completeMarkRepository.delete(init, TransactionId(txId))
        assertThat(completeMarkRepository.find(init), equalTo(null))
    }

    @Test
    fun `didn't delete when txId was different`() = runTest {
        val init = ScheduleId(genLong())
        val txId: String = genBothify()
        val isCompleteMarked: Boolean = genBoolean()
        val completeMarkRepository: CompleteMarkRepository =
            ScopedCompleteMarkRepository(genTransactionIdGenerator(txId))

        completeMarkRepository.insert(init, isCompleteMarked)
        completeMarkRepository.delete(init, TransactionId(""))
        assertThat(completeMarkRepository.find(init), equalTo(CompleteMark(TransactionId(txId), isCompleteMarked)))
    }

    @Test
    fun `didn't delete when scheduleId was different`() = runTest {
        val init = ScheduleId(1L)
        val txId: String = genBothify()
        val isCompleteMarked: Boolean = genBoolean()
        val completeMarkRepository: CompleteMarkRepository =
            ScopedCompleteMarkRepository(genTransactionIdGenerator(txId))

        completeMarkRepository.insert(init, isCompleteMarked)
        completeMarkRepository.delete(ScheduleId(0L), TransactionId(""))
        assertThat(completeMarkRepository.find(init), equalTo(CompleteMark(TransactionId(txId), isCompleteMarked)))
    }

    @Test
    fun testUpdate() = runTest {
        val init = ScheduleId(genLong())
        val transactionIdGenerator: TransactionIdGenerator = genTransactionIdGenerator()
        val completeMarkRepository: CompleteMarkRepository = ScopedCompleteMarkRepository(transactionIdGenerator)

        completeMarkRepository.insert(init, isComplete = true)
        completeMarkRepository.insert(init, isComplete = false)
        assertThat(completeMarkRepository.find(init)?.isComplete, equalTo(false))
    }

    @Test
    fun `return emptyMap when repository never emit`() = runTest {
        val emptyRepository: CompleteMarkRepository = mock { whenever(mock.get()) doReturn emptyFlow() }
        assertThat(emptyRepository.snapshot(), equalTo(emptyMap()))
    }
}