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
import kotlinx.datetime.Month
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class MonthTransformsKtTest {
    @Test
    fun `Month func should convert valid REPEAT_MONTH string to Month enum`() {
        assertThat(Month(REPEAT_MONTH_JAN), sameInstance(Month.JANUARY))
        assertThat(Month(REPEAT_MONTH_FEB), sameInstance(Month.FEBRUARY))
        assertThat(Month(REPEAT_MONTH_MAR), sameInstance(Month.MARCH))
        assertThat(Month(REPEAT_MONTH_APR), sameInstance(Month.APRIL))
        assertThat(Month(REPEAT_MONTH_MAY), sameInstance(Month.MAY))
        assertThat(Month(REPEAT_MONTH_JUN), sameInstance(Month.JUNE))
        assertThat(Month(REPEAT_MONTH_JUL), sameInstance(Month.JULY))
        assertThat(Month(REPEAT_MONTH_AUG), sameInstance(Month.AUGUST))
        assertThat(Month(REPEAT_MONTH_SEP), sameInstance(Month.SEPTEMBER))
        assertThat(Month(REPEAT_MONTH_OCT), sameInstance(Month.OCTOBER))
        assertThat(Month(REPEAT_MONTH_NOV), sameInstance(Month.NOVEMBER))
        assertThat(Month(REPEAT_MONTH_DEC), sameInstance(Month.DECEMBER))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Month func should throw required exception for invalid REPEAT_MONTH string`() {
        Month("INVALID_MONTH")
    }

    @Test
    fun `Should convert Month to REPEAT_MONTH string`() {
        assertThat(Month.JANUARY.toRepeatMonth(), equalTo(REPEAT_MONTH_JAN))
        assertThat(Month.FEBRUARY.toRepeatMonth(), equalTo(REPEAT_MONTH_FEB))
        assertThat(Month.MARCH.toRepeatMonth(), equalTo(REPEAT_MONTH_MAR))
        assertThat(Month.APRIL.toRepeatMonth(), equalTo(REPEAT_MONTH_APR))
        assertThat(Month.MAY.toRepeatMonth(), equalTo(REPEAT_MONTH_MAY))
        assertThat(Month.JUNE.toRepeatMonth(), equalTo(REPEAT_MONTH_JUN))
        assertThat(Month.JULY.toRepeatMonth(), equalTo(REPEAT_MONTH_JUL))
        assertThat(Month.AUGUST.toRepeatMonth(), equalTo(REPEAT_MONTH_AUG))
        assertThat(Month.SEPTEMBER.toRepeatMonth(), equalTo(REPEAT_MONTH_SEP))
        assertThat(Month.OCTOBER.toRepeatMonth(), equalTo(REPEAT_MONTH_OCT))
        assertThat(Month.NOVEMBER.toRepeatMonth(), equalTo(REPEAT_MONTH_NOV))
        assertThat(Month.DECEMBER.toRepeatMonth(), equalTo(REPEAT_MONTH_DEC))
    }
}