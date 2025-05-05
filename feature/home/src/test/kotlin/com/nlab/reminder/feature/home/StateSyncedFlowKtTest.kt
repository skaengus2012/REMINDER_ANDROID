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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.toNonBlankString
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class StateSyncedFlowKtTest {
    @Test
    fun `Given valid repositories, When collect StateSyncFlow, Then emit correct StateSynced`() = runTest {
        val expectedScheduleCount = genNonNegativeLong()
        val expectedTags = List(3) {
            genTag(id = TagId(it.toLong()), name = "tag$it".toNonBlankString())
        }
        val environment = genHomeEnvironment(
            scheduleRepository = mockk {
                every { getScheduleCountAsStream(any()) } returns flowOf(expectedScheduleCount)
            },
            tagRepository = mockk {
                every { getTagsAsStream(GetTagQuery.OnlyUsed) } returns flowOf(expectedTags.toSet())
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