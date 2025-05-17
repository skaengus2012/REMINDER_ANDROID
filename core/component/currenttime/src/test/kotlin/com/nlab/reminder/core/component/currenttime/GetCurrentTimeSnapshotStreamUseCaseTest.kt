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

package com.nlab.reminder.core.component.currenttime

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.genTimeSnapshot
import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import kotlinx.datetime.Clock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class GetCurrentTimeSnapshotStreamUseCaseTest {
    @Test
    fun `Given timeSnapshotStream, When collected, Then emit instant value once`() = runTest {
        val expectedInstant = Clock.System.now()
        val timeSnapshotRepository: TimeSnapshotRepository = mockk {
            every { getAsStream() } returns flowOf(genTimeSnapshot(value = expectedInstant))
        }
        val useCase = genGetCurrentTimeSnapshotStreamUseCase(timeSnapshotRepository = timeSnapshotRepository)
        useCase().test {
            assertThat(awaitItem(), equalTo(expectedInstant))
            awaitComplete()
        }
    }

    @Test
    fun `Given remote snapshot, When collected, Then do not notify systemTimeUsage`() = runTest {
        val timeSnapshotRepository: TimeSnapshotRepository = mockk {
            every { getAsStream() } returns flowOf(genTimeSnapshot(fromRemote = true))
        }
        val systemTimeUsageBroadcast: SystemTimeUsageBroadcast = mockk(relaxed = true)
        val useCase = genGetCurrentTimeSnapshotStreamUseCase(
            timeSnapshotRepository = timeSnapshotRepository,
            systemTimeUsageBroadcast = systemTimeUsageBroadcast
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }
        coVerify(exactly = 0) {
            systemTimeUsageBroadcast.notifyEvent()
        }
    }

    @Test
    fun `Given local snapshot, When collected, Then notify systemTimeUsage once`() = runTest {
        val timeSnapshotRepository: TimeSnapshotRepository = mockk {
            every { getAsStream() } returns flowOf(genTimeSnapshot(fromRemote = false))
        }
        val systemTimeUsageBroadcast: SystemTimeUsageBroadcast = mockk(relaxed = true)
        val useCase = genGetCurrentTimeSnapshotStreamUseCase(
            timeSnapshotRepository = timeSnapshotRepository,
            systemTimeUsageBroadcast = systemTimeUsageBroadcast
        )
        backgroundScope.launch(unconfinedTestDispatcher()) {
            useCase.invoke().collect()
        }
        coVerify(exactly = 1) {
            systemTimeUsageBroadcast.notifyEvent()
        }
    }
}

private fun genGetCurrentTimeSnapshotStreamUseCase(
    timeSnapshotRepository: TimeSnapshotRepository = mockk(),
    systemTimeUsageBroadcast: SystemTimeUsageBroadcast = mockk(relaxed = true)
) = GetCurrentTimeSnapshotStreamUseCase(
    timeSnapshotRepository = timeSnapshotRepository,
    systemTimeUsageBroadcast = systemTimeUsageBroadcast
)