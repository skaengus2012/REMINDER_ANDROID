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

package com.nlab.reminder.core.data.repository.impl

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.genTimeSnapshot
import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.network.datasource.TrustedTimeDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class RemoteFirstTimeSnapshotRepositoryTest {
    @Test
    fun `Given trusted time available, When collecting now stream, Then return remote snapshot once`() = runTest {
        val expected = genTimeSnapshot(fromRemote = true)
        val trustedTimeDataSource = mockk<TrustedTimeDataSource> {
            coEvery { getCurrentTime() } returns Result.Success(expected.value)
        }
        val timeSnapshotRepository = genRemoteFirstTimeSnapshotRepository(
            trustedTimeDataSource = trustedTimeDataSource
        )
        timeSnapshotRepository.getNowSnapshotAsStream().test {
            assertThat(awaitItem(), equalTo(expected))
            awaitComplete()
        }
    }

    @Test
    fun `Given trusted time fails, When collecting now stream, Then fallback snapshot is emitted`() = runTest {
        val expected = genTimeSnapshot(fromRemote = false)
        val trustedTimeDataSource = mockk<TrustedTimeDataSource> {
            coEvery { getCurrentTime() } returns Result.Failure(IllegalStateException())
        }
        val fallbackSnapshotRepository = mockk<TimeSnapshotRepository> {
            coEvery { getNowSnapshotAsStream() } returns flowOf(expected)
        }
        val timeSnapshotRepository = genRemoteFirstTimeSnapshotRepository(
            trustedTimeDataSource = trustedTimeDataSource,
            fallbackSnapshotRepository = fallbackSnapshotRepository
        )
        timeSnapshotRepository.getNowSnapshotAsStream().test {
            assertThat(awaitItem(), equalTo(expected))
            awaitComplete()
        }
    }
}

private fun genRemoteFirstTimeSnapshotRepository(
    trustedTimeDataSource: TrustedTimeDataSource,
    fallbackSnapshotRepository: TimeSnapshotRepository = mockk()
) = RemoteFirstTimeSnapshotRepository(
    trustedTimeDataSource = trustedTimeDataSource,
    fallbackSnapshotRepository = fallbackSnapshotRepository
)