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

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import com.nlab.reminder.core.kotlin.NonNegativeLong
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * @author Doohyun
 */
class CleanUpScheduleBacklogsUseCaseTest {
    @Test
    fun `deleteAndCompleteAreRunInOrder`() = runTest {
        val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase = mockk()
        coEvery { registerScheduleDeletionJob.invoke() } returns ScheduleJobResult.Success
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk()
        coEvery {
            registerScheduleCompleteJob.invoke(0.seconds, null)
        } returns ScheduleJobResult.Success
        val useCase = CleanUpScheduleBacklogsUseCase(
            registerScheduleDeletionJob = registerScheduleDeletionJob,
            registerScheduleCompleteJob = registerScheduleCompleteJob
        )
        val result = useCase.invoke()
        
        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Success,
                    completionResult = ScheduleJobResult.Success
                )
            )
        )
        coVerifyOrder {
            registerScheduleDeletionJob.invoke()
            registerScheduleCompleteJob.invoke(
                debounceTimeout = 0.seconds,
                processUntilPriority = null
            )
        }
    }

    @Test
    fun `completeIsRunEvenIfDeleteFails`() = runTest {
        val expectedException = RuntimeException()
        val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase = mockk()
        coEvery {
            registerScheduleDeletionJob.invoke()
        } returns ScheduleJobResult.Failure(expectedException)
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk()
        coEvery {
            registerScheduleCompleteJob.invoke(0.seconds, null)
        } returns ScheduleJobResult.Success
        val useCase = CleanUpScheduleBacklogsUseCase(
            registerScheduleDeletionJob = registerScheduleDeletionJob,
            registerScheduleCompleteJob = registerScheduleCompleteJob
        )
        val result = useCase.invoke()
        
        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Failure(expectedException),
                    completionResult = ScheduleJobResult.Success
                )
            )
        )
        coVerifyOrder {
            registerScheduleDeletionJob.invoke()
            registerScheduleCompleteJob.invoke(
                debounceTimeout = 0.seconds,
                processUntilPriority = null
            )
        }
    }

    @Test
    fun `returnCancelledIfDeleteIsCancelled`() = runTest {
        val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase = mockk()
        coEvery {
            registerScheduleDeletionJob.invoke()
        } returns ScheduleJobResult.Cancelled
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk()
        coEvery {
            registerScheduleCompleteJob.invoke(0.seconds, null)
        } returns ScheduleJobResult.Success
        val useCase = CleanUpScheduleBacklogsUseCase(
            registerScheduleDeletionJob = registerScheduleDeletionJob,
            registerScheduleCompleteJob = registerScheduleCompleteJob
        )
        val result = useCase.invoke()
        
        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Cancelled,
                    completionResult = ScheduleJobResult.Success
                )
            )
        )
    }

    @Test
    fun `returnCancelledIfCompleteIsCancelled`() = runTest {
        val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase = mockk()
        coEvery { registerScheduleDeletionJob.invoke() } returns ScheduleJobResult.Success
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk()
        coEvery {
            registerScheduleCompleteJob.invoke(0.seconds, null)
        } returns ScheduleJobResult.Cancelled
        val useCase = CleanUpScheduleBacklogsUseCase(
            registerScheduleDeletionJob = registerScheduleDeletionJob,
            registerScheduleCompleteJob = registerScheduleCompleteJob
        )
        val result = useCase.invoke()
        
        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Success,
                    completionResult = ScheduleJobResult.Cancelled
                )
            )
        )
    }

    @Test
    fun `returnFailureIfCompleteFails`() = runTest {
        val expectedException = RuntimeException()
        val registerScheduleDeletionJob: RegisterScheduleDeletionJobUseCase = mockk()
        coEvery { registerScheduleDeletionJob.invoke() } returns ScheduleJobResult.Success
        val registerScheduleCompleteJob: RegisterScheduleCompleteJobUseCase = mockk()
        coEvery {
            registerScheduleCompleteJob.invoke(0.seconds, null)
        } returns ScheduleJobResult.Failure(expectedException)
        val useCase = CleanUpScheduleBacklogsUseCase(
            registerScheduleDeletionJob = registerScheduleDeletionJob,
            registerScheduleCompleteJob = registerScheduleCompleteJob
        )
        val result = useCase.invoke()
        
        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Success,
                    completionResult = ScheduleJobResult.Failure(expectedException)
                )
            )
        )
    }
}
