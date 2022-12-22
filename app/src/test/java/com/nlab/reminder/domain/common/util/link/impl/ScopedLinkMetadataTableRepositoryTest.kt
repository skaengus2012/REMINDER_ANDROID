/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.util.link.impl

import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.domain.common.util.link.genLinkMetadata
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genFlowExecutionDispatcher
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class ScopedLinkMetadataTableRepositoryTest {
    @Test
    fun `ignore blank link requests`() = runTest {
        val linkMetadataRepository: LinkMetadataRepository = mock()
        val linkMetadataTableRepository =
            ScopedLinkMetadataTableRepository(linkMetadataRepository, CoroutineScope(Dispatchers.Unconfined))

        linkMetadataTableRepository.setLinks(listOf(" "))
        verify(linkMetadataRepository, never()).get(any())
    }

    @Test
    fun `ignore already loaded link requests`() = runTest {
        val link: String = genBothify()
        val linkMetadata: LinkMetadata = genLinkMetadata()
        val linkMetadataRepository: LinkMetadataRepository = mock {
            whenever(mock.get(any())) doReturn Result.Success(linkMetadata)
        }
        val linkMetadataTableRepository =
            ScopedLinkMetadataTableRepository(linkMetadataRepository, CoroutineScope(Dispatchers.Unconfined))
        linkMetadataTableRepository.setLinks(listOf(link))
        linkMetadataTableRepository.setLinks(listOf(link))

        verify(linkMetadataRepository, once()).get(any())
    }

    @Test
    fun `request once when same links inputted`() = runTest {
        val link: String = genBothify()
        val linkMetadataRepository: LinkMetadataRepository = mock()
        val linkMetadataTableRepository =
            ScopedLinkMetadataTableRepository(linkMetadataRepository, CoroutineScope(Dispatchers.Unconfined))

        linkMetadataTableRepository.setLinks(List(10) { link })
        verify(linkMetadataRepository, once()).get(any())
    }

    @Test
    fun `link request executed in parallel`() = runTest {
        var actualLinkSize = 0
        val expectedLinkSize = 10
        val mutex = Mutex()
        val links: List<String> = List(expectedLinkSize) { genBothify() }
        val fakeLinkMetadataRepository: LinkMetadataRepository = object : LinkMetadataRepository {
            override suspend fun get(link: String): Result<LinkMetadata> {
                mutex.withLock { ++actualLinkSize }
                delay(1_000)
                return  Result.Success(genLinkMetadata())
            }
        }
        val linkMetadataTableRepository = ScopedLinkMetadataTableRepository(
            fakeLinkMetadataRepository,
            CoroutineScope(genFlowExecutionDispatcher(testScheduler))
        )

        withTimeout(2_000) {
            linkMetadataTableRepository.setLinks(links)
            advanceTimeBy(1_000)
            assertThat(actualLinkSize, equalTo(expectedLinkSize))
        }
    }

    @Test
    fun `notify linkMetadataTable when request success`() = runTest {
        val link: String = genBothify()
        val linkMetadata: LinkMetadata = genLinkMetadata()
        val linkMetadataRepository: LinkMetadataRepository = mock {
            whenever(mock.get(link)) doReturn Result.Success(linkMetadata)
        }
        val linkMetadataTableRepository =
            ScopedLinkMetadataTableRepository(linkMetadataRepository, CoroutineScope(Dispatchers.Default))
        val notifiedLinkTable = async {
            linkMetadataTableRepository.getStream()
                .filter { it.isNotEmpty() }
                .take(1)
                .first()
        }

        linkMetadataTableRepository.setLinks(listOf(link))
        assertThat(notifiedLinkTable.await(), equalTo(mapOf(link to linkMetadata)))
    }
}