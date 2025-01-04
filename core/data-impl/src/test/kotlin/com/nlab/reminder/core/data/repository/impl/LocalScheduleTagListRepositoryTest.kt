/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genTagId
import com.nlab.reminder.core.data.repository.ScheduleTagListRepository
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Thalys
 */
class LocalScheduleTagListRepositoryTest {
    @Test
    fun `Given scheduleIds, When getScheduleTagListAsStream, Then table found from dao`() = runTest {
        val scheduleId = genScheduleId()
        val expectedTagId = genTagId()
        val expectedTable = mapOf(scheduleId to setOf(expectedTagId))
        val repository = genScheduleTagListRepository(scheduleTagListDAO = mock {
            whenever(mock.findByScheduleIdsAsStream(setOf(scheduleId.rawId))) doReturn flowOf(
                arrayOf(
                    ScheduleTagListEntity(
                        scheduleId = scheduleId.rawId,
                        tagId = expectedTagId.rawId
                    )
                )
            )
        })
        val actualTable = repository
            .getScheduleTagListAsStream(scheduleIds = setOf(scheduleId))
            .first()
        assertThat(actualTable, equalTo(expectedTable))
    }

    @Test
    fun `Given tagId, When getTagUsageCount, Then usageCount found from dao`() = runTest {
        val id = genTagId()
        val expectedUsageCount = genNonNegativeLong()
        val repository = genScheduleTagListRepository(scheduleTagListDAO = mock {
            whenever(mock.findTagUsageCount(id.rawId)) doReturn expectedUsageCount.value
        })
        val actualUsageCount = repository
            .getTagUsageCount(id)
            .getOrThrow()
        assertThat(actualUsageCount, equalTo(expectedUsageCount))
    }
}

private fun genScheduleTagListRepository(
    scheduleTagListDAO: ScheduleTagListDAO = mock(),
): ScheduleTagListRepository = LocalScheduleTagListRepository(scheduleTagListDAO)