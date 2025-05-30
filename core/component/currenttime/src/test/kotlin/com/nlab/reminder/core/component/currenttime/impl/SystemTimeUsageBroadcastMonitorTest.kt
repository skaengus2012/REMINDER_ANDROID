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

package com.nlab.reminder.core.component.currenttime.impl

import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class SystemTimeUsageBroadcastMonitorTest {
    @Test
    fun `Given multiple notify events, When collecting event flow, Then receive same number of events`() = runTest {
        val broadcastMonitor = SystemTimeUsageBroadcastMonitor()
        val expectedExecutionCount = genInt(min = 5, max = 10)

        val actualExecutionList = mutableListOf<Unit>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            for (i in broadcastMonitor.event) actualExecutionList.add(i)
        }

        repeat(expectedExecutionCount) {
            broadcastMonitor.notifyEvent()
        }

        assertThat(actualExecutionList.size, equalTo(expectedExecutionCount))
    }
}