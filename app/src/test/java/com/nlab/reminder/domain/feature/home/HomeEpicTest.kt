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

import com.nlab.reminder.domain.common.data.model.genTags
import com.nlab.statekit.test.tester
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
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
        val todaySchedulesCount = genLong()
        val timetableSchedulesCount = genLong()
        val allSchedulesCount = genLong()
        val tags = genTags(count = genInt(min = 1, max = 3))

        HomeEpic(
            tagRepository = mock { whenever(mock.get()) doReturn flowOf(tags) },
            scheduleRepository = mock {
                whenever(mock.getTodaySchedulesCount()) doReturn flowOf(todaySchedulesCount)
                whenever(mock.getTimetableSchedulesCount()) doReturn flowOf(timetableSchedulesCount)
                whenever(mock.getAllSchedulesCount()) doReturn flowOf(allSchedulesCount)
            })
            .tester()
            .expectedAction(
                HomeAction.SummaryLoaded(
                    todaySchedulesCount = todaySchedulesCount,
                    timetableSchedulesCount = timetableSchedulesCount,
                    allSchedulesCount = allSchedulesCount,
                    tags = tags
                )
            )
            .verify()
    }
}