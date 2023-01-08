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

package com.nlab.reminder.internal.common.util.link.impl

import com.nlab.reminder.domain.common.util.link.*
import com.nlab.reminder.internal.common.android.database.LinkMetadataDao
import com.nlab.reminder.internal.common.android.database.toEntity
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLong
import com.nlab.reminder.test.once
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.test.genNumerify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalCachedLinkMetadataTableRepositoryTest {
    @Test
    fun `getStream from linkMetadataDao`() = runTest {
        val link: String = genBothify()
        val linkMetadata: LinkMetadata = genLinkMetadata()
        val linkMetadataDao: LinkMetadataDao = mock {
            whenever(mock.findAsStream()) doReturn flowOf(listOf(linkMetadata.toEntity(link, genLong())))
        }
        val linkMetadataTableRepository: LinkMetadataTableRepository =
            genLinkMetadataTableRepository(linkMetadataDao = linkMetadataDao)

        val table: LinkMetadataTable =
            linkMetadataTableRepository.getStream()
                .take(1)
                .first()
        assertThat(
            table,
            equalTo(mapOf(link to linkMetadata))
        )
    }

    @Test
    fun `ignore blank link requests`() = runTest {
        val linkMetadataRepository: LinkMetadataRepository = mock()
        val linkMetadataTableRepository: LinkMetadataTableRepository =
            genLinkMetadataTableRepository(linkMetadataRepository = linkMetadataRepository)

        linkMetadataTableRepository.setLinks(listOf(" "))
        verify(linkMetadataRepository, never()).get(any())
    }

    @Test
    fun `request once when same links inputted`() = runTest {
        val link: String = genBothify()
        val linkMetadataRepository: LinkMetadataRepository = mock()
        val linkMetadataTableRepository: LinkMetadataTableRepository =
            genLinkMetadataTableRepository(linkMetadataRepository = linkMetadataRepository)

        linkMetadataTableRepository.setLinks(List(10) { link })
        verify(linkMetadataRepository, once()).get(any())
    }

    @Test
    fun `insert linkMetadata when loaded metadata title was not black`() = runTest {
        val link = genBothify()
        val linkMetadata: LinkMetadata = genLinkMetadata(title = genNumerify(), image = " ")
        val timestamp = genLong()
        val linkMetadataDao: LinkMetadataDao = mock()
        setLinks(
            link,
            linkMetadata,
            timestamp,
            linkMetadataDao
        )

        verify(linkMetadataDao, once()).insertAndClearOldData(linkMetadata.toEntity(link, timestamp))
    }

    @Test
    fun `insert linkMetadata when loaded metadata image was blank`() = runTest {
        val link = genNumerify()
        val linkMetadata: LinkMetadata = genLinkMetadata(title = " ", image = genBothify())
        val timestamp = genLong()
        val linkMetadataDao: LinkMetadataDao = mock()
        setLinks(
            link,
            linkMetadata,
            timestamp,
            linkMetadataDao
        )

        verify(linkMetadataDao, once()).insertAndClearOldData(linkMetadata.toEntity(link, timestamp))
    }

    @Test
    fun `insert linkMetadata when loaded metadata image and title was not black`() = runTest {
        val link = genNumerify()
        val linkMetadata: LinkMetadata = genLinkMetadata(title = genBothify(), image = genBothify())
        val timestamp = genLong()
        val linkMetadataDao: LinkMetadataDao = mock()
        setLinks(
            link,
            linkMetadata,
            timestamp,
            linkMetadataDao
        )

        verify(linkMetadataDao, once()).insertAndClearOldData(linkMetadata.toEntity(link, timestamp))
    }

    @Test
    fun `ignore insert linkMetadata when loaded metadata image and title was black`() = runTest {
        val link = genNumerify()
        val linkMetadata: LinkMetadata = genLinkMetadata(title = "   ", image = "  ")
        val timestamp = genLong()
        val linkMetadataDao: LinkMetadataDao = mock()
        setLinks(
            link,
            linkMetadata,
            timestamp,
            linkMetadataDao
        )

        verify(linkMetadataDao, never()).insertAndClearOldData(linkMetadata.toEntity(link, timestamp))
    }

    private suspend fun setLinks(
        link: String,
        linkMetadata: LinkMetadata,
        timestamp: Long,
        linkMetadataDao: LinkMetadataDao
    ) {
        val linkMetadataRepository: LinkMetadataRepository = mock {
            whenever(mock.get(link)) doReturn Result.Success(linkMetadata)
        }
        val linkMetadataTableRepository: LinkMetadataTableRepository =
            genLinkMetadataTableRepository(
                linkMetadataRepository,
                linkMetadataDao,
                getTimestamp = { timestamp }
            )
        linkMetadataTableRepository.setLinks(listOf(link))
    }

    private fun genLinkMetadataTableRepository(
        linkMetadataRepository: LinkMetadataRepository = mock(),
        linkMetadataDao: LinkMetadataDao = mock(),
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
        getTimestamp: () -> Long = mock()
    ): LocalCachedLinkMetadataTableRepository = LocalCachedLinkMetadataTableRepository(
        linkMetadataRepository, linkMetadataDao, coroutineScope, getTimestamp
    )
}