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

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.schedule.model.genScheduleElement
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
internal class FetchLinkMetadataUseCaseTest {
    @Test
    fun `Given empty schedules, When invoked, Then repository never fetch`() = runTest {
        val repository: LinkMetadataTableRepository = mock()
        val useCase = genFetchLinkMetadataUseCase(repository)

        useCase.invoke(emptyList())
        verify(repository, never()).fetch(any())
    }

    @Test
    fun `Given empty link schedules, When invoked, Then repository never fetch`() = runTest {
        val scheduleItems = List(genInt(min = 1, max = 10)) { genScheduleElement(genSchedule(link = Link.EMPTY)) }
        val repository: LinkMetadataTableRepository = mock()
        val useCase = genFetchLinkMetadataUseCase(repository)

        useCase.invoke(scheduleItems)
        verify(repository, never()).fetch(any())
    }

    @Test
    fun `Given same link schedules, When invoked, Then repository fetched with distinct`() = runTest {
        val link = genLink()
        val scheduleItems = List(genInt(min = 2, max = 10)) { genScheduleElement(genSchedule(link = Link(link.value))) }
        val repository: LinkMetadataTableRepository = mock()
        val useCase = genFetchLinkMetadataUseCase(repository)

        useCase.invoke(scheduleItems)
        verify(repository, once()).fetch(setOf(link))
    }
}

private fun genFetchLinkMetadataUseCase(repository: LinkMetadataTableRepository) =
    FetchLinkMetadataUseCase(repository, dispatcher = Dispatchers.Unconfined)