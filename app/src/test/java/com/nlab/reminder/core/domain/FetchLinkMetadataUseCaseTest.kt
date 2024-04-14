package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.schedule.model.genScheduleElement
import com.nlab.testkit.faker.genInt
import org.mockito.kotlin.once
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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