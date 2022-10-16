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

import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.genCompleteMark
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
class ScheduleUiStatesKtTest {
    @Test
    fun `created scheduleUiState with isComplete of completeMark`() {
        val schedule: Schedule = genSchedule()
        val completeMarkSnapshot = mapOf(schedule.id() to genCompleteMark(isComplete = schedule.isComplete.not()))
        val expectedScheduleUiState = ScheduleUiState(schedule, completeMarkSnapshot)
        assertThat(
            expectedScheduleUiState,
            equalTo(ScheduleUiState(schedule, schedule.isComplete.not()))
        )
    }

    @Test
    fun `created scheduleUiState with owners complete when snapshot was empty`() {
        val schedule: Schedule = genSchedule()
        val expectedScheduleUiState = ScheduleUiState(schedule, emptyMap())
        assertThat(
            expectedScheduleUiState,
            equalTo(ScheduleUiState(schedule, schedule.isComplete))
        )
    }
}