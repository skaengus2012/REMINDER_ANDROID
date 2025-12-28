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

package com.nlab.reminder.core.component.schedule

import com.nlab.reminder.core.data.model.genScheduleCompletionBacklog
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genPositiveInt
import com.nlab.testkit.faker.genBoolean
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @author Thalys
 */
class DefaultUpdateScheduleCompletionUseCaseTest {
    @Test
    fun `Given saving backlog succeeds, When invoked, Then save backlog and register job in order`() = runTest {
        suspend fun verify(targetCompleted: Boolean) {
            val scheduleId = genScheduleId()
            val priority = genNonNegativeLong()
            val debounceTimeout = genPositiveInt().value.seconds
            val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository = mockk {
                coEvery { save(scheduleId, targetCompleted) } returns Result.success(
                    genScheduleCompletionBacklog(
                        scheduleId = scheduleId,
                        targetCompleted = targetCompleted,
                        priority = priority
                    )
                )
            }
            val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk(relaxed = true)
            val useCase = genUpdateScheduleCompletionUseCase(
                scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
                registerScheduleCompleteJob = registerScheduleCompleteJob,
                debounceTimeout = debounceTimeout
            )
            useCase.invoke(scheduleId, targetCompleted)
            coVerifyOrder {
                scheduleCompletionBacklogRepository.save(scheduleId, targetCompleted)
                registerScheduleCompleteJob.invoke(debounceTimeout = debounceTimeout, processUntilPriority = priority)
            }
        }
        verify(targetCompleted = true)
        verify(targetCompleted = false)
    }

    @Test
    fun `Given saving backlog fails, When invoked, Then does not register job`() = runTest {
        val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository = mockk {
            coEvery {
                save(any(), any())
            } returns Result.failure(RuntimeException())
        }
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk(relaxed = true)
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
            registerScheduleCompleteJob = registerScheduleCompleteJob,
        )
        useCase.invoke(scheduleId = genScheduleId(), targetCompleted = genBoolean())
        verify { registerScheduleCompleteJob wasNot Called }
    }
}

private fun genUpdateScheduleCompletionUseCase(
    scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository = mockk {
        coEvery {
            save(any(), any())
        } returns Result.success(genScheduleCompletionBacklog())
    },
    registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk(relaxed = true),
    debounceTimeout: Duration = genPositiveInt().value.seconds
) = DefaultUpdateScheduleCompletionUseCase(
    scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
    registerScheduleCompleteJob = registerScheduleCompleteJob,
    debounceTimeout = debounceTimeout
)