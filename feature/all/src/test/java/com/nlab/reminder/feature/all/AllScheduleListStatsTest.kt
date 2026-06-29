/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.feature.all

import com.nlab.reminder.core.component.schedulelist.ScheduleListStats
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.genIntGreaterThanZero
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class AllScheduleListStatsTest {

    @Test
    fun `Given completedShown and stats, When create, Then mapping properties`() {
        val completedShown = genBoolean()
        val completedCount = genInt(min = 0, max = 100)
        val selectedCount = genIntGreaterThanZero(max = 100)
        val stats = ScheduleListStats(
            completedCount = completedCount.toNonNegativeInt(),
            selectedCount = selectedCount.toNonNegativeInt()
        )

        val result = AllScheduleListStats(
            completedShown = completedShown,
            stats = stats
        )

        assertThat(result.completedShown, equalTo(completedShown))
        assertThat(result.completedCount, equalTo(completedCount.toNonNegativeInt()))
        assertThat(result.selectedCount, equalTo(selectedCount.toPositiveInt()))
    }

    @Test
    fun `Given selectedCount is zero, When create, Then selectedCount is null`() {
        val stats = ScheduleListStats(
            completedCount = genInt(min = 0, max = 100).toNonNegativeInt(),
            selectedCount = 0.toNonNegativeInt()
        )

        val result = AllScheduleListStats(
            completedShown = genBoolean(),
            stats = stats
        )

        assertThat(result.selectedCount, equalTo(null))
    }
}
