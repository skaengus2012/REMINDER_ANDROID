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
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * @author Thalys
 */
class DefaultUpdateScheduleCompletionUseCaseTest {
    @Test
    fun `Given save backlog succeeds, When update completion, Then save and register`() = runTest {
        suspend fun verify(targetCompleted: Boolean) {
            val scheduleId = genScheduleId()
            val priority = genNonNegativeLong()
            val debounceTimeout = genPositiveInt().value.seconds
            val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository =
                mockk {
                    coEvery { save(scheduleId, targetCompleted) } returns Result.success(
                        genScheduleCompletionBacklog(
                            scheduleId = scheduleId,
                            targetCompleted = targetCompleted,
                            priority = priority
                        )
                    )
                }
            val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
                mockk {
                    coEvery {
                        this@mockk.invoke(debounceTimeout, priority)
                    } returns ScheduleJobResult.Success
                }
            val useCase = genUpdateScheduleCompletionUseCase(
                scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
                requestScheduleCompletionJob = requestScheduleCompletionJob,
                debounceTimeout = debounceTimeout
            )

            val result = useCase.invoke(scheduleId, targetCompleted)

            assertThat(result, equalTo(ScheduleJobResult.Success))
            coVerifyOrder {
                scheduleCompletionBacklogRepository.save(scheduleId, targetCompleted)
                requestScheduleCompletionJob.invoke(
                    debounceTimeout = debounceTimeout,
                    processUntilPriority = priority
                )
            }
        }

        verify(targetCompleted = true)
        verify(targetCompleted = false)
    }

    @Test
    fun `Given save backlog fails, When update completion, Then return failure`() = runTest {
        val expectedException = RuntimeException()
        val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository =
            mockk {
                coEvery {
                    save(any(), any())
                } returns Result.failure(expectedException)
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase = mockk()
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
            requestScheduleCompletionJob = requestScheduleCompletionJob,
        )

        val result = useCase.invoke(
            scheduleId = genScheduleId(),
            targetCompleted = genBoolean()
        )

        assertThat(
            result,
            equalTo(ScheduleJobResult.Failure(expectedException))
        )
        coVerify { requestScheduleCompletionJob wasNot Called }
    }

    @Test
    fun `Given register job fails, When update completion, Then return failure`() = runTest {
        val scheduleId = genScheduleId()
        val priority = genNonNegativeLong()
        val debounceTimeout = genPositiveInt().value.seconds
        val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository =
            mockk {
                coEvery { save(scheduleId, true) } returns Result.success(
                    genScheduleCompletionBacklog(
                        scheduleId = scheduleId,
                        targetCompleted = true,
                        priority = priority
                    )
                )
            }
        val expectedException = RuntimeException()
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(debounceTimeout, priority)
                } returns ScheduleJobResult.Failure(expectedException)
            }
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
            requestScheduleCompletionJob = requestScheduleCompletionJob,
            debounceTimeout = debounceTimeout
        )

        val result = useCase.invoke(scheduleId, true)

        assertThat(
            result,
            equalTo(ScheduleJobResult.Failure(expectedException))
        )
    }

    @Test
    fun `Given register job is retrying, When update completion, Then return retrying`() = runTest {
        val scheduleId = genScheduleId()
        val priority = genNonNegativeLong()
        val debounceTimeout = genPositiveInt().value.seconds
        val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository =
            mockk {
                coEvery { save(scheduleId, true) } returns Result.success(
                    genScheduleCompletionBacklog(
                        scheduleId = scheduleId,
                        targetCompleted = true,
                        priority = priority
                    )
                )
            }
        val requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase =
            mockk {
                coEvery {
                    this@mockk.invoke(debounceTimeout, priority)
                } returns ScheduleJobResult.Retrying
            }
        val useCase = genUpdateScheduleCompletionUseCase(
            scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
            requestScheduleCompletionJob = requestScheduleCompletionJob,
            debounceTimeout = debounceTimeout
        )

        val result = useCase.invoke(scheduleId, true)

        assertThat(result, equalTo(ScheduleJobResult.Retrying))
    }
}

private fun genUpdateScheduleCompletionUseCase(
    scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository = mockk {
        coEvery {
            save(any(), any())
        } returns Result.success(genScheduleCompletionBacklog())
    },
    requestScheduleCompletionJob: RequestScheduleCompletionJobUseCase = mockk(),
    debounceTimeout: Duration = genPositiveInt().value.seconds
): DefaultUpdateScheduleCompletionUseCase {
    return DefaultUpdateScheduleCompletionUseCase(
        scheduleCompletionBacklogRepository = scheduleCompletionBacklogRepository,
        requestScheduleCompletionJob = requestScheduleCompletionJob,
        debounceTimeout = debounceTimeout
    )
}