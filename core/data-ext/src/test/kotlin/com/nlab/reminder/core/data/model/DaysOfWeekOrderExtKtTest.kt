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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class DaysOfWeekOrderExtKtTest {
    @Test
    fun testDaysOfWeekOrderToResourceId() {
        assertThat(DaysOfWeekOrder.First.resourceId, equalTo(StringIds.first))
        assertThat(DaysOfWeekOrder.Second.resourceId, equalTo(StringIds.second))
        assertThat(DaysOfWeekOrder.Third.resourceId, equalTo(StringIds.third))
        assertThat(DaysOfWeekOrder.Fourth.resourceId, equalTo(StringIds.fourth))
        assertThat(DaysOfWeekOrder.Fifth.resourceId, equalTo(StringIds.fifth))
        assertThat(DaysOfWeekOrder.Last.resourceId, equalTo(StringIds.last))
    }
}