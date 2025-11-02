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

package com.nlab.reminder.feature.all

import app.cash.turbine.test
import com.nlab.reminder.core.component.schedulelist.content.GetScheduleListResourcesStreamUseCase
import com.nlab.reminder.core.component.schedulelist.content.genScheduleListResources
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.testkit.faker.genBoolean
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class GetAllScheduleListResourcesStreamUseCaseTest {
    @Test
    fun hello() = runTest {
        val expectedSchedules = genSchedules().toSet()
        val scheduleStreamSlot = slot<Flow<List<Schedule>>>()
        val useCase = genGetAllScheduleListResourcesStreamUseCase(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(true)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(GetScheduleQuery.All)
                } returns flowOf(expectedSchedules)
            },
            getScheduleListResourcesStream = mockk {
                every {
                    this@mockk.invoke(capture(scheduleStreamSlot))
                } returns flowOf(genScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = scheduleStreamSlot.captured
        capturedArg.test {
            val actualSchedules = awaitItem()
            assertThat(actualSchedules.toSet(), equalTo(expectedSchedules))

            awaitComplete()
        }
    }

    @Test
    fun hello2() = runTest {
        val expectedSchedules = genSchedules().toSet()
        val scheduleStreamSlot = slot<Flow<List<Schedule>>>()
        val useCase = genGetAllScheduleListResourcesStreamUseCase(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(false)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(GetScheduleQuery.ByComplete(isComplete = false))
                } returns flowOf(expectedSchedules)
            },
            getScheduleListResourcesStream = mockk {
                every {
                    this@mockk.invoke(capture(scheduleStreamSlot))
                } returns flowOf(genScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = scheduleStreamSlot.captured
        capturedArg.test {
            val actualSchedules = awaitItem()
            assertThat(actualSchedules.toSet(), equalTo(expectedSchedules))

            awaitComplete()
        }
    }

    @Test
    fun hello3() = runTest {
        val completedSchedule = genSchedule(
            id = ScheduleId(1),
            isComplete = true,
            visiblePriority = 1.toNonNegativeLong()
        )
        val notCompletedSchedule = genSchedule(
            id = ScheduleId(2),
            isComplete = false,
            visiblePriority = 2.toNonNegativeLong()
        )
        val expectedSortedSchedules = listOf(
            notCompletedSchedule,
            completedSchedule
        )
        val scheduleStreamSlot = slot<Flow<List<Schedule>>>()
        val useCase = genGetAllScheduleListResourcesStreamUseCase(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(genBoolean())
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(linkedSetOf(completedSchedule, notCompletedSchedule))
            },
            getScheduleListResourcesStream = mockk {
                every {
                    this@mockk.invoke(capture(scheduleStreamSlot))
                } returns flowOf(genScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = scheduleStreamSlot.captured
        capturedArg.test {
            val actualSchedules = awaitItem()
            assertThat(actualSchedules, equalTo(expectedSortedSchedules))

            awaitComplete()
        }
    }

    @Test
    fun hello4() = runTest {
        val firstVisibleSchedule = genSchedule(
            id = ScheduleId(2),
            isComplete = false,
            visiblePriority = 1.toNonNegativeLong()
        )
        val secondVisibleSchedule = genSchedule(
            id = ScheduleId(1),
            isComplete = false,
            visiblePriority = 2.toNonNegativeLong()
        )
        val expectedSortedSchedules = listOf(
            firstVisibleSchedule,
            secondVisibleSchedule
        )
        val scheduleStreamSlot = slot<Flow<List<Schedule>>>()
        val useCase = genGetAllScheduleListResourcesStreamUseCase(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(genBoolean())
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(linkedSetOf(secondVisibleSchedule, firstVisibleSchedule))
            },
            getScheduleListResourcesStream = mockk {
                every {
                    this@mockk.invoke(capture(scheduleStreamSlot))
                } returns flowOf(genScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = scheduleStreamSlot.captured
        capturedArg.test {
            val actualSchedules = awaitItem()
            assertThat(actualSchedules, equalTo(expectedSortedSchedules))

            awaitComplete()
        }
    }
}

private fun genGetAllScheduleListResourcesStreamUseCase(
    completedScheduleShownRepository: CompletedScheduleShownRepository = mockk(),
    scheduleRepository: ScheduleRepository = mockk(),
    getScheduleListResourcesStream: GetScheduleListResourcesStreamUseCase = mockk()
) = GetAllScheduleListResourcesStreamUseCase(
    completedScheduleShownRepository = completedScheduleShownRepository,
    scheduleRepository = scheduleRepository,
    getScheduleListResourcesStream = getScheduleListResourcesStream
)