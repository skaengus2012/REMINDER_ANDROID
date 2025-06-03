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

import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.Month
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class MonthExtKtTest {
    @Test
    fun testMonthToFullNameResourceId() {
        assertThat(Month.JANUARY.fullNameResourceId, equalTo(StringIds.january))
        assertThat(Month.FEBRUARY.fullNameResourceId, equalTo(StringIds.february))
        assertThat(Month.MARCH.fullNameResourceId, equalTo(StringIds.march))
        assertThat(Month.APRIL.fullNameResourceId, equalTo(StringIds.april))
        assertThat(Month.MAY.fullNameResourceId, equalTo(StringIds.may))
        assertThat(Month.JUNE.fullNameResourceId, equalTo(StringIds.june))
        assertThat(Month.JULY.fullNameResourceId, equalTo(StringIds.july))
        assertThat(Month.AUGUST.fullNameResourceId, equalTo(StringIds.august))
        assertThat(Month.SEPTEMBER.fullNameResourceId, equalTo(StringIds.september))
        assertThat(Month.OCTOBER.fullNameResourceId, equalTo(StringIds.october))
        assertThat(Month.NOVEMBER.fullNameResourceId, equalTo(StringIds.november))
        assertThat(Month.DECEMBER.fullNameResourceId, equalTo(StringIds.december))
    }

    @Test
    fun testMonthToShortNameResourceId() {
        assertThat(Month.JANUARY.shortNameResourceId, equalTo(StringIds.january_short))
        assertThat(Month.FEBRUARY.shortNameResourceId, equalTo(StringIds.february_short))
        assertThat(Month.MARCH.shortNameResourceId, equalTo(StringIds.march_short))
        assertThat(Month.APRIL.shortNameResourceId, equalTo(StringIds.april_short))
        assertThat(Month.MAY.shortNameResourceId, equalTo(StringIds.may_short))
        assertThat(Month.JUNE.shortNameResourceId, equalTo(StringIds.june_short))
        assertThat(Month.JULY.shortNameResourceId, equalTo(StringIds.july_short))
        assertThat(Month.AUGUST.shortNameResourceId, equalTo(StringIds.august_short))
        assertThat(Month.SEPTEMBER.shortNameResourceId, equalTo(StringIds.september_short))
        assertThat(Month.OCTOBER.shortNameResourceId, equalTo(StringIds.october_short))
        assertThat(Month.NOVEMBER.shortNameResourceId, equalTo(StringIds.november_short))
        assertThat(Month.DECEMBER.shortNameResourceId, equalTo(StringIds.december_short))
    }
}