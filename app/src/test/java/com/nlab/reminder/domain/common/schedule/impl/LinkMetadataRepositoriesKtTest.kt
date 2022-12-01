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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.genLinkMetadata
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBothify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class LinkMetadataRepositoriesKtTest {
    @Test
    fun `get linkThumbnail when repository request was succeed`() = runTest {
        val expectedLinkMetadata: LinkMetadata = genLinkMetadata()
        val link: String = genBothify()
        val schedule: Schedule = genSchedule(link = link)
        val linkThumbnailRepository: LinkMetadataRepository = mock {
            whenever(mock.get(link)) doReturn Result.Success(expectedLinkMetadata)
        }

        assertThat(
            linkThumbnailRepository.findLinkMetadata(schedule),
            equalTo(expectedLinkMetadata)
        )
    }

    @Test
    fun `get empty when repository request was failed`() = runTest {
        val schedule: Schedule = genSchedule(link = genBothify())
        val linkThumbnailRepository: LinkMetadataRepository = mock {
            whenever(mock.get(any())) doReturn Result.Failure(RuntimeException())
        }

        assertThat(
            linkThumbnailRepository.findLinkMetadata(schedule),
            equalTo(LinkMetadata.Empty)
        )
    }

    @Test
    fun `get empty when schedule was null`() = runTest {
        val schedule: Schedule = genSchedule(link = null)
        val linkThumbnailRepository: LinkMetadataRepository = mock {
            whenever(mock.get(any())) doReturn Result.Success(genLinkMetadata())
        }

        assertThat(
            linkThumbnailRepository.findLinkMetadata(schedule),
            equalTo(LinkMetadata.Empty)
        )
    }
}