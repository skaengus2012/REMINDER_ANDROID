/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genBoolean
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultScheduleUiStateFlowFactoryTest {
    @Test
    fun `schedule combined with complete mark`() = runTest {
        val expectCompleteMarked: Boolean = genBoolean()
        val schedule: Schedule = genSchedule(isComplete = expectCompleteMarked.not())
        val completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(
                mapOf(schedule.id to genCompleteMark(isComplete = expectCompleteMarked))
            )
        }
        testTemplate(schedule, completeMarkRepository = completeMarkRepository) { scheduleUiState ->
            scheduleUiState.copy(
                isCompleteMarked = expectCompleteMarked,
                isSelected = false
            )
        }
    }

    @Test
    fun `schedule combined with selectionTables`() = runTest {
        val expectSelected: Boolean = genBoolean()
        val schedule: Schedule = genSchedule()
        val selectionRepository: SelectionRepository = mock {
            whenever(mock.selectionTableStream()) doReturn MutableStateFlow(
                mapOf(schedule.id to expectSelected)
            )
        }
        testTemplate(schedule, selectionRepository = selectionRepository) { scheduleUiState ->
            scheduleUiState.copy(
                isCompleteMarked = schedule.isComplete,
                isSelected = expectSelected
            )
        }
    }

    private suspend fun testTemplate(
        schedule: Schedule = genSchedule(),
        completeMarkRepository: CompleteMarkRepository = mock {
            whenever(mock.get()) doReturn MutableStateFlow(emptyMap())
        },
        selectionRepository: SelectionRepository = mock {
            whenever(mock.selectionTableStream()) doReturn MutableStateFlow(emptyMap())
        },
        decorateExpectedScheduleUiState: (ScheduleUiState) -> ScheduleUiState = { it }
    ) {
        val scheduleUiStateFlowFactory = DefaultScheduleUiStateFlowFactory(
            completeMarkRepository,
            selectionRepository
        )
        val acc: List<ScheduleUiState> =
            scheduleUiStateFlowFactory
                .with(flowOf(listOf(schedule)))
                .take(1)
                .first()
        assertThat(
            acc, equalTo(listOf(genScheduleUiState(schedule)).map(decorateExpectedScheduleUiState))
        )
    }
}