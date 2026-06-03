/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedule

import com.nlab.reminder.core.data.model.genScheduleId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.trueValue
import org.junit.Test

/**
 * @author Doohyun
 */
class EnsuredDeleteScheduleUseCaseTest {
    @Test
    fun `Given internalUseCase, When invoked, Then internalUseCase invoked`() = runTest {
        val internalUseCase: DeleteScheduleUseCase = mockk {
            coEvery { invoke(scheduleIds = any()) } returns ScheduleJobResult.Success
        }
        val useCase = EnsuredDeleteScheduleUseCase(
            coroutineScope = this,
            deleteScheduleUseCase = internalUseCase
        )
        val scheduleIds = setOf(genScheduleId())
        useCase.invoke(scheduleIds = scheduleIds)
        
        coVerify(exactly = 1) {
            internalUseCase.invoke(scheduleIds)
        }
    }

    @Test
    fun `Given updateJob, When cancelled, Then internal useCase still completes`() = runTest {
        var updated = false
        val delayTimeMillis = 1000L
        val useCase = EnsuredDeleteScheduleUseCase(
            coroutineScope = this,
            deleteScheduleUseCase = mockk {
                coEvery { invoke(scheduleIds = any()) } coAnswers {
                    delay(delayTimeMillis)
                    updated = true
                    ScheduleJobResult.Success
                }
            }
        )
        val scheduleIds = setOf(genScheduleId())
        val updateJob = backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke(scheduleIds = scheduleIds)
        }
        
        advanceTimeBy(delayTimeMillis = delayTimeMillis / 2)
        assertThat(updated.not(), trueValue())
        updateJob.cancel()

        advanceTimeBy(delayTimeMillis)
        assertThat(updated, trueValue())
    }
}
