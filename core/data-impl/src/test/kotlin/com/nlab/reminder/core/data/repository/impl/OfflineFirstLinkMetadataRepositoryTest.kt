package com.nlab.reminder.core.data.repository.impl

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkAndMetadataAndEntity
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.network.datasource.LinkThumbnailResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class OfflineFirstLinkMetadataRepositoryTest {
    @Test
    fun `Given cached data, When collecting stream, Then return matching data and any source not called`() = runTest {
        // given
        val (expectedLink, expectedMetadata) = genLinkAndMetadataAndEntity()
        val linkMetadataDAO = mockk<LinkMetadataDAO>(relaxed = true)
        val remoteDataSource = mockk<LinkThumbnailDataSource>(relaxed = true)
        val linkMetadataRepository = genOfflineFirstLinkMetadataRepository(
            linkMetadataDAO = linkMetadataDAO,
            remoteDataSource = remoteDataSource,
            remoteCatch = mockk {
                every { snapshot() } returns mapOf(expectedLink to expectedMetadata)
            }
        )

        // when
        val collected = mutableListOf<Map<Link, LinkMetadata>>()
        unconfinedBackgroundScope.launch {
            linkMetadataRepository
                .getLinkToMetadataTableAsStream(setOf(expectedLink))
                .toList(destination = collected)
        }
        advanceUntilIdle()

        // then
        assertThat(
            collected,
            equalTo(listOf(mapOf(expectedLink to expectedMetadata)))
        )
        coVerify(exactly = 0) { linkMetadataDAO.findByLinks(any()) }
        coVerify(exactly = 0) { remoteDataSource.getLinkThumbnail(any()) }
    }

    @Test
    fun `Given no data, When collected stream, Then return emptyMap once`() = runTest {
        val linkMetadataRepository = genOfflineFirstLinkMetadataRepository(
            linkMetadataDAO = mockk {
                coEvery { findByLinks(any()) } returns emptyList()
            },
            remoteDataSource = mockk {
                coEvery { getLinkThumbnail(any()) } returns Result.Failure(RuntimeException())
            },
            remoteCatch = mockk {
                every { snapshot() } returns emptyMap()
            }
        )

        linkMetadataRepository.getLinkToMetadataTableAsStream(setOf(genLink())).test {
            // then
            assertThat(awaitItem(), equalTo(emptyMap()))
            awaitComplete()
        }
    }

    @Test
    fun `Given local data, When collected stream, Then return matching data from dao`() = runTest {
        // given
        val (expectedLink, expectedMetadata, entity) = genLinkAndMetadataAndEntity()
        val linkMetadataRepository = genOfflineFirstLinkMetadataRepository(
            linkMetadataDAO = mockk(relaxed = true) {
                coEvery { findByLinks(setOf(expectedLink.rawLink)) } returns listOf(entity)
            }
        )

        // when
        linkMetadataRepository.getLinkToMetadataTableAsStream(setOf(expectedLink)).test {
            // then
            assertThat(awaitItem(), equalTo(emptyMap()))
            assertThat(awaitItem(), equalTo(mapOf(expectedLink to expectedMetadata)))
            awaitComplete()
        }
    }

    @Test
    fun `Given remote data, When collected stream, Then return matching data from remoteDataSource`() = runTest {
        // given
        val (expectedLink, expectedMetadata) = genLinkAndMetadataAndEntity()
        val linkMetadataRepository = genOfflineFirstLinkMetadataRepository(
            remoteDataSource = mockk {
                coEvery { getLinkThumbnail(expectedLink.rawLink) } returns Result.Success(
                    LinkThumbnailResponse(
                        title = expectedMetadata.title?.value,
                        image = expectedMetadata.imageUrl?.value
                    )
                )
            }
        )

        // when
        linkMetadataRepository.getLinkToMetadataTableAsStream(setOf(expectedLink)).test {
            // then
            assertThat(awaitItem(), equalTo(emptyMap()))
            assertThat(awaitItem(), equalTo(mapOf(expectedLink to expectedMetadata)))
            awaitComplete()
        }
    }
}

private fun genOfflineFirstLinkMetadataRepository(
    linkMetadataDAO: LinkMetadataDAO = mockk<LinkMetadataDAO>(relaxed = true) {
        coEvery { findByLinks(any()) } returns emptyList()
    },
    remoteDataSource: LinkThumbnailDataSource = mockk<LinkThumbnailDataSource> {
        coEvery { getLinkThumbnail(any()) } returns Result.Failure(RuntimeException())
    },
    remoteCatch: LinkMetadataRemoteCache = mockk<LinkMetadataRemoteCache>(relaxed = true) {
        every { snapshot() } returns emptyMap()
    }
) = OfflineFirstLinkMetadataRepository(
    linkMetadataDAO = linkMetadataDAO,
    remoteDataSource = remoteDataSource,
    remoteCache = remoteCatch,
)