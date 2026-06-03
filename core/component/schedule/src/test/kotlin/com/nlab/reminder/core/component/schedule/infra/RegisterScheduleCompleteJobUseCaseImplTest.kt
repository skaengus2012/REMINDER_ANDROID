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
import androidx.work.WorkInfo
import androidx.work.impl.WorkManagerImpl
import com.nlab.reminder.core.component.schedule.ScheduleJobResult
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * @author Thalys
 */
class RegisterScheduleCompleteJobUseCaseImplTest {
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

        val useCase = RegisterScheduleCompleteJobUseCaseImpl(context)
        val result = useCase.invoke(debounceTimeout = 0.seconds, processUntilPriority = null)

        assertThat(result, equalTo(ScheduleJobResult.Success))
    }

    @Test
    fun `Given work fails, When invoked, Then return Failure`() = runTest {
        val workInfo: WorkInfo = mockk {
            every { state } returns WorkInfo.State.FAILED
        }
        every { workManager.getWorkInfoByIdFlow(any()) } returns flowOf(workInfo)

        val useCase = RegisterScheduleCompleteJobUseCaseImpl(context)
        val result = useCase.invoke(
            debounceTimeout = 1.seconds,
            processUntilPriority = genNonNegativeLong()
        )

        assertThat(result is ScheduleJobResult.Failure, equalTo(true))
        assertThat(
            (result as ScheduleJobResult.Failure).throwable.message!!,
            equalTo("Work failed with state: FAILED")
        )
    }

    @Test
    fun `Given work is cancelled, When invoked, Then return Cancelled`() = runTest {
        val workInfo: WorkInfo = mockk {
            every { state } returns WorkInfo.State.CANCELLED
        }
        every { workManager.getWorkInfoByIdFlow(any()) } returns flowOf(workInfo)

        val useCase = RegisterScheduleCompleteJobUseCaseImpl(context)
        val result = useCase.invoke(debounceTimeout = 0.seconds, processUntilPriority = null)

        assertThat(result, equalTo(ScheduleJobResult.Cancelled))
    }
}
