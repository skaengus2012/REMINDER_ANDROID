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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.statekit.middleware.epic.scenario
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class HomeEpicTest {
    @Test
    fun `Loaded summary from repository`() {
        val uiState = genHomeUiStateSuccess()

        HomeEpic(
            tagRepository = mock { whenever(mock.getTagsAsStream(GetTagQuery.All)) doReturn flowOf(uiState.tags) },
            scheduleRepository = mock {
                whenever(mock.getTodaySchedulesCount()) doReturn flowOf(uiState.todayScheduleCount)
                whenever(mock.getTimetableSchedulesCount()) doReturn flowOf(uiState.timetableScheduleCount)
                whenever(mock.getAllSchedulesCount()) doReturn flowOf(uiState.allScheduleCount)
            })
            .scenario()
            .action(
                HomeAction.SummaryLoaded(
                    todaySchedulesCount = uiState.todayScheduleCount,
                    timetableSchedulesCount = uiState.timetableScheduleCount,
                    allSchedulesCount = uiState.allScheduleCount,
                    tags = uiState.tags
                )
            )
            .verify()
    }
}