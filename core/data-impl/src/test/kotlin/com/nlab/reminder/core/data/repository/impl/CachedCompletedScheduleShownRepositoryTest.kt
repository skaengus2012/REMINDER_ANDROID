/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository.impl

import app.cash.turbine.test
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

/**
 * @author Doohyun
 */
class CachedCompletedScheduleShownRepositoryTest {
    @Test
    fun `Given value to set, When setShown invoked, Then delegates to internal repository`() = runTest {
        val input = genBoolean()
        val internalRepository: CompletedScheduleShownRepository = mockk {
            every { getAsStream() } returns emptyFlow()
            coEvery { setShown(input) } returns Result.Success(Unit)
        }
        val repository = CachedCompletedScheduleShownRepository(
            coroutineScope = backgroundScope,
            completedScheduleShownRepository = internalRepository
        )
        repository.setShown(input)
        coVerify(exactly = 1) { internalRepository.setShown(input) }
    }

    @Test
    fun `Given internal repository, When collect, Then receives value from internal repository`() = runTest {
        val expected = genBoolean()
        val repository = CachedCompletedScheduleShownRepository(
            coroutineScope = backgroundScope,
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(expected)
            }
        )
        repository.getAsStream().test {
            val actual = awaitItem()
            assertThat(actual, equalTo(expected))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given multiple subscribers, When getAsStream collected, Then internal stream created only once`() = runTest {
        val internalRepository: CompletedScheduleShownRepository = mockk {
            every { getAsStream() } returns flowOf(genBoolean())
        }
        val repository = CachedCompletedScheduleShownRepository(
            coroutineScope = CoroutineScope(Dispatchers.Default),
            completedScheduleShownRepository = internalRepository
        )
        repeat(genInt(min = 3, max = 10)) {
            backgroundScope.launch(unconfinedTestDispatcher()) {
                repository.getAsStream().collect()
            }
        }
        verify(exactly = 1) {
            @Suppress("UnusedFlow")
            internalRepository.getAsStream()
        }
    }
}