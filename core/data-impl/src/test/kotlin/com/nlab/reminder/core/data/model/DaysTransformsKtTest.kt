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

import com.nlab.reminder.core.local.database.model.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DaysTransformsKtTest {
    @Test
    fun `Days func should convert valid REPEAT_DAYS string to Days enum`() {
        assertThat(Days(REPEAT_DAYS_SUN), sameInstance(Days.Sun))
        assertThat(Days(REPEAT_DAYS_MON), sameInstance(Days.Mon))
        assertThat(Days(REPEAT_DAYS_TUE), sameInstance(Days.Tue))
        assertThat(Days(REPEAT_DAYS_WED), sameInstance(Days.Wed))
        assertThat(Days(REPEAT_DAYS_THU), sameInstance(Days.Thu))
        assertThat(Days(REPEAT_DAYS_FRI), sameInstance(Days.Fri))
        assertThat(Days(REPEAT_DAYS_SAT), sameInstance(Days.Sat))
        assertThat(Days(REPEAT_DAYS_DAY), sameInstance(Days.Default))
        assertThat(Days(REPEAT_DAYS_WEEKDAY), sameInstance(Days.Weekday))
        assertThat(Days(REPEAT_DAYS_WEEKEND), sameInstance(Days.Weekend))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Days func should throw required exception for invalid REPEAT_DAYS string`() {
        Days("INVALID_DAYS")
    }

    @Test
    fun `Should convert Days to REPEAT_DAYS string`() {
        assertThat(Days.Sun.toRepeatDays(), equalTo(REPEAT_DAYS_SUN))
        assertThat(Days.Mon.toRepeatDays(), equalTo(REPEAT_DAYS_MON))
        assertThat(Days.Tue.toRepeatDays(), equalTo(REPEAT_DAYS_TUE))
        assertThat(Days.Wed.toRepeatDays(), equalTo(REPEAT_DAYS_WED))
        assertThat(Days.Thu.toRepeatDays(), equalTo(REPEAT_DAYS_THU))
        assertThat(Days.Fri.toRepeatDays(), equalTo(REPEAT_DAYS_FRI))
        assertThat(Days.Sat.toRepeatDays(), equalTo(REPEAT_DAYS_SAT))
        assertThat(Days.Default.toRepeatDays(), equalTo(REPEAT_DAYS_DAY))
        assertThat(Days.Weekday.toRepeatDays(), equalTo(REPEAT_DAYS_WEEKDAY))
        assertThat(Days.Weekend.toRepeatDays(), equalTo(REPEAT_DAYS_WEEKEND))
    }
}