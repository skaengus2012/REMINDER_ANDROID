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

package com.nlab.reminder.core.component.schedule.infra

import android.content.Context
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import com.nlab.reminder.core.component.schedule.ScheduleJobResult
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author Doohyun
 */
class RegisterScheduleDeletionJobUseCaseImplTest {
    private val context: Context = mockk(relaxed = true)
    private val workManager: WorkManagerImpl = mockk(relaxed = true)

    @Before
    fun setUp() {
        val field = WorkManagerImpl::class.java.getDeclaredField("sDelegatedInstance")
        field.isAccessible = true
        field.set(null, workManager)
    }

    @After
    fun tearDown() {
        val field = WorkManagerImpl::class.java.getDeclaredField("sDelegatedInstance")
        field.isAccessible = true
        field.set(null, null)
    }

    @Test
    fun `Given work succeeds, When invoked, Then return Success`() = runTest {
        val workInfo: WorkInfo = mockk {
            every { state } returns WorkInfo.State.SUCCEEDED
        }
        every { workManager.getWorkInfoByIdFlow(any()) } returns flowOf(workInfo)

        val useCase = RegisterScheduleDeletionJobUseCaseImpl(context)
        val result = useCase.invoke()

        assertThat(result, equalTo(ScheduleJobResult.Success))
    }

    @Test
    fun `workFailsWithErrorMessage`() = runTest {
        val errorMessage = "Test error message"
        val outputData = Data.Builder()
            .putString("key_schedule_deletion_error_message", errorMessage)
            .build()
        val workInfo: WorkInfo = mockk {
            every { state } returns WorkInfo.State.FAILED
            every { this@mockk.outputData } returns outputData
        }
        every { workManager.getWorkInfoByIdFlow(any()) } returns flowOf(workInfo)

        val useCase = RegisterScheduleDeletionJobUseCaseImpl(context)
        val result = useCase.invoke()

        assertThat(result is ScheduleJobResult.Failure, equalTo(true))
        assertThat(
            (result as ScheduleJobResult.Failure).throwable.message!!,
            equalTo(errorMessage)
        )
    }

    @Test
    fun `Given work is cancelled, When invoked, Then return Cancelled`() = runTest {
        val workInfo: WorkInfo = mockk {
            every { state } returns WorkInfo.State.CANCELLED
        }
        every { workManager.getWorkInfoByIdFlow(any()) } returns flowOf(workInfo)

        val useCase = RegisterScheduleDeletionJobUseCaseImpl(context)
        val result = useCase.invoke()

        assertThat(result, equalTo(ScheduleJobResult.Cancelled))
    }
}
