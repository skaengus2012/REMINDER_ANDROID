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

import com.nlab.reminder.core.util.link.LinkThumbnailRepository
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.util.link.LinkThumbnail
import com.nlab.reminder.core.util.link.genLinkThumbnail
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.genSchedule
import com.nlab.reminder.test.genBothify
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class LinkThumbnailRepositoriesKtTest {
    @Test
    fun `get linkThumbnail when repository request was succeed`() = runTest {
        val expectedLinkThumbnail: LinkThumbnail = genLinkThumbnail()
        val link: String = genBothify()
        val schedule: Schedule = genSchedule(link = link)
        val linkThumbnailRepository: LinkThumbnailRepository = mock {
            whenever(mock.get(link)) doReturn Result.Success(expectedLinkThumbnail)
        }

        assertThat(
            linkThumbnailRepository.findLinkThumbnail(schedule),
            equalTo(expectedLinkThumbnail)
        )
    }

    @Test
    fun `get null when repository request was failed`() = runTest {
        val schedule: Schedule = genSchedule(link = genBothify())
        val linkThumbnailRepository: LinkThumbnailRepository = mock {
            whenever(mock.get(any())) doReturn Result.Failure(RuntimeException())
        }

        assertNull(linkThumbnailRepository.findLinkThumbnail(schedule))
    }

    @Test
    fun `get null when schedule was null`() = runTest {
        val schedule: Schedule = genSchedule(link = null)
        val linkThumbnailRepository: LinkThumbnailRepository = mock {
            whenever(mock.get(any())) doReturn Result.Success(genLinkThumbnail())
        }

        assertNull(linkThumbnailRepository.findLinkThumbnail(schedule))
    }
}