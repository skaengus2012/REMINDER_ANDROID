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
    fun `Given trusted time available, When collecting stream, Then return remote snapshot once`() = runTest {
        val expected = genTimeSnapshot(fromRemote = true)
        val trustedTimeDataSource = mockk<TrustedTimeDataSource> {
            coEvery { getCurrentTime() } returns Result.Success(expected.value)
        }
        val timeSnapshotRepository = genRemoteFirstTimeSnapshotRepository(
            trustedTimeDataSource = trustedTimeDataSource
        )
        timeSnapshotRepository.getAsStream().test {
            assertThat(awaitItem(), equalTo(expected))
            awaitComplete()
        }
    }

    @Test
    fun `Given trusted time fails, When collecting stream, Then fallback snapshot is emitted`() = runTest {
        val expected = genTimeSnapshot(fromRemote = false)
        val trustedTimeDataSource = mockk<TrustedTimeDataSource> {
            coEvery { getCurrentTime() } returns Result.Failure(IllegalStateException())
        }
        val fallbackSnapshotRepository = mockk<TimeSnapshotRepository> {
            coEvery { getAsStream() } returns flowOf(expected)
        }
        val timeSnapshotRepository = genRemoteFirstTimeSnapshotRepository(
            trustedTimeDataSource = trustedTimeDataSource,
            fallbackSnapshotRepository = fallbackSnapshotRepository
        )
        timeSnapshotRepository.getAsStream().test {
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