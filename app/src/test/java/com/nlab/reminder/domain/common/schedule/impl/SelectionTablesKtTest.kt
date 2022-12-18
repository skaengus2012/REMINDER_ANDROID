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
import com.nlab.reminder.domain.common.schedule.ScheduleId
import com.nlab.reminder.domain.common.schedule.SelectionTable
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBoolean
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
class SelectionTablesKtTest {
    @Test
    fun `get selected when selectedTable hit`() {
        val expectSchedule: Schedule = genSchedule()
        val expectSelected: Boolean = genBoolean()
        val selectionTable: SelectionTable = mapOf(expectSchedule.id to expectSelected)

        assertThat(selectionTable.isSelected(expectSchedule), equalTo(expectSelected))
    }

    @Test
    fun `get false when selectTable hit failed`() {
        val selectionTable: SelectionTable = emptyMap()
        assertThat(selectionTable.isSelected(genSchedule()), equalTo(false))
    }
}