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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_FRI
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_MON
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SAT
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_SUN
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_THU
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_TUE
import com.nlab.reminder.core.local.database.entity.REPEAT_WEEK_WED
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlinx.datetime.DayOfWeek
import org.hamcrest.CoreMatchers.equalTo

/**
 * @author Doohyun
 */
class DaysOfWeekTransformsKtTest {
    @Test
    fun `DayOfWeek func should convert valid REPEAT_WEEK string to DayOfWeek`() {
        assertThat(DayOfWeek(REPEAT_WEEK_SUN), sameInstance(DayOfWeek.SUNDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_MON), sameInstance(DayOfWeek.MONDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_TUE), sameInstance(DayOfWeek.TUESDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_WED), sameInstance(DayOfWeek.WEDNESDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_THU), sameInstance(DayOfWeek.THURSDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_FRI), sameInstance(DayOfWeek.FRIDAY))
        assertThat(DayOfWeek(REPEAT_WEEK_SAT), sameInstance(DayOfWeek.SATURDAY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `DayOfWeek func should throw required exception for invalid REAT_WEEKS string`() {
        DayOfWeek("INVALID_REPEAT_WEEK")
    }

    @Test
    fun `Should convert DayOfWeek to REPEAT_WEEK string`() {
        assertThat(DayOfWeek.SUNDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_SUN))
        assertThat(DayOfWeek.MONDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_MON))
        assertThat(DayOfWeek.TUESDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_TUE))
        assertThat(DayOfWeek.WEDNESDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_WED))
        assertThat(DayOfWeek.THURSDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_THU))
        assertThat(DayOfWeek.FRIDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_FRI))
        assertThat(DayOfWeek.SATURDAY.toRepeatWeek(), equalTo(REPEAT_WEEK_SAT))
    }
}