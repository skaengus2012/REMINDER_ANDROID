package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import org.mockito.kotlin.once
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
class CachedLinkMetadataTableRepositoryTest {
    @Test
    fun `When fetched, Then internalRepository fetched`() = runTest {
        val link = genLink()
        val internalRepository: LinkMetadataTableRepository = mock()
        val repository = genCachedLinkMetadataTableRepository(internalRepository)

        repository.fetch(setOf(link))
        verify(internalRepository, once()).fetch(setOf(link))
    }

    @Test
    fun `Given fetched link repository, When fetched same link, Then internalRepository never fetched`() = runTest {
        val link = genLink()
        val internalRepository: LinkMetadataTableRepository = mock()
        val repository = genCachedLinkMetadataTableRepository(internalRepository).apply {
            fetch(setOf(link))
        }

        repository.fetch(setOf(link))
        verify(internalRepository, once()).fetch(setOf(link))
    }

    @Test
    fun `Given linkMetadata loaded two times, Then repository get merged result`() = runTest {
        val linkMetadataTables = listOf(
            LinkMetadataTable(mapOf(genLink("https://first.com") to genLinkMetadata())),
            LinkMetadataTable(mapOf(genLink("https://second.com") to genLinkMetadata()))
        )
        val internalRepository: LinkMetadataTableRepository = mock {
            whenever(mock.getStream()) doReturn flowOf(
                linkMetadataTables[0],
                linkMetadataTables[1]
            )
        }
        val repository = genCachedLinkMetadataTableRepository(internalRepository)

        val expectedResult: LinkMetadataTable = repository.getStream()
            .filter { it.value.size == 2 }
            .first()
        assertThat(
            expectedResult,
            equalTo(LinkMetadataTable(linkMetadataTables[0].value + linkMetadataTables[1].value))
        )
    }

    @Test
    fun `Repository has emptyResult for init`() = runTest {
        val internalRepository: LinkMetadataTableRepository = mock {
            whenever(mock.getStream()) doReturn emptyFlow()
        }
        val repository = genCachedLinkMetadataTableRepository(internalRepository)

        val expectedResult: LinkMetadataTable = repository.getStream()
            .take(1)
            .first()
        assertThat(
            expectedResult,
            equalTo(LinkMetadataTable(emptyMap()))
        )
    }
}

private fun genCachedLinkMetadataTableRepository(
    internalRepository: LinkMetadataTableRepository = mock(),
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
): LinkMetadataTableRepository = CachedLinkMetadataTableRepository(internalRepository, dispatcher)