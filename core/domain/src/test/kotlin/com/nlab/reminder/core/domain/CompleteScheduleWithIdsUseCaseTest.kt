/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleUpdateRequest
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class CompleteScheduleWithIdsUseCaseTest {
    @Test
    fun `Given empty ids, When invoked, Then repository never works`() = runTest {
        val repository: ScheduleRepository = mock()
        val useCase = genCompleteScheduleWithIdsUseCase(scheduleRepository = repository)
        val requestIds = emptyList<ScheduleId>()

        val result = useCase.invoke(requestIds, genBoolean())
        verify(repository, never()).update(any())
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given not empty ids, When invoked, Then repository invoke update`() = runTest {
        val repository: ScheduleRepository = mock {
            whenever(mock.update(any())) doReturn Result.Success(Unit)
        }
        val useCase = genCompleteScheduleWithIdsUseCase(scheduleRepository = repository)
        val requestIds = List(genInt(min = 1, max = 10)) { genScheduleId(it.toLong()) }
        val isComplete = genBoolean()

        val result = useCase.invoke(requestIds, isComplete)
        verify(repository, once()).update(ScheduleUpdateRequest.Completes(buildMap {
            requestIds.forEach { put(it, isComplete) }
        }))
        assertThat(result.isSuccess, equalTo(true))
    }
}

private fun genCompleteScheduleWithIdsUseCase(
    scheduleRepository: ScheduleRepository = mock(),
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) = CompleteScheduleWithIdsUseCase(scheduleRepository, dispatcher)