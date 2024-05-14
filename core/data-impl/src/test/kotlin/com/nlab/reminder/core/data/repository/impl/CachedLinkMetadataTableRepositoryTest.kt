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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.LinkMetadataTable
import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class CachedLinkMetadataTableRepositoryTest {
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