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

import com.nlab.reminder.core.data.local.database.*
import com.nlab.reminder.core.data.model.*
import com.nlab.reminder.core.data.repository.*
import com.nlab.reminder.core.kotlin.*
import com.nlab.reminder.core.local.database.*
import com.nlab.testkit.faker.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
internal class LocalTagRepositoryTest {
    @Test
    fun `Given tag with empty tagId, When saved tag, dao called insert`() = runTest {
        val expectedNewId = genTagId()
        val expectedName = genBothify()
        val tagDao = mock<TagDao> {
            whenever(mock.insert(TagEntity(name = expectedName))) doReturn expectedNewId.value
            whenever(mock.findByIds(listOf(expectedNewId.value))) doReturn listOf(
                TagEntity(
                    tagId = expectedNewId.value,
                    name = expectedName
                )
            )
        }
        val actualTag = genTagRepository(tagDao = tagDao)
            .save(Tag(id = TagId.Empty, name = expectedName))
            .getOrThrow()
        verify(tagDao, once()).insert(TagEntity(name = expectedName))
        assert(actualTag == genTag(expectedNewId, expectedName))
    }

    @Test
    fun `Given tag, When saved tag, dao called update`() = runTest {
        val expectedTag = genTag()
        val tagDao = mock<TagDao> {
            whenever(mock.findByIds(listOf(expectedTag.id.value))) doReturn listOf(expectedTag.toEntity())
        }
        val actualTag = genTagRepository(tagDao = tagDao)
            .save(expectedTag)
            .getOrThrow()
        verify(tagDao, once()).update(expectedTag.toEntity())
        assert(actualTag == expectedTag)
    }

    @Test(expected = IllegalStateException::class)
    fun `Given tag saving is successful, but not found tag, When saved tag, throw exception`() = runTest {
        val expectedTag = genTag()
        val tagDao = mock<TagDao> {
            whenever(mock.findByIds(listOf(expectedTag.id.value))) doReturn emptyList()
        }
        genTagRepository(tagDao = tagDao)
            .save(expectedTag)
            .getOrThrow()
    }

    @Test
    fun `When tag delete, Then dao called deleteById`() = runTest {
        val id = genTagId()
        val tagDao: TagDao = mock()

        genTagRepository(tagDao = tagDao).delete(id)
        verify(tagDao, once()).deleteById(id.value)
    }

    @Test
    fun `Get tags from dao`() = runTest {
        val tags = genTags()
        val tagEntities = tags.toEntities()

        testGet(
            tagDao = mock<TagDao> { whenever(mock.get()) doReturn tagEntities },
            query = TagGetQuery.All,
            expectedResult = tags
        )
        testGet(
            tagDao = mock<TagDao> { whenever(mock.findByIds(tags.map { it.id.value })) doReturn tagEntities },
            query = TagGetQuery.ByIds(tags.map { it.id }),
            expectedResult = tags
        )
    }

    @Test
    fun `Get tags stream from dao`() = runTest {
        val tags = genTags()
        val tagEntities = tags.toEntities()

        testGetAsStream(
            tagDao = mock<TagDao> { whenever(mock.getAsStream()) doReturn flowOf(tagEntities) },
            query = TagGetQuery.All,
            expectedResult = tags
        )

        testGetAsStream(
            tagDao = mock<TagDao> {
                whenever(mock.findByIdsAsStream(tags.map { it.id.value })) doReturn flowOf(tagEntities)
            },
            query = TagGetQuery.ByIds(tags.map { it.id }),
            expectedResult = tags
        )
    }

    @Test
    fun `Get tag usage count from dao`() = runTest {
        val id = genTagId()
        val usageCount: Long = genLong()
        val scheduleTagListDao: ScheduleTagListDao = mock {
            whenever(mock.findTagUsageCount(id.value)) doReturn usageCount
        }
        val result = genTagRepository(scheduleTagListDao = scheduleTagListDao).getUsageCount(id)

        assertThat(
            result,
            equalTo(Result.Success(usageCount))
        )
    }
}

private fun genTagRepository(
    tagDao: TagDao = mock(),
    scheduleTagListDao: ScheduleTagListDao = mock()
): TagRepository = LocalTagRepository(tagDao, scheduleTagListDao)

private suspend fun testGet(
    tagDao: TagDao,
    query: TagGetQuery,
    expectedResult: List<Tag>
) {
    val tagRepository = genTagRepository(tagDao = tagDao)
    val actualTags = tagRepository.getTags(query)
    assert(actualTags.getOrThrow() == expectedResult)
}

private suspend fun testGetAsStream(
    tagDao: TagDao,
    query: TagGetQuery,
    expectedResult: List<Tag>
) {
    val tagRepository = genTagRepository(tagDao = tagDao)
    val actualTags = tagRepository.getTagsAsStream(query)
    assert(actualTags.first() == expectedResult)
}