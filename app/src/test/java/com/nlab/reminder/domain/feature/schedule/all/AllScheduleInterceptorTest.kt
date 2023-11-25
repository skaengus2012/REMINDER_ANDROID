/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownRepository
import com.nlab.statekit.middleware.interceptor.scenario
import com.nlab.testkit.genBoolean
import com.nlab.testkit.once
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class AllScheduleInterceptorTest {
    @Test
    fun `Given State Loaded, When OnCompletedScheduleVisibilityUpdateClicked, Then Repository called setShown`() = runTest {
        val isVisible = genBoolean()
        val completedScheduleShownRepository: CompletedScheduleShownRepository = mock {
            whenever(mock.setShown(isVisible)) doReturn Result.Success(Unit)
        }

        genInterceptor(completedScheduleShownRepository = completedScheduleShownRepository)
            .scenario()
            .initState(genAllScheduleUiStateLoaded())
            .action(AllScheduleAction.OnCompletedScheduleVisibilityUpdateClicked(isVisible))
            .dispatchIn(testScope = this)
        verify(completedScheduleShownRepository, once()).setShown(isShown = isVisible)
    }
}

private fun genInterceptor(
    completedScheduleShownRepository: CompletedScheduleShownRepository = mock()
): AllScheduleInterceptor = AllScheduleInterceptor(completedScheduleShownRepository)