/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genLinkMetadataTable
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.statekit.middleware.epic.scenario
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genInt
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class AllScheduleEpicTest {
    @Test
    fun `Loaded only not completed schedules from repository`() {
        val isCompletedSchedulesShown = false
        val schedules = List(genInt(min = 5, max = 10)) { index ->
            genSchedule(scheduleId = ScheduleId(index.toLong()), isComplete = false)
        }

        genAllScheduleEpic(
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.ByComplete(isComplete = false)))
                    .doReturn(flowOf(schedules.toImmutableList()))
            },
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            })
            .scenario()
            .action(AllScheduleAction.ScheduleLoaded(schedules.toImmutableList(), isCompletedSchedulesShown))
            .verify()
    }

    @Test
    fun `Completed Schedule loaded from repository`() {
        val isCompletedSchedulesShown = true
        val schedules = genSchedules()

        genAllScheduleEpic(
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.All)) doReturn flowOf(schedules.toImmutableList())
            },
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            })
            .scenario()
            .action(AllScheduleAction.ScheduleLoaded(schedules.toImmutableList(), isCompletedSchedulesShown))
            .verify()
    }

    @Test
    fun `LinkMetadataTable loaded from repository`() {
        val linkMetadataTable = genLinkMetadataTable()
        genAllScheduleEpic(
            linkMetadataTableRepository = mock {
                whenever(mock.get()) doReturn flowOf(linkMetadataTable)
            })
            .scenario()
            .action(AllScheduleAction.LinkMetadataLoaded(linkMetadataTable))
            .verify()
    }

    @Test
    fun `CompleteMark loaded from repository`() {
        val completeMark = persistentMapOf(genScheduleId() to genBoolean(),)
        genAllScheduleEpic(
            completeMarkRepository = mock {
                whenever(mock.get()) doReturn MutableStateFlow(completeMark)
            })
            .scenario()
            .action(AllScheduleAction.CompleteMarkLoaded(completeMark))
            .verify()
    }
}

private fun genAllScheduleEpic(
    scheduleRepository: ScheduleRepository = mock {
        whenever(mock.getAsStream(ScheduleGetStreamRequest.All)) doReturn emptyFlow()
    },
    completeMarkRepository: ScheduleCompleteMarkRepository = mock {
        whenever(mock.get()) doReturn MutableStateFlow(persistentMapOf())
    },
    linkMetadataTableRepository: LinkMetadataTableRepository = mock {
        whenever(mock.get()) doReturn emptyFlow()
    },
    completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
        whenever(mock.getAsStream()) doReturn emptyFlow()
    },
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
): AllScheduleEpic = AllScheduleEpic(
    scheduleRepository,
    completeMarkRepository,
    linkMetadataTableRepository,
    completedScheduleShownRepository,
    dispatcher
)