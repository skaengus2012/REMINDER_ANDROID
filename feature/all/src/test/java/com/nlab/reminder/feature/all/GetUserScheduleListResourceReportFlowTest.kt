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
import com.nlab.reminder.core.component.schedulelist.content.GetUserScheduleListResourcesFlowUseCase
import com.nlab.reminder.core.component.schedulelist.content.genScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.genUserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.genUserScheduleListResources
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.SchedulesLookup
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genIntGreaterThanZero
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.trueValue
import org.junit.Test

/**
 * @author Thalys
 */
class GetUserScheduleListResourceReportFlowTest {
    @Test
    fun `Given completed schedule shown, When collect, Then use schedules with all query`() = runTest {
        val schedules = genSchedules()
        val expectedSchedulesLookup = SchedulesLookup(schedules)
        val schedulesLookupFlowSlot = slot<Flow<SchedulesLookup>>()
        val useCase = genGetUserScheduleListResourceReportFlow(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(true)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(GetScheduleQuery.All)
                } returns flowOf(schedules)
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(capture(schedulesLookupFlowSlot))
                } returns flowOf(genUserScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = schedulesLookupFlowSlot.captured
        capturedArg.test {
            val actualSchedulesLookup = awaitItem()
            assertThat(actualSchedulesLookup, equalTo(expectedSchedulesLookup))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given completed schedule shown, When collect, Then receive shown status report`() = runTest {
        val completedSchedules = List(genIntGreaterThanZero(max = 10)) {
            genSchedule(
                id = ScheduleId(it.toLong()),
                isComplete = true,
            )
        }
        val notCompletedSchedules = List(genIntGreaterThanZero(max = 10)) {
            genSchedule(
                id = ScheduleId(completedSchedules.size + it.toLong()),
                isComplete = false
            )
        }
        val expectedUserScheduleListResources = buildList {
            completedSchedules.forEach {
                this += genUserScheduleListResource(genScheduleListResource(it))
            }
            notCompletedSchedules.forEach {
                this += genUserScheduleListResource(genScheduleListResource(it))
            }
        }
        val useCase = genGetUserScheduleListResourceReportFlow(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(true)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(completedSchedules.toSet() + notCompletedSchedules.toSet())
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(expectedUserScheduleListResources.toHashSet())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        useCase.invoke().test {
            val actualReport = awaitItem()
            assertThat(actualReport.completedScheduleShown, trueValue())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given completed schedule not shown, When collect, Then use schedules with not completed query`() = runTest {
        val schedules = genSchedules()
        val expectedSchedulesLookup = SchedulesLookup(schedules)
        val schedulesLookupFlowSlot = slot<Flow<SchedulesLookup>>()
        val useCase = genGetUserScheduleListResourceReportFlow(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(false)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(GetScheduleQuery.ByComplete(isComplete = false))
                } returns flowOf(schedules)
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(capture(schedulesLookupFlowSlot))
                } returns flowOf(genUserScheduleListResources())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        val capturedArg = schedulesLookupFlowSlot.captured
        capturedArg.test {
            val actualSchedulesLookup = awaitItem()
            assertThat(actualSchedulesLookup, equalTo(expectedSchedulesLookup))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given completed schedule not shown, When collect, Then receive hidden status report`() = runTest {
        val notCompletedSchedules = genSchedules()
        val expectedUserScheduleListResources = notCompletedSchedules.toSet { schedule ->
            genUserScheduleListResource(genScheduleListResource(schedule))
        }
        val useCase = genGetUserScheduleListResourceReportFlow(
            completedScheduleShownRepository = mockk {
                every { getAsStream() } returns flowOf(false)
            },
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(notCompletedSchedules)
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(expectedUserScheduleListResources)
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }

        useCase.invoke().test {
            val actualReport = awaitItem()
            assertThat(actualReport.completedScheduleShown.not(), trueValue())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedules, When collect, Then schedules sorted by completion status`() = runTest {
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
        val expectedUserScheduleListResources = listOf(
            genUserScheduleListResource(
                schedule = genScheduleListResource(notCompletedSchedule)
            ),
            genUserScheduleListResource(
                schedule = genScheduleListResource(completedSchedule)
            ),
        )
        val useCase = genGetUserScheduleListResourceReportFlow(
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(linkedSetOf(completedSchedule, notCompletedSchedule))
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(expectedUserScheduleListResources.toHashSet())
            }
        )
        useCase.invoke().test {
            val actualReport = awaitItem()
            assertThat(actualReport.userScheduleListResources, equalTo(expectedUserScheduleListResources))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedules with same completion status, When collect, Then sorted by visible priority`() = runTest {
        val completedStatus = genBoolean()
        val firstVisibleSchedule = genSchedule(
            id = ScheduleId(2),
            isComplete = completedStatus,
            visiblePriority = 1.toNonNegativeLong()
        )
        val secondVisibleSchedule = genSchedule(
            id = ScheduleId(1),
            isComplete = completedStatus,
            visiblePriority = 2.toNonNegativeLong()
        )
        val expectedUserScheduleListResources = listOf(
            genUserScheduleListResource(
                schedule = genScheduleListResource(firstVisibleSchedule)
            ),
            genUserScheduleListResource(
                schedule = genScheduleListResource(secondVisibleSchedule)
            ),
        )
        val useCase = genGetUserScheduleListResourceReportFlow(
            scheduleRepository = mockk {
                every {
                    getSchedulesAsStream(any())
                } returns flowOf(linkedSetOf(secondVisibleSchedule, firstVisibleSchedule))
            },
            getUserScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(expectedUserScheduleListResources.toHashSet())
            }
        )
        useCase.invoke().test {
            val actualReport = awaitItem()
            assertThat(actualReport.userScheduleListResources, equalTo(expectedUserScheduleListResources))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given slow getScheduleListResources, When new schedules emitted, Then ignore invalidated value`() = runTest {
        fun verify(firstSchedules: Set<Schedule>, secondSchedules: Set<Schedule>) {
            val secondScheduleEmitDelayedTimeMs = 1000L
            val secondResourcesEmitDelayTimeMs = 500L
            val useCase = genGetUserScheduleListResourceReportFlow(
                scheduleRepository = mockk {
                    every {
                        getSchedulesAsStream(any())
                    } returns flow {
                        emit(firstSchedules)
                        delay(secondScheduleEmitDelayedTimeMs)
                        emit(secondSchedules)
                    }
                },
                getUserScheduleListResourcesFlow = mockk {
                    every { this@mockk.invoke(any()) } answers {
                        if (args[0] == firstSchedules) {
                            firstSchedules
                                .toSet { schedule ->
                                    genUserScheduleListResource(schedule = genScheduleListResource(schedule))
                                }
                                .let { flowOf(it) }
                        } else {
                            flow {
                                // Scenario where second link metadata search is slow
                                delay(secondResourcesEmitDelayTimeMs)
                                secondSchedules
                                    .toSet { schedule ->
                                        genUserScheduleListResource(schedule = genScheduleListResource(schedule))
                                    }
                                    .run { emit(this) }
                            }
                        }
                    }
                }
            )
            val collected = mutableListOf<UserScheduleListResourceReport>()
            backgroundScope.launch(unconfinedTestDispatcher()) {
                useCase.invoke().toList(destination = collected)
            }
            advanceTimeBy(secondScheduleEmitDelayedTimeMs + secondResourcesEmitDelayTimeMs / 2)
            assertThat(collected.size, equalTo(1))
        }

        verify(
            firstSchedules = setOf(
                genSchedule(id = ScheduleId(1))
            ),
            secondSchedules = setOf(
                genSchedule(id = ScheduleId(1)),
                genSchedule(id = ScheduleId(2))
            )
        )

        verify(
            firstSchedules = setOf(
                genSchedule(id = ScheduleId(1)),
                genSchedule(id = ScheduleId(2))
            ),
            secondSchedules = setOf(
                genSchedule(id = ScheduleId(2)),
                genSchedule(id = ScheduleId(3))
            )
        )
    }
}

private fun genGetUserScheduleListResourceReportFlow(
    completedScheduleShownRepository: CompletedScheduleShownRepository = mockk {
        every { getAsStream() } returns flowOf(genBoolean())
    },
    scheduleRepository: ScheduleRepository = mockk(),
    getUserScheduleListResourcesFlow: GetUserScheduleListResourcesFlowUseCase = mockk()
) = GetUserScheduleListResourceReportFlowUseCase(
    completedScheduleShownRepository = completedScheduleShownRepository,
    scheduleRepository = scheduleRepository,
    getUserScheduleListResourcesFlow = getUserScheduleListResourcesFlow
)