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
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.isSuccess
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.transaction.UpdateOrReplaceAndGetTagTransaction
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
    fun `Given modify query, When save, Then updateOrReplace trimmed text and return updated tag`() = runTest {
        // Given
        val inputTagId = genTagId()
        val inputName = genBothify(" ?# ")
        val trimmedName = inputName.trim().toNonBlankString()
        val (expectedTag, entity) = genTagAndEntity(genTag(id = inputTagId, name = trimmedName))
        val query = SaveTagQuery.Modify(id = inputTagId, name = inputName.toNonBlankString())

        // When
        val updateOrReplaceAndGetTagTransaction: UpdateOrReplaceAndGetTagTransaction = mockk {
            coEvery { invoke(tagId = inputTagId.rawId, name = trimmedName) } returns entity
        }
        val repository = genLocalTagRepository(updateOrReplaceAndGetTag = updateOrReplaceAndGetTagTransaction)
        val actualTag = repository.save(query)

        // Then
        coVerify(exactly = 1) { updateOrReplaceAndGetTagTransaction.invoke(inputTagId.rawId, trimmedName) }
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
    fun `Given tagIds based query, When collect tags, Then return all matching tags from dao`() = runTest {
        // Given
        val tagAndEntities = genTagAndEntities()
        val tagIds = tagAndEntities.toSet { (tag) -> tag.id }
        val rawTagIds = tagAndEntities.toSet { (tag) -> tag.id.rawId }
        val expectedTags = tagAndEntities.toSet { it.first }
        val query = GetTagQuery.ByIds(tagIds)

        // When
        val tagDAO: TagDAO = mockk {
            val resultEntities = tagAndEntities.map { (_, entity) -> entity }.toTypedArray()
            every { findByIdsAsStream(rawTagIds) } returns flowOf(resultEntities)
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO)
        val actualTags = repository.getTagsAsStream(query).first()

        // Then
        assertThat(actualTags, equalTo(expectedTags))
    }

    @Test
    fun `Given all query, When collect tagUsages, Then return tagUsages from dao`() = runTest {
        // Given
        val tagAndEntities = genTagAndEntities()
        val expectedTagUsages = genTagUsages(tags = tagAndEntities.map { it.first })
        val rawTagIds = tagAndEntities.toSet { (tag) -> tag.id.rawId }
        val query = GetTagUsageQuery.All

        // When
        val tagDAO: TagDAO = mockk {
            every { getAsStream() } returns flowOf(tagAndEntities.map { (_, entity) -> entity }.toTypedArray())
        }
        val scheduleTagListDAO: ScheduleTagListDAO = mockk {
            every { findByTagIdsAsStream(rawTagIds) } returns flowOf(
                expectedTagUsages
                    .toScheduleTagListEntities()
                    .toTypedArray()
            )
        }
        val repository = genLocalTagRepository(tagDAO = tagDAO, scheduleTagListDAO = scheduleTagListDAO)
        val actualTagUsages = repository.getTagUsagesAsStream(query).first()

        // Then
        assertThat(actualTagUsages, equalTo(expectedTagUsages))
    }
}

private fun genLocalTagRepository(
    tagDAO: TagDAO = mockk(),
    scheduleTagListDAO: ScheduleTagListDAO = mockk(),
    updateOrReplaceAndGetTag: UpdateOrReplaceAndGetTagTransaction = mockk()
) = LocalTagRepository(tagDAO, scheduleTagListDAO, updateOrReplaceAndGetTag)