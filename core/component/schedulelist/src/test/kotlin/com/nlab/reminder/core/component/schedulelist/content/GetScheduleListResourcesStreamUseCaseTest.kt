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

package com.nlab.reminder.core.component.schedulelist.content

import app.cash.turbine.test
import com.nlab.reminder.core.data.model.genLinkMetadata
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleContent
import com.nlab.reminder.core.data.model.genSchedules
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toNonBlankString
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class GetScheduleListResourcesStreamUseCaseTest {
    @Test
    fun `Given schedules, When collect, Then emit resources with identical content mapping`() = runTest {
        val schedules = genSchedules()
        val useCase = genGetScheduleListResourcesStreamUseCase()
        val flow = MutableStateFlow(schedules.toSet())
        useCase.invoke(schedulesStream = flow).test {
            val actualResources = awaitItem()
            val expectedIdToSchedule = schedules.associateBy { it.id }
            actualResources.forEach { resource ->
                val schedule = expectedIdToSchedule.getValue(key = resource.id)
                assertThat(resource.title, equalTo(schedule.content.title))
                assertThat(resource.note, equalTo(schedule.content.note))
                assertThat(resource.link, equalTo(schedule.content.link))
                assertThat(resource.timing, equalTo(schedule.content.timing))
                assertThat(resource.isComplete, equalTo(schedule.isComplete))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedules with tagIds, When collect, Then emit resources with matching sorted tags`() = runTest {
        val schedules = genSchedules()
        val tagIds = schedules.map { it.content.tagIds }
            .flatten()
            .distinct()
            .toSet()
        val tags = tagIds.toSet { tagId ->
            genTag(id = tagId, name = tagId.rawId.toString().toNonBlankString())
        }
        val useCase = genGetScheduleListResourcesStreamUseCase(
            tagRepository = mockk {
                every { getTagsAsStream(GetTagQuery.ByIds(tagIds)) } returns flowOf(tags)
            }
        )
        useCase.invoke(schedulesStream = flowOf(schedules.toSet())).test {
            val actualResources = awaitItem()
            val expectedIdToSchedule = schedules.associateBy { it.id }
            actualResources.forEach { resource ->
                val expectedTags = expectedIdToSchedule.getValue(key = resource.id).content.tagIds
                    // It's valid because it was named rawId,
                    .sortedBy { it.rawId }
                    .map { tagId -> tags.find { it.id == tagId } }

                assertThat(resource.tags, equalTo(expectedTags))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedule without tags, When collect, Then emit resource with empty tags`() = runTest {
        val schedule = genSchedule(
            content = genScheduleContent(tagIds = emptySet())
        )
        val useCase = genGetScheduleListResourcesStreamUseCase()
        useCase.invoke(schedulesStream = flowOf(setOf(schedule))).test {
            val actualResource = awaitItem().first()
            assertThat(actualResource.tags, equalTo(emptyList()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedules with links, When collect, Then emit resources with matching link metadata`() = runTest {
        val schedules = genSchedules()
        val links = schedules.mapNotNull { it.content.link }.toSet()
        val linkToMetadataTable = links.associateWith { genLinkMetadata() }
        val useCase = genGetScheduleListResourcesStreamUseCase(
            linkMetadataRepository = mockk {
                every { getLinkToMetadataTableAsStream(links) } returns flowOf(linkToMetadataTable)
            }
        )
        useCase.invoke(schedulesStream = flowOf(schedules.toSet())).test {
            val actualResources = awaitItem()
            val expectedIdToSchedule = schedules.associateBy { it.id }
            actualResources.forEach { resource ->
                val expectedMetadata = expectedIdToSchedule.getValue(key = resource.id)
                    .content
                    .link
                    ?.let { linkToMetadataTable[it] }
                assertThat(resource.linkMetadata, equalTo(expectedMetadata))
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given schedule without link, When collect, Then emit resource with null link metadata`() = runTest {
        val schedule = genSchedule(
            content = genScheduleContent(link = null)
        )
        val useCase = genGetScheduleListResourcesStreamUseCase()
        useCase.invoke(schedulesStream = flowOf(setOf(schedule))).test {
            val actualResource = awaitItem().first()
            assertThat(actualResource.linkMetadata, nullValue())

            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun genGetScheduleListResourcesStreamUseCase(
    tagRepository: TagRepository = mockk {
        every { getTagsAsStream(any()) } returns flowOf(emptySet())
    },
    linkMetadataRepository: LinkMetadataRepository = mockk {
        every { getLinkToMetadataTableAsStream(any()) } returns flowOf(emptyMap())
    }
): GetScheduleListResourcesStreamUseCase = GetScheduleListResourcesStreamUseCase(
    tagRepository = tagRepository,
    linkMetadataRepository = linkMetadataRepository
)