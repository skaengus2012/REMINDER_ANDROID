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
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.core.schedule.genScheduleItem
import com.nlab.statekit.middleware.epic.scenario
import com.nlab.testkit.genInt
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
        val scheduleItems = schedules.map { genScheduleItem(it) }
        val schedulesFlow = flowOf(schedules)

        AllScheduleEpic(
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            },
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.ByComplete(isComplete = false)))
                    .doReturn(schedulesFlow)
            },
            mapToScheduleItems = mock {
                whenever(mock.invoke(schedulesFlow)) doReturn flowOf(scheduleItems)
            })
            .scenario()
            .action(AllScheduleAction.ScheduleItemsLoaded(scheduleItems, isCompletedSchedulesShown))
            .verify()
    }

    @Test
    fun `Completed Schedule loaded from repository`() {
        val isCompletedSchedulesShown = true
        val schedules = genSchedules()
        val scheduleItems = schedules.map { genScheduleItem(it) }
        val schedulesFlow = flowOf(schedules)

        AllScheduleEpic(
            completedScheduleShownRepository = mock {
                whenever(mock.getAsStream()) doReturn flowOf(isCompletedSchedulesShown)
            },
            scheduleRepository = mock {
                whenever(mock.getAsStream(ScheduleGetStreamRequest.All)) doReturn schedulesFlow
            },
            mapToScheduleItems = mock {
                whenever(mock.invoke(schedulesFlow)) doReturn flowOf(scheduleItems)
            })
            .scenario()
            .action(AllScheduleAction.ScheduleItemsLoaded(scheduleItems, isCompletedSchedulesShown))
            .verify()
    }
}