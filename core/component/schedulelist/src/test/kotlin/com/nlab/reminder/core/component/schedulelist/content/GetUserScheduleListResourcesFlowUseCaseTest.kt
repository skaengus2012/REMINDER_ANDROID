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

package com.nlab.reminder.core.component.schedulelist.content

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.SchedulesLookup
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleTiming
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.shuffledSubset
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class GetUserScheduleListResourcesFlowUseCaseTest {
    @Test
    fun `Given schedulesLookupFlow, When invoked, Then pass schedules to downstream`() = runTest {
        val expectedSchedulesQuery = genSchedules()
        val schedulesFlowSlot = slot<Flow<Set<Schedule>>>()
        val useCase = genGetUserScheduleListResourcesFlowUseCase(
            getScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(capture(schedulesFlowSlot))
                } returns flowOf(emptySet())
            }
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase
                .invoke(schedulesLookupFlow = flowOf(SchedulesLookup(expectedSchedulesQuery)))
                .collect()
        }

        val capturedArg = schedulesFlowSlot.captured
        capturedArg.test {
            val actualSchedules = awaitItem()
            assertThat(actualSchedules, sameInstance(expectedSchedulesQuery))

            awaitComplete()
        }
    }

    @Test
    fun `Given scheduleResources, When invoked, Then received UserScheduleListResources`() = runTest {
        val schedules = genSchedules()
        val expectedScheduleResources = schedules.toSet { genScheduleListResource(it.id) }
        val useCase = genGetUserScheduleListResourcesFlowUseCase(
            getScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(expectedScheduleResources)
            }
        )
        useCase.invoke(flowOf(SchedulesLookup(schedules))).test {
            val actualUserScheduleResources = awaitItem()
            assertThat(
                actualUserScheduleResources.toSet { it.schedule },
                equalTo(expectedScheduleResources)
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given selected IDs, When invoked, Then reflects selection state in result`()= runTest {
        val schedules = genSchedules()
        val expectedSelectedIds =
            schedules.shuffledSubset(generateMinSize = schedules.size / 2).toSet { it.id }
        val useCase = genGetUserScheduleListResourcesFlowUseCase(
            getScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(schedules.toSet { genScheduleListResource(it.id) })
            },
            userSelectedSchedulesStore = mockk {
                every { this@mockk.selectedIds } returns MutableStateFlow(expectedSelectedIds)
            }
        )
        useCase.invoke(flowOf(SchedulesLookup(schedules))).test {
            val actualUserScheduleResources = awaitItem()
            assertThat(
                actualUserScheduleResources.filter { it.selected }.toSet { it.schedule.id },
                equalTo(expectedSelectedIds)
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Emits new list only when selection state produces a change`() = runTest {
        val inputSchedule = genSchedule(id = ScheduleId(1))
        val invalidId = ScheduleId(2)
        val firstSelectedIds = setOf(inputSchedule.id, invalidId)
        val secondSelectedIds = setOf(inputSchedule.id)
        val thirdSelectedIds = setOf(invalidId)
        val selectedIdsMutableStateFlow = MutableStateFlow(firstSelectedIds)
        val useCase = genGetUserScheduleListResourcesFlowUseCase(
            getScheduleListResourcesFlow = mockk {
                every {
                    this@mockk.invoke(any())
                } returns flowOf(setOf(genScheduleListResource(inputSchedule.id)))
            },
            userSelectedSchedulesStore = mockk {
                every { this@mockk.selectedIds } returns selectedIdsMutableStateFlow
            }
        )

        val received = mutableListOf<Set<UserScheduleListResource>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase
                .invoke(flowOf(SchedulesLookup(setOf(inputSchedule))))
                .toList(destination = received)
        }

        advanceUntilIdle()
        // assert first emit
        assertThat(received.size, equalTo(1))

        selectedIdsMutableStateFlow.value = secondSelectedIds
        advanceUntilIdle()
        // assert second emit, should not emit
        assertThat(received.size, equalTo(1))

        selectedIdsMutableStateFlow.value = thirdSelectedIds
        advanceUntilIdle()
        // assert second emit, should emit
        assertThat(received.size, equalTo(2))
    }
}

private fun genGetUserScheduleListResourcesFlowUseCase(
    getScheduleListResourcesFlow: GetScheduleListResourcesFlowUseCase,
    userSelectedSchedulesStore: UserSelectedSchedulesStore = mockk {
        every { selectedIds } returns MutableStateFlow(emptySet())
    }
) = GetUserScheduleListResourcesFlowUseCase(
    getScheduleListResourcesFlow = getScheduleListResourcesFlow,
    userSelectedSchedulesStore = userSelectedSchedulesStore
)

private fun genScheduleListResource(id: ScheduleId): ScheduleListResource = ScheduleListResource(
    id = id,
    title = genNonBlankString(),
    note = genNonBlankString(),
    link = genLink(),
    linkMetadata = genLinkMetadata(),
    timing = genScheduleTiming(),
    isComplete = genBoolean(),
    tags = genTags().toList()
)