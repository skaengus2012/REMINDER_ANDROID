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

import com.nlab.reminder.core.data.model.genLink
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleContent
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.repository.GetScheduleQuery
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.ScheduleDetailRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.ScheduleTagListRepository
import com.nlab.reminder.core.data.repository.TagRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Thalys
 */
class DefaultScheduleDetailRepositoryTest {
    private lateinit var query: GetScheduleQuery

    @Before
    fun setup() {
        query = GetScheduleQuery.All
    }

    @Test
    fun `Given schedule, When getScheduleDetailsAsStream, Then scheduleDetail has schedule`() = runTest {
        val schedule = genSchedule()
        val repository = genScheduleDetailRepository(
            scheduleRepository = mock {
                whenever(mock.getSchedulesAsStream(query)) doReturn flowOf(setOf(schedule))
            }
        )

        val actualDetail = repository
            .getScheduleDetailsAsStream(query)
            .first()
            .first()
        assertThat(actualDetail.schedule, equalTo(schedule))
    }

    @Test
    fun `Given schedule, usage tag, When getScheduleDetailsAsStream, Then scheduleDetail has usage tag`() = runTest {
        val schedule = genSchedule()
        val usageTag = genTag()
        val repository = genScheduleDetailRepository(
            scheduleRepository = mock {
                whenever(mock.getSchedulesAsStream(query)) doReturn flowOf(setOf(schedule))
            },
            tagRepository = mock {
                whenever(mock.getTagsAsStream(GetTagQuery.ByIds(setOf(usageTag.id)))) doReturn flowOf(setOf(usageTag))
            },
            scheduleTagListRepository = mock {
                whenever(mock.getScheduleTagListAsStream(setOf(schedule.id)))
                    .doReturn(flowOf(mapOf(schedule.id to setOf(usageTag.id))))
            }
        )

        val actualDetail = repository
            .getScheduleDetailsAsStream(query)
            .first()
            .first()
        assertThat(actualDetail.tags.first(), equalTo(usageTag))
    }

    @Test
    fun `Given schedule with link, When getScheduleDetailsAsStream, Then scheduleDetail has linkMetadata`() = runTest {
        val link = genLink()
        val schedule = genSchedule(content = genScheduleContent(link = link))
        val expectedLinkMetadata = genLinkMetadata()
        val repository = genScheduleDetailRepository(
            scheduleRepository = mock {
                whenever(mock.getSchedulesAsStream(query)) doReturn flowOf(setOf(schedule))
            },
            linkMetadataRepository = mock {
                whenever(mock.getAsStream(setOf(link))) doReturn flowOf(mapOf(link to expectedLinkMetadata))
            }
        )

        val actualDetail = repository
            .getScheduleDetailsAsStream(query)
            .first()
            .first()
        assertThat(actualDetail.linkMetadata, equalTo(expectedLinkMetadata))
    }

    @Test
    fun `Given schedule without link, When getScheduleDetailsAsStream, Then scheduleDetail has no linkMetadata`() = runTest {
        val schedule = genSchedule(content = genScheduleContent(link = null))
        val repository = genScheduleDetailRepository(
            scheduleRepository = mock {
                whenever(mock.getSchedulesAsStream(query)) doReturn flowOf(setOf(schedule))
            }
        )

        val actualDetail = repository
            .getScheduleDetailsAsStream(query)
            .first()
            .first()
        assertThat(actualDetail.linkMetadata, nullValue())
    }
}

private fun genScheduleDetailRepository(
    scheduleRepository: ScheduleRepository,
    tagRepository: TagRepository = mock {
        whenever(mock.getTagsAsStream(any())) doReturn flowOf(emptySet())
    },
    scheduleTagListRepository: ScheduleTagListRepository = mock {
        whenever(mock.getScheduleTagListAsStream(any())) doReturn flowOf(emptyMap())
    },
    linkMetadataRepository: LinkMetadataRepository = mock {
        whenever(mock.getAsStream(any())) doReturn flowOf(emptyMap())
    }
): ScheduleDetailRepository = DefaultScheduleDetailRepository(
    scheduleRepository = scheduleRepository,
    tagRepository = tagRepository,
    scheduleTagListRepository = scheduleTagListRepository,
    linkMetadataRepository = linkMetadataRepository
)