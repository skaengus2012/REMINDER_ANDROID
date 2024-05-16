/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleSelectedIdRepository
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class MapToScheduleItemsUseCaseTest {
    @Test
    fun `Given schedules, Then return scheduleItem including schedules`() = runTest {
        val schedules = genSchedules()
        val useCase = genMapToScheduleItemsUseCase()
        assertThat(
            useCase.expectedSnapshot(schedules).map { it.schedule },
            equalTo(schedules)
        )
    }

    @Test
    fun `Given complete marked, Then scheduleItem has marked`() = runTest {
        val schedule = genSchedule(isComplete = genBoolean())
        val useCase = genMapToScheduleItemsUseCase(
            completeMarkRepository = mock {
                whenever(mock.getStream()) doReturn MutableStateFlow(mapOf(schedule.id to schedule.isComplete.not()))
            }
        )
        assertThat(
            useCase.expectedSnapshot(listOf(schedule))
                .first()
                .isCompleteMarked,
            equalTo(schedule.isComplete.not())
        )
    }

    @Test
    fun `Given linkMetadataTable, Then scheduleItem has linkMetadata`() = runTest {
        val link = genLink()
        val linkMetadata = genLinkMetadata()
        val schedule = genSchedule(link = link)
        val useCase = genMapToScheduleItemsUseCase(
            linkMetadataTableRepository = mock {
                whenever(mock.getStream()) doReturn flowOf(LinkMetadataTable(mapOf(link to linkMetadata)))
            }
        )
        assertThat(
            useCase.expectedSnapshot(listOf(schedule))
                .first()
                .linkMetadata,
            equalTo(linkMetadata)
        )
    }

    @Test
    fun `Given schedule link was empty, Then scheduleItem linkMetadata was null`() = runTest {
        val schedule = genSchedule(link = Link.EMPTY)
        val useCase = genMapToScheduleItemsUseCase()
        assertThat(
            useCase.expectedSnapshot(listOf(schedule))
                .first()
                .linkMetadata,
            equalTo(null)
        )
    }

    @Test
    fun `Given selected id set, Then schedule included id set is selected`() = runTest {
        val schedules = List(genInt(min = 2, max = 10)) {
            genSchedule(scheduleId = genScheduleId(it.toLong()))
        }
        val selectedSchedule = schedules.last()
        val useCase = genMapToScheduleItemsUseCase(
            selectedIdRepository = mock {
                whenever(mock.getStream()) doReturn MutableStateFlow(setOf(selectedSchedule.id))
            }
        )
        assert(useCase.expectedSnapshot(schedules).find { it.id == selectedSchedule.id }!!.isSelected,)
    }

    @Test
    fun `Given selected id set, Then schedule not included id set is unselected`() = runTest {
        val schedules = List(genInt(min = 2, max = 10)) {
            genSchedule(scheduleId = genScheduleId(it.toLong()))
        }
        val selectedSchedule = schedules.last()
        val useCase = genMapToScheduleItemsUseCase(
            selectedIdRepository = mock {
                whenever(mock.getStream()) doReturn MutableStateFlow(setOf(selectedSchedule.id))
            }
        )
        assert(useCase.expectedSnapshot(schedules).find { it.id != selectedSchedule.id }!!.isSelected.not())
    }
}

private fun genMapToScheduleItemsUseCase(
    completeMarkRepository: ScheduleCompleteMarkRepository = mock {
        whenever(mock.getStream()) doReturn MutableStateFlow(emptyMap())
    },
    linkMetadataTableRepository: LinkMetadataTableRepository = mock {
        whenever(mock.getStream()) doReturn MutableStateFlow(LinkMetadataTable(emptyMap())) // TODO stateFlow 로 바꾸자..
    },
    selectedIdRepository: ScheduleSelectedIdRepository = mock {
        whenever(mock.getStream()) doReturn MutableStateFlow(emptySet())
    }
) = MapToScheduleElementsUseCase(
    completeMarkRepository,
    linkMetadataTableRepository,
    selectedIdRepository,
    dispatcher = Dispatchers.Unconfined
)

private suspend fun MapToScheduleElementsUseCase.expectedSnapshot(
    schedules: List<Schedule>
): List<ScheduleElement> = invoke(flowOf(schedules))
    .take(1)
    .first()