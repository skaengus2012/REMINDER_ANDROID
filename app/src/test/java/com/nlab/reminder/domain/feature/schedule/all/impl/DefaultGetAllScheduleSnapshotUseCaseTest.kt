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

package com.nlab.reminder.domain.feature.schedule.all.impl

import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.schedule.visibleconfig.CompletedScheduleShownRepository
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataTableRepository
import com.nlab.reminder.domain.common.util.link.genLinkMetadata
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleSnapshot
import com.nlab.reminder.domain.feature.schedule.all.genAllScheduleSnapshot
import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGetAllScheduleSnapshotUseCaseTest {
    @Test
    fun `find all schedules when doneScheduleShown was true`() = runTest {
        testFindTemplate(
            isDoneScheduleShown = true,
            setupMock = { scheduleRepository, expectSchedules ->
                whenever(scheduleRepository.get(GetRequest.All)) doReturn flowOf(expectSchedules)
            }
        )
    }

    @Test
    fun `find not complete schedules when doneScheduleShown was false`() = runTest {
        testFindTemplate(
            isDoneScheduleShown = false,
            setupMock = { scheduleRepository, expectSchedules ->
                whenever(
                    scheduleRepository.get(GetRequest.ByComplete(isComplete = false))
                ) doReturn flowOf(expectSchedules)
            }
        )
    }

    private suspend fun testFindTemplate(
        isDoneScheduleShown: Boolean,
        setupMock: (ScheduleRepository, schedules: List<Schedule>) -> Unit,
    ) {
        val expectedSchedules: List<Schedule> = genSchedules()
        val expectedUiStates: List<ScheduleUiState> = genScheduleUiStates(expectedSchedules)
        val getAllScheduleSnapshotUseCase = genDefaultGetAllScheduleSnapshotUseCase(
            scheduleRepository = mock { setupMock(mock, expectedSchedules) },
            completedScheduleShownRepository = mock { whenever(mock.get()) doReturn flowOf(isDoneScheduleShown) },
            scheduleUiStateFlowFactory = object : ScheduleUiStateFlowFactory {
                override fun with(schedules: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> =
                    schedules.filter { it == expectedSchedules }.map { expectedUiStates }
            },
        )
        val snapshot: AllScheduleSnapshot =
            getAllScheduleSnapshotUseCase().take(1).first()
        assertThat(
            snapshot, equalTo(
                genAllScheduleSnapshot(
                    uiStates = expectedUiStates,
                    isCompletedScheduleShown = isDoneScheduleShown
                )
            )
        )
    }

    @Test
    fun `setLinks when scheduleUiState received`() = runTest {
        val expectedUiStates: List<ScheduleUiState> = genScheduleUiStates(genSchedules(link = genBothify()))
        val linkMetadataTableRepository: LinkMetadataTableRepository = mock {
            whenever(mock.getStream()) doReturn MutableStateFlow(emptyMap())
        }
        val getAllScheduleSnapshotUseCase = genDefaultGetAllScheduleSnapshotUseCase(
            linkMetadataTableRepository = linkMetadataTableRepository,
            scheduleUiStateFlowFactory = object : ScheduleUiStateFlowFactory {
                override fun with(schedules: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> =
                    flowOf(expectedUiStates)
            },
        )

        getAllScheduleSnapshotUseCase()
            .take(1)
            .first()
        verify(linkMetadataTableRepository, once()).setLinks(expectedUiStates.map { it.link })
    }

    @Test
    fun `combine with linkMetadataTable when notified new links`() = runTest {
        val link: String = genBothify()
        val scheduleUiState: ScheduleUiState = genScheduleUiState(genSchedule(link = link))
        val expectedLinkMetadata: LinkMetadata = genLinkMetadata()
        val getAllScheduleSnapshotUseCase = genDefaultGetAllScheduleSnapshotUseCase(
            linkMetadataTableRepository = mock {
                whenever(mock.getStream()) doReturn MutableStateFlow(mapOf(link to expectedLinkMetadata))
            },
            scheduleUiStateFlowFactory = object : ScheduleUiStateFlowFactory {
                override fun with(schedules: Flow<List<Schedule>>): Flow<List<ScheduleUiState>> = flowOf(
                    listOf(scheduleUiState)
                )
            },
        )

        val uiState: ScheduleUiState =
            getAllScheduleSnapshotUseCase()
                .mapNotNull { it.scheduleUiStates.firstOrNull() }
                .filter { uiState -> uiState.linkMetadata != LinkMetadata.Empty }
                .take(1)
                .first()
        assertThat(uiState.linkMetadata, equalTo(expectedLinkMetadata))
    }

    private fun genDefaultGetAllScheduleSnapshotUseCase(
        scheduleRepository: ScheduleRepository = mock {
            whenever(mock.get(any())) doReturn flowOf(emptyList())
        },
        linkMetadataTableRepository: LinkMetadataTableRepository = mock {
            whenever(mock.getStream()) doReturn MutableStateFlow(emptyMap())
        },
        completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
            whenever(mock.get()) doReturn flowOf(genBoolean())
        },
        scheduleUiStateFlowFactory: ScheduleUiStateFlowFactory = mock {
            whenever(mock.with(any())) doReturn flowOf(emptyList())
        }
    ): DefaultGetAllScheduleSnapshotUseCase = DefaultGetAllScheduleSnapshotUseCase(
        scheduleRepository,
        linkMetadataTableRepository,
        completedScheduleShownRepository,
        scheduleUiStateFlowFactory
    )
}