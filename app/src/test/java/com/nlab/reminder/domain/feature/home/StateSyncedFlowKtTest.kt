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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class StateSyncedFlowKtTest {
    @Test
    fun `Given scheduleRepository and tagRepository emitting correct values, When collect StateSyncFlow, Then emit correct values`() = runTest {
        val expectedScheduleCount = genNonNegativeLong()
        val expectedTags = genTags().sortedBy { it.name.value }
        val environment = genHomeEnvironment(
            scheduleRepository = mock {
                whenever(mock.getScheduleCountAsStream(any())) doReturn flowOf(expectedScheduleCount)
            },
            tagRepository = mock {
                whenever(mock.getTagsAsStream(any())) doReturn flowOf(expectedTags.shuffled())
            }
        )

        val flow = StateSyncFlow(environment)
        val actualValue = flow.first()
        assertThat(
            actualValue,
            equalTo(
                HomeAction.StateSynced(
                    todaySchedulesCount = expectedScheduleCount,
                    timetableSchedulesCount = expectedScheduleCount,
                    allSchedulesCount = expectedScheduleCount,
                    sortedTags = expectedTags
                )
            )
        )
    }
}