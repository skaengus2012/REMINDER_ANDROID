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

import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIFTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FIRST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_FOURTH
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_LAST
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_SECOND
import com.nlab.reminder.core.local.database.entity.REPEAT_DAY_ORDER_THIRD
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class DaysOfWeekOrderTransformsKtTest {
    @Test
    fun `DaysOfWeekOrder func should convert REPEAT_DAY_ORDER string to DaysOfWeekOrder enum`() {
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_FIRST), sameInstance(DaysOfWeekOrder.First))
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_SECOND), sameInstance(DaysOfWeekOrder.Second))
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_THIRD), sameInstance(DaysOfWeekOrder.Third))
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_FOURTH), sameInstance(DaysOfWeekOrder.Fourth))
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_FIFTH), sameInstance(DaysOfWeekOrder.Fifth))
        assertThat(DaysOfWeekOrder(REPEAT_DAY_ORDER_LAST), sameInstance(DaysOfWeekOrder.Last))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `DaysOfWeekOrder func should throw required exception for invalid REPEAT_DAY_ORDER string`() {
        DaysOfWeekOrder("INVALID_REPEAT_DAY_ORDER")
    }

    @Test
    fun `Should convert Days to REPEAT_DAYS string`() {
        assertThat(DaysOfWeekOrder.First.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_FIRST))
        assertThat(DaysOfWeekOrder.Second.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_SECOND))
        assertThat(DaysOfWeekOrder.Third.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_THIRD))
        assertThat(DaysOfWeekOrder.Fourth.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_FOURTH))
        assertThat(DaysOfWeekOrder.Fifth.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_FIFTH))
        assertThat(DaysOfWeekOrder.Last.toRepeatDayOrder(), equalTo(REPEAT_DAY_ORDER_LAST))
    }
}