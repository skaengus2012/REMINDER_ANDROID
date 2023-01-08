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

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.getOrThrow
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.domain.common.util.link.genLinkMetadata
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class CachedLinkMetadataRepositoryTest {
    @Test
    fun `try loading from internal repository when cache was not hit`() = runTest {
        val link: String = genBothify()
        val internalRepository: LinkMetadataRepository = mock()
        val cachedRepository = CachedLinkMetadataRepository(internalRepository)
        cachedRepository.get(link)

        verify(internalRepository, once()).get(link)
    }

    @Test
    fun `skip loading from internal repository when cache was hit`() = runTest {
        val repeatCount = 10
        val link: String = genBothify()
        val expectedThumbnail: LinkMetadata = genLinkMetadata()
        val internalRepository: LinkMetadataRepository = mock {
            whenever(mock.get(link)) doReturn Result.Success(expectedThumbnail)
        }
        val cachedRepository = CachedLinkMetadataRepository(internalRepository)

        val results = mutableListOf<LinkMetadata>()
        repeat(repeatCount) { results += cachedRepository.get(link).getOrThrow() }

        assertThat(results, equalTo(List(10) { expectedThumbnail }))
        verify(internalRepository, once()).get(link)
    }
}