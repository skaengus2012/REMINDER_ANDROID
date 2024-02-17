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

package com.nlab.reminder.internal.data.repository

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.TimestampRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.internal.common.android.database.LinkMetadataDao
import com.nlab.reminder.internal.data.model.toEntity
import com.nlab.testkit.genBlank
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
import com.nlab.testkit.genLongGreaterThanZero
import com.nlab.testkit.once
import com.nlab.testkit.unconfinedCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class LocalLinkMetadataTableRepositoryTest {
    @Test
    fun `Metadata was cached, when link is fetched`() = runTest {
        val link = genLink()
        val linkMetadata = genLinkMetadata()
        val timestamp = genLongGreaterThanZero()
        val linkMetadataDao: LinkMetadataDao = mock()
        val linkMetadataTableRepository = genLinkMetadataTableRepository(
            linkMetadataDao = linkMetadataDao,
            linkMetadataRepository = mock {
                whenever(mock.get(link)) doReturn Result.Success(linkMetadata)
            },
            timestampRepository = mock {
                whenever(mock.get()) doReturn timestamp
            }
        )

        linkMetadataTableRepository.fetch(setOf(link))
        verify(linkMetadataDao, once()).insertAndClearOldData(linkMetadata.toEntity(link, timestamp))
    }

    @Test
    fun `Only metadata of valid links was cached`() = runTest {
        val links = List(genInt(min = 2, max = 10)) { genLink() }
        val emptyLink = Link(genBlank())
        val linkMetadataDao: LinkMetadataDao = mock()
        val linkMetadataTableRepository = genLinkMetadataTableRepository(
            linkMetadataDao = linkMetadataDao,
            linkMetadataRepository = object : LinkMetadataRepository {
                override suspend fun get(link: Link): Result<LinkMetadata> {
                    return Result.Success(genLinkMetadata())
                }
            }
        )

        linkMetadataTableRepository.fetch((links + emptyLink).toSet())
        verify(linkMetadataDao, times(links.size)).insertAndClearOldData(any())
    }

    @Test
    fun `Cached, when metadata loading succeed`() = runTest {
        val successTargetLink = Link("A")
        val failedTargetLink = Link("B")
        val linkMetadataDao: LinkMetadataDao = mock()
        val linkMetadataTableRepository = genLinkMetadataTableRepository(
            linkMetadataDao = linkMetadataDao,
            linkMetadataRepository = mock {
                whenever(mock.get(successTargetLink)) doReturn Result.Success(genLinkMetadata())
                whenever(mock.get(failedTargetLink)) doReturn Result.Failure(Throwable())
            }
        )

        linkMetadataTableRepository.fetch(setOf(successTargetLink, failedTargetLink))
        verify(linkMetadataDao, once()).insertAndClearOldData(any())
    }

    @Test
    fun `Only cacheable metadata is cached`() = runTest {
        val links = List(3) { genLink() }
        val linkMetadataDao: LinkMetadataDao = mock()
        val linkMetadataTableRepository = genLinkMetadataTableRepository(
            linkMetadataDao = linkMetadataDao,
            linkMetadataRepository = mock {
                whenever(mock.get(links[0])) doReturn Result.Success(genLinkMetadata(title = genBlank()))
                whenever(mock.get(links[1])) doReturn Result.Success(genLinkMetadata(imageUrl = genBlank()))
                whenever(mock.get(links[2])) doReturn Result.Success(
                    genLinkMetadata(title = genBlank(), imageUrl = genBlank())
                )
            }
        )

        linkMetadataTableRepository.fetch(links.toSet())
        verify(linkMetadataDao, times(2)).insertAndClearOldData(any())
    }

    @Test
    fun `Get cached linkMetadata from dao`() = runTest {
        val link = genLink()
        val linkMetadata = genLinkMetadata()
        val linkMetadataTableRepository = genLinkMetadataTableRepository(
            linkMetadataDao = mock {
                whenever(mock.findAsStream()) doReturn flowOf(listOf(linkMetadata.toEntity(link, genLong())))
            }
        )

        val linkMetadataTable = linkMetadataTableRepository.getStream()
            .take(1)
            .first()
        assertThat(
            linkMetadataTable,
            CoreMatchers.equalTo(LinkMetadataTable(mapOf(link to linkMetadata)))
        )
    }
}

private fun TestScope.genLinkMetadataTableRepository(
    linkMetadataDao: LinkMetadataDao = mock(),
    linkMetadataRepository: LinkMetadataRepository = mock(),
    timestampRepository: TimestampRepository = mock {
        whenever(mock.get()) doReturn genLong()
    },
    coroutineScope: CoroutineScope = unconfinedCoroutineScope()
): LinkMetadataTableRepository = LocalLinkMetadataTableRepository(
    linkMetadataDao, linkMetadataRepository, timestampRepository, coroutineScope
)