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

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * @author Doohyun
 */
class CleanUpScheduleBacklogsUseCaseTest {
    @Test
    fun `Given jobs succeed, When cleanup backlogs, Then return success report`() = runTest {
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Success
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Success
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
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
            requestScheduleDeletionJob.invoke()
            requestScheduleCompletionJob.invoke(
                debounceTimeout = 0.seconds,
                processUntilPriority = null
            )
        }
    }

    @Test
    fun `Given delete fails, When cleanup backlogs, Then complete is run and return failure`() = runTest {
        val expectedException = RuntimeException()
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke()
                } returns ScheduleJobResult.Failure(expectedException)
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Success
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
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
            requestScheduleDeletionJob.invoke()
            requestScheduleCompletionJob.invoke(
                debounceTimeout = 0.seconds,
                processUntilPriority = null
            )
        }
    }

    @Test
    fun `Given delete cancelled, When cleanup backlogs, Then return cancelled`() = runTest {
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Cancelled
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Success
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
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
    fun `Given complete cancelled, When cleanup backlogs, Then return cancelled`() = runTest {
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Success
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Cancelled
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
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
    fun `Given complete fails, When cleanup backlogs, Then return failure`() = runTest {
        val expectedException = RuntimeException()
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Success
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Failure(expectedException)
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
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

    @Test
    fun `Given deletion job is retrying, When cleanup backlogs, Then return retrying report`() = runTest {
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Retrying
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Success
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
        )

        val result = useCase.invoke()

        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Retrying,
                    completionResult = ScheduleJobResult.Success
                )
            )
        )
    }

    @Test
    fun `Given completion job is retrying, When cleanup backlogs, Then return retrying report`() = runTest {
        val requestScheduleDeletionJob: RequestScheduleDeletionJobUseCase =
            mockk {
                coEvery { this@mockk.invoke() } returns ScheduleJobResult.Success
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(
                        debounceTimeout = 0.seconds,
                        processUntilPriority = null
                    )
                } returns ScheduleJobResult.Retrying
            }
        val useCase = CleanUpScheduleBacklogsUseCase(
            requestScheduleDeletionJob = requestScheduleDeletionJob,
            requestScheduleCompletionJob = requestScheduleCompletionJob
        )

        val result = useCase.invoke()

        assertThat(
            result,
            equalTo(
                CleanUpScheduleBacklogsReport(
                    deletionResult = ScheduleJobResult.Success,
                    completionResult = ScheduleJobResult.Retrying
                )
            )
        )
    }
}
