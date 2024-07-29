package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.genLinkAndMetadataAndEntity
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.foundation.time.TimestampProvider
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.local.database.LinkMetadataDao
import com.nlab.reminder.core.network.LinkThumbnailDataSource
import com.nlab.reminder.core.network.LinkThumbnailResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
internal class OfflineFirstLinkMetadataRepositoryTest {
    @Test
    fun `Given cached in memory data, When getAsFlow, Then dataSources never works`() = runTest {
        val (expectedLink, expectedMetadata) = genLinkAndMetadataAndEntity()
        val linkMetadataDao = mock<LinkMetadataDao>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDao = linkMetadataDao,
            initialCache = mapOf(expectedLink to expectedMetadata)
        )
        linkMetadataRepository
            .getAsStream(setOf(expectedLink))
            .shareIn(unconfinedCoroutineScope(), SharingStarted.Eagerly)
        advanceUntilIdle()
        verify(linkMetadataDao, never()).findByLinks(any())
    }

    @Test
    fun `Given local cache existed, When getAsFlow, Then flow returns metadata async`() = runTest {
        val (expectedLink, expectedMetadata, expectedEntity) = genLinkAndMetadataAndEntity()
        val linkMetadataDao = mock<LinkMetadataDao> {
            whenever(mock.findByLinks(listOf(expectedEntity.link))) doReturn listOf(expectedEntity)
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnailResource(any())) doReturn Result.Failure(Throwable())
        }
        val initCache = emptyMap<Link.Present, LinkMetadata>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDao = linkMetadataDao,
            linkThumbnailDataSource = linkThumbnailDataSource,
            initialCache = initCache
        )
        val actualTableList = foldActualResultsWhenScopeUntilIdle(
            linkMetadataRepository = linkMetadataRepository,
            parameter = expectedLink
        )
        assertThat(
            actualTableList,
            equalTo(
                listOf(
                    initCache,
                    mapOf(expectedLink to expectedMetadata)
                )
            )
        )
    }

    @Test
    fun `Given remote data, When getAsFlow, Then flow returns metadata async`() = runTest {
        val (expectedLink, expectedMetadata, expectedEntity) = genLinkAndMetadataAndEntity()
        val remoteExpectedMetadata = genLinkMetadata(
            title = expectedMetadata.title + "_remote",
            imageUrl = expectedMetadata.imageUrl + "_remote"
        )
        val linkMetadataDao = mock<LinkMetadataDao> {
            whenever(mock.findByLinks(any())) doSuspendableAnswer {
                delay(5000)
                listOf(expectedEntity)
            }
        }
        val linkThumbnailDataSource = mock<LinkThumbnailDataSource> {
            whenever(mock.getLinkThumbnailResource(expectedLink.value)) doReturn Result.Success(
                LinkThumbnailResource(
                    title = remoteExpectedMetadata.title,
                    image = remoteExpectedMetadata.imageUrl
                )
            )
        }
        val initCache = emptyMap<Link.Present, LinkMetadata>()
        val linkMetadataRepository = getLinkMetadataRepository(
            linkMetadataDao = linkMetadataDao,
            linkThumbnailDataSource = linkThumbnailDataSource,
            initialCache = initCache
        )
        val actualTableList = foldActualResultsWhenScopeUntilIdle(
            linkMetadataRepository = linkMetadataRepository,
            parameter = expectedLink
        )
        assertThat(
            actualTableList,
            equalTo(
                listOf(
                    initCache,
                    mapOf(expectedLink to expectedMetadata),
                    mapOf(expectedLink to remoteExpectedMetadata)
                )
            )
        )
    }
}

private fun getLinkMetadataRepository(
    linkMetadataDao: LinkMetadataDao = mock<LinkMetadataDao>(),
    linkThumbnailDataSource: LinkThumbnailDataSource = mock<LinkThumbnailDataSource>(),
    timestampProvider: TimestampProvider = mock {
        whenever(mock.now()) doReturn 0L
    },
    initialCache: Map<Link.Present, LinkMetadata> = emptyMap()
) = OfflineFirstLinkMetadataRepository(
    linkMetadataDao = linkMetadataDao,
    linkThumbnailDataSource = linkThumbnailDataSource,
    timestampProvider = timestampProvider,
    initialCache = initialCache,
)

private suspend fun TestScope.foldActualResultsWhenScopeUntilIdle(
    linkMetadataRepository: OfflineFirstLinkMetadataRepository,
    parameter: Link.Present,
): List<Map<Link.Present, LinkMetadata>> = buildList {
    linkMetadataRepository
        .getAsStream(setOf(parameter))
        .onEach { add(it) }
        .launchIn(unconfinedCoroutineScope())
    advanceUntilIdle()
}