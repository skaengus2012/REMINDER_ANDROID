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

import com.nlab.reminder.domain.common.util.transaction.TransactionId
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genInt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScopedCompleteMarkRepositoryTest {
    @Test
    fun `insert executed on io thread`() = runTest {
        val repository = ScopedCompleteMarkRepository()
        val count = genInt("####")
        (0 until count)
            .map { ScheduleId(it.toLong()) }
            .map { scheduleId ->
                launch(Dispatchers.IO) {
                    repository.insert(
                        mapOf(
                            scheduleId to CompleteMark(genBoolean(), genBoolean(), TransactionId(genBothify()))
                        )
                    )
                }
            }
            .joinAll()
        assertThat(repository.get().first().size, equalTo(count))
    }

    @Test
    fun `completeMark applied without first element`() = runTest {
        val repository = ScopedCompleteMarkRepository()
        val scheduleAndCompleteMark = List(genInt()) { index ->
            ScheduleId(index.toLong()) to CompleteMark(
                genBoolean(),
                isApplied = false,
                TransactionId(genBothify())
            )
        }
        repository.insert(scheduleAndCompleteMark.toMap())
        repository.insert(scheduleAndCompleteMark.first().let { (scheduleId, completeMark) ->
            mapOf(
                scheduleId to completeMark.copy(transactionId = TransactionId(""))
            )
        })
        repository.updateToApplied(scheduleAndCompleteMark.toMap())

        assertThat(
            repository.get().first().filter { it.value.isApplied }.size,
            equalTo(scheduleAndCompleteMark.size - 1)
        )
    }
}