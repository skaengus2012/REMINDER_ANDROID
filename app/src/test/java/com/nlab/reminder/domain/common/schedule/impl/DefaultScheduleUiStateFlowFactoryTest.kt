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

import com.nlab.reminder.core.util.link.LinkThumbnail
import com.nlab.reminder.core.util.link.LinkThumbnailRepository
import com.nlab.reminder.core.util.link.genLinkThumbnail
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.core.kotlin.util.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultScheduleUiStateFlowFactoryTest {
    @Test
    fun `schedule combined with complete mark`() = runTest {
        testTemplate()
    }

    @Test
    fun `set link from linkThumbnailRepository`() = runTest {
        val expectedLinkThumbnail: LinkThumbnail = genLinkThumbnail()
        val linkThumbnailRepository: LinkThumbnailRepository = mock {
            whenever(mock.get(any())) doReturn Result.Success(expectedLinkThumbnail)
        }
        testTemplate(
            linkThumbnailRepository,
            decorateExpectedScheduleUiState = { scheduleUiState ->
                scheduleUiState.copy(linkThumbnail = expectedLinkThumbnail)
            }
        )
    }

    private suspend fun testTemplate(
        linkThumbnailRepository: LinkThumbnailRepository = mock(),
        decorateExpectedScheduleUiState: (ScheduleUiState) -> ScheduleUiState = { it }
    ) {
        val completeMarkTestFixture = CompleteMarkCombineTestFixture()
        val scheduleUiStateFlowFactory = DefaultScheduleUiStateFlowFactory(
            completeMarkTestFixture.completeMarkRepository,
            linkThumbnailRepository
        )
        val acc: List<ScheduleUiState> =
            scheduleUiStateFlowFactory
                .with(completeMarkTestFixture.schedulesFlow)
                .take(1)
                .first()
        assertThat(
            acc, equalTo(completeMarkTestFixture.expectedScheduleUiStates.map(decorateExpectedScheduleUiState))
        )
    }
}