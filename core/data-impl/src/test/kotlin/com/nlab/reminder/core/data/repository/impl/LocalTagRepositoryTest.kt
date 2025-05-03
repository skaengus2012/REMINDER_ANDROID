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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.*
import com.nlab.reminder.core.data.repository.*
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonNegativeInt
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.transaction.UpdateOrMergeAndGetTagTransaction
import com.nlab.testkit.faker.genBothify
import io.mockk.coEvery
import io.mockk.coVerify
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
class LocalTagRepositoryTest {
    @Test
    fun `Given add query, When save, Then insert trimmed text and return new tag`() = runTest {
        // Given
        val inputName = genBothify(" ?# ")
        val trimmedName = inputName.trim().toNonBlankString()
        val (expectedTag, entity) = genTagAndEntity(genTag(name = trimmedName))
        val query = SaveTagQuery.Add(name = inputName.toNonBlankString())

        // When
        val tagDAO: TagDAO = mockk {
            coEvery { insertAndGet(trimmedName) } returns entity
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO)
        val actualTag = repository.save(query)

        // Then
        coVerify(exactly = 1) { tagDAO.insertAndGet(trimmedName) }
        assertThat(actualTag.getOrThrow(), equalTo(expectedTag))
    }

    @Test
    fun `Given modify query with not merge, When save, Then update trimmed text and return updated tag`() = runTest {
        // Given
        val inputTagId = genTagId()
        val inputName = genBothify(" ?# ")
        val trimmedName = inputName.trim().toNonBlankString()
        val (expectedTag, entity) = genTagAndEntity(genTag(id = inputTagId, name = trimmedName))
        val query = SaveTagQuery.Modify(id = inputTagId, name = inputName.toNonBlankString(), shouldMergeIfExists = false)

        // When
        val tagDAO: TagDAO = mockk {
            coEvery { updateAndGet(inputTagId.rawId, trimmedName) } returns entity
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO)
        val actualTag = repository.save(query)

        // Then
        coVerify(exactly = 1) { tagDAO.updateAndGet(inputTagId.rawId, trimmedName) }
        assertThat(actualTag.getOrThrow(), equalTo(expectedTag))
    }

    @Test
    fun `Given modify query with merge, When save, Then update or merge trimmed text, return updated tag`() = runTest {
        // Given
        val inputTagId = genTagId()
        val inputName = genBothify(" ?# ")
        val trimmedName = inputName.trim().toNonBlankString()
        val (expectedTag, entity) = genTagAndEntity(genTag(id = inputTagId, name = trimmedName))
        val query = SaveTagQuery.Modify(id = inputTagId, name = inputName.toNonBlankString(), shouldMergeIfExists = true)

        // When
        val updateOrMergeAndGetTagTransaction: UpdateOrMergeAndGetTagTransaction = mockk {
            coEvery { invoke(tagId = inputTagId.rawId, name = trimmedName) } returns entity
        }
        val repository = genLocalTagRepository(updateOrMergeAndGetTag = updateOrMergeAndGetTagTransaction)
        val actualTag = repository.save(query)

        // Then
        coVerify(exactly = 1) { updateOrMergeAndGetTagTransaction.invoke(inputTagId.rawId, trimmedName) }
        assertThat(actualTag.getOrThrow(), equalTo(expectedTag))
    }

    @Test
    fun `Given tagId, When delete, Then dao call deleteById and return success`() = runTest {
        // Given
        val tagId = genTagId()

        // When
        val tagDAO: TagDAO = mockk(relaxed = true)
        val repository = genLocalTagRepository(tagDAO = tagDAO)
        val result = repository.delete(tagId)

        // Then
        coVerify(exactly = 1) { tagDAO.deleteById(tagId.rawId) }
        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `Given tagId, When getting usage count, Then returns correct count`() = runTest {
        // Given
        val tagId = genTagId()
        val expectedUsageCount = genNonNegativeInt()

        // When
        val scheduleTagListDAO: ScheduleTagListDAO = mockk {
            coEvery { findScheduleIdCountByTagId(tagId.rawId) } returns expectedUsageCount.value
        }
        val repository = genLocalTagRepository(scheduleTagListDAO = scheduleTagListDAO)
        val actualUsageCount = repository.getUsageCount(tagId)

        // Then
        assertThat(
            actualUsageCount,
            equalTo(Result.Success(expectedUsageCount))
        )
    }

    @Test
    fun `Given used tags exist, When querying OnlyUsed, Then only used tags are returned`() = runTest {
        // Given
        val tagAndEntities = genTagAndEntities()
        val query = GetTagQuery.OnlyUsed

        // When
        val tagDAO: TagDAO = mockk {
            every { getAsStream() } returns flowOf(tagAndEntities.map { (_, entity) -> entity })
        }
        val scheduleTagListDAO: ScheduleTagListDAO = mockk {
            every { getAllTagIdsAsStream() } returns flowOf(tagAndEntities.map { (tag) -> tag.id.rawId })
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO, scheduleTagListDAO = scheduleTagListDAO)
        val actualTags = repository.getTagsAsStream(query).first()

        // Then
        assertThat(actualTags, equalTo(tagAndEntities.toSet { it.first }))
    }

    @Test
    fun `Given no used tags, When querying OnlyUsed, Then returns empty set`() = runTest {
        // Given
        val tagAndEntities = genTagAndEntities()
        val query = GetTagQuery.OnlyUsed

        // When
        val tagDAO: TagDAO = mockk {
            every { getAsStream() } returns flowOf(tagAndEntities.map { (_, entity) -> entity })
        }
        val scheduleTagListDAO: ScheduleTagListDAO = mockk {
            every { getAllTagIdsAsStream() } returns flowOf(emptyList())
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO, scheduleTagListDAO = scheduleTagListDAO)
        val actualTagUsages = repository.getTagsAsStream(query).first()

        // Then
        assertThat(actualTagUsages, equalTo(emptySet()))
    }

    @Test
    fun `Given tagIds and entities, When querying by ids, Then returns matching tags`() = runTest {
        // Given
        val tagAndEntities = genTagAndEntities()
        val tagIds = tagAndEntities.toSet { (tag) -> tag.id }
        val rawTagIds = tagAndEntities.toSet { (tag) -> tag.id.rawId }
        val expectedTags = tagAndEntities.toSet { it.first }
        val query = GetTagQuery.ByIds(tagIds)

        // When
        val tagDAO: TagDAO = mockk {
            every { findByIdsAsStream(rawTagIds) } returns flowOf(tagAndEntities.map { (_, entity) -> entity })
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO)
        val actualTags = repository.getTagsAsStream(query).first()

        // Then
        assertThat(actualTags, equalTo(expectedTags))
    }
}

private fun genLocalTagRepository(
    tagDAO: TagDAO = mockk(),
    scheduleTagListDAO: ScheduleTagListDAO = mockk(),
    updateOrMergeAndGetTag: UpdateOrMergeAndGetTagTransaction = mockk()
) = LocalTagRepository(tagDAO, scheduleTagListDAO, updateOrMergeAndGetTag)