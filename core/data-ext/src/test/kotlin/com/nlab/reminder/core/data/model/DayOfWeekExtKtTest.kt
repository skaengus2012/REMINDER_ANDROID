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
import kotlinx.datetime.DayOfWeek
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class DayOfWeekExtKtTest {
    @Test
    fun testDayOfWeekToResourceId() {
        assertThat(DayOfWeek.SUNDAY.resourceId, equalTo(StringIds.sunday))
        assertThat(DayOfWeek.MONDAY.resourceId, equalTo(StringIds.monday))
        assertThat(DayOfWeek.TUESDAY.resourceId, equalTo(StringIds.tuesday))
        assertThat(DayOfWeek.WEDNESDAY.resourceId, equalTo(StringIds.wednesday))
        assertThat(DayOfWeek.THURSDAY.resourceId, equalTo(StringIds.thursday))
        assertThat(DayOfWeek.FRIDAY.resourceId, equalTo(StringIds.friday))
        assertThat(DayOfWeek.SATURDAY.resourceId, equalTo(StringIds.saturday))
    }
}