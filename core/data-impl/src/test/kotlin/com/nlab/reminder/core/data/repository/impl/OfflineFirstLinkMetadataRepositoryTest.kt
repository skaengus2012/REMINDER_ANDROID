package com.nlab.reminder.core.data.repository.impl

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkAndMetadataAndEntity
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.network.datasource.LinkThumbnailResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
import kotlinx.coroutines.test.unconfinedCoroutineScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
class OfflineFirstLinkMetadataRepositoryTest {
    @Test
    fun `Given cached in memory data, When getAsFlow, Then dataSources never works`() = runTest {
        val (expectedLink, expectedMetadata) = genLinkAndMetadataAndEntity()
        val linkMetadataDAO = mock<LinkMetadataDAO>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            inMemoryCache = InMemoryLinkMetadataCache(mapOf(expectedLink to expectedMetadata))
        )
        linkMetadataRepository
            .getAsStream(setOf(expectedLink))
            .shareIn(unconfinedCoroutineScope(), SharingStarted.Eagerly)
        advanceUntilIdle()
        verify(linkMetadataDAO, never()).findByLinks(any())
    }

    @Test
    fun `Given local cache existed, When getAsFlow, Then flow returns metadata async`() = runTest {
        val (expectedLink, expectedMetadata, expectedEntity) = genLinkAndMetadataAndEntity()
        val linkMetadataDAO = mock<LinkMetadataDAO> {
            whenever(mock.findByLinks(setOf(expectedLink.rawLink))) doReturn arrayOf(expectedEntity)
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnail(any())) doReturn Result.Failure(Throwable())
        }
        val initCache = emptyMap<Link, LinkMetadata>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            linkThumbnailDataSource = linkThumbnailDataSource,
            inMemoryCache = InMemoryLinkMetadataCache(initCache)
        )

        linkMetadataRepository.getAsStream(setOf(expectedLink)).test {
            assertThat(awaitItem(), equalTo(initCache))
            assertThat(awaitItem(), equalTo(mapOf(expectedLink to expectedMetadata)))
        }
    }

    @Test
    fun `Given local, remote data, When getAsFlow, Then flow returns metadata async`() = runTest {
        val (expectedLink, expectedMetadata, expectedEntity) = genLinkAndMetadataAndEntity()
        val remoteExpectedMetadata = genRemoteMetadata(expectedMetadata)
        val linkMetadataDAO = mock<LinkMetadataDAO> {
            whenever(mock.findByLinks(any())) doSuspendableAnswer {
                delay(5000)
                arrayOf(expectedEntity)
            }
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnail(expectedLink.rawLink)) doReturn Result.Success(
                LinkThumbnailResponse(
                    title = remoteExpectedMetadata.title?.value,
                    image = remoteExpectedMetadata.imageUrl?.value
                )
            )
        }
        val initCache = emptyMap<Link, LinkMetadata>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            linkThumbnailDataSource = linkThumbnailDataSource,
            inMemoryCache = InMemoryLinkMetadataCache(initCache)
        )
        linkMetadataRepository.getAsStream(setOf(expectedLink)).test {
            assertThat(awaitItem(), equalTo(initCache))
            assertThat(awaitItem(), equalTo(mapOf(expectedLink to expectedMetadata)))
            assertThat(awaitItem(), equalTo(mapOf(expectedLink to remoteExpectedMetadata)))
        }
    }

    @Test
    fun `Given remote data, When getAsFlow and remote cache not flushed, Then linkMetadataDao not inserted`() = runTest {
        val (expectedLink, expectedMetadata) = genLinkAndMetadataAndEntity()
        val remoteExpectedMetadata = genRemoteMetadata(expectedMetadata)
        val linkMetadataDAO = mock<LinkMetadataDAO> {
            whenever(mock.findByLinks(any())) doReturn emptyArray()
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnail(expectedLink.rawLink)) doReturn Result.Success(
                LinkThumbnailResponse(
                    title = remoteExpectedMetadata.title?.value,
                    image = remoteExpectedMetadata.imageUrl?.value
                )
            )
        }
        val inMemoryCache = mock<InMemoryLinkMetadataCache> {
            whenever(mock.tableFlow) doReturn MutableStateFlow(emptyMap())
            whenever(mock.put(expectedLink, remoteExpectedMetadata)) doReturn emptyMap()
        }
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            linkThumbnailDataSource = linkThumbnailDataSource,
            inMemoryCache = inMemoryCache
        )
        unconfinedBackgroundScope.launch {
            linkMetadataRepository.getAsStream(setOf(expectedLink)).collect()
        }
        advanceUntilIdle()
        verify(linkMetadataDAO, never()).insertAndGet(any())
    }

    @Test
    fun `Given failed remote response result, When getAsFlow, Then linkMetadataDao never inserted`() = runTest {
        val linkMetadataDAO = mock<LinkMetadataDAO> {
            whenever(mock.findByLinks(any())) doReturn emptyArray()
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnail(any())) doReturn Result.Failure(RuntimeException())
        }
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            linkThumbnailDataSource = linkThumbnailDataSource
        )
        unconfinedBackgroundScope.launch {
            linkMetadataRepository.getAsStream(setOf(genLink())).collect()
        }
        advanceUntilIdle()
        verify(linkMetadataDAO, never()).insertAndGet(any())
    }
}

private fun getLinkMetadataRepository(
    linkMetadataDAO: LinkMetadataDAO = mock<LinkMetadataDAO>(),
    linkThumbnailDataSource: LinkThumbnailDataSource = mock<LinkThumbnailDataSource>(),
    inMemoryCache: InMemoryLinkMetadataCache = InMemoryLinkMetadataCache(emptyMap())
) = OfflineFirstLinkMetadataRepository(
    linkMetadataDAO = linkMetadataDAO,
    linkThumbnailDataSource = linkThumbnailDataSource,
    inMemoryCache = inMemoryCache,
)

private fun genRemoteMetadata(linkMetadata: LinkMetadata): LinkMetadata = genLinkMetadata(
    title = linkMetadata.title?.value + "_remote",
    imageUrl = linkMetadata.imageUrl?.value + "_remote"
)