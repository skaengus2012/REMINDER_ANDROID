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

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class DaysOfMonthExtKtTest {
    @Test
    fun testDaysOfMonthToRawValue() {
        assertThat(DaysOfMonth.DAY_1.rawValue, equalTo(1))
        assertThat(DaysOfMonth.DAY_2.rawValue, equalTo(2))
        assertThat(DaysOfMonth.DAY_3.rawValue, equalTo(3))
        assertThat(DaysOfMonth.DAY_4.rawValue, equalTo(4))
        assertThat(DaysOfMonth.DAY_5.rawValue, equalTo(5))
        assertThat(DaysOfMonth.DAY_6.rawValue, equalTo(6))
        assertThat(DaysOfMonth.DAY_7.rawValue, equalTo(7))
        assertThat(DaysOfMonth.DAY_8.rawValue, equalTo(8))
        assertThat(DaysOfMonth.DAY_9.rawValue, equalTo(9))
        assertThat(DaysOfMonth.DAY_10.rawValue, equalTo(10))
        assertThat(DaysOfMonth.DAY_11.rawValue, equalTo(11))
        assertThat(DaysOfMonth.DAY_12.rawValue, equalTo(12))
        assertThat(DaysOfMonth.DAY_13.rawValue, equalTo(13))
        assertThat(DaysOfMonth.DAY_14.rawValue, equalTo(14))
        assertThat(DaysOfMonth.DAY_15.rawValue, equalTo(15))
        assertThat(DaysOfMonth.DAY_16.rawValue, equalTo(16))
        assertThat(DaysOfMonth.DAY_17.rawValue, equalTo(17))
        assertThat(DaysOfMonth.DAY_18.rawValue, equalTo(18))
        assertThat(DaysOfMonth.DAY_19.rawValue, equalTo(19))
        assertThat(DaysOfMonth.DAY_20.rawValue, equalTo(20))
        assertThat(DaysOfMonth.DAY_21.rawValue, equalTo(21))
        assertThat(DaysOfMonth.DAY_22.rawValue, equalTo(22))
        assertThat(DaysOfMonth.DAY_23.rawValue, equalTo(23))
        assertThat(DaysOfMonth.DAY_24.rawValue, equalTo(24))
        assertThat(DaysOfMonth.DAY_25.rawValue, equalTo(25))
        assertThat(DaysOfMonth.DAY_26.rawValue, equalTo(26))
        assertThat(DaysOfMonth.DAY_27.rawValue, equalTo(27))
        assertThat(DaysOfMonth.DAY_28.rawValue, equalTo(28))
        assertThat(DaysOfMonth.DAY_29.rawValue, equalTo(29))
        assertThat(DaysOfMonth.DAY_30.rawValue, equalTo(30))
        assertThat(DaysOfMonth.DAY_31.rawValue, equalTo(31))
    }
}