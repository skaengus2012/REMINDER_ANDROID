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
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.transaction.ReplaceTagTransaction
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
   /**
    @Test
    fun `Given non blanked name, When add, Then dao called insertAndGet`() = runTest {
        val (expectedTag, entity) = genTagAndEntity()
        val name = entity.name
        val tagDAO = mock<TagDAO> {
            whenever(mock.insertAndGet(name = name)) doReturn entity
        }
        val actualTag = genTagRepository(tagDAO = tagDAO)
            .save(SaveTagQuery.Add(name = name.toNonBlankString()))
            .getOrThrow()
        assertThat(actualTag, equalTo(expectedTag))
    }
*/
    /**
    @Test
    fun `Given tagId and non blanked name, When modify, Then dao called updateOrReplaceAndGet`() = runTest {
        val (expectedTag, entity) = genTagAndEntity()
        val id = entity.tagId
        val name = entity.name
        val tagRelationDAO = mock<TagRelationDAO> {
            whenever(mock.updateOrReplaceAndGet(tagId = id, name = name)) doReturn entity
        }
        val actualTag = genTagRepository(tagRelationDAO = tagRelationDAO)
            .save(SaveTagQuery.Modify(id = TagId(id), name = name.toNonBlankString()))
            .getOrThrow()
        assertThat(actualTag, equalTo(expectedTag))
    }*/

    @Test
    fun `Given tagId, When delete, Then dao called delete`() = runTest {
        val id = genTagId()
        val tagDAO: TagDAO = mock()
        genTagRepository(tagDAO = tagDAO)
            .delete(id)
            .getOrThrow()
        verify(tagDAO, once()).deleteById(id.rawId)
    }

    @Test
    fun `Given all getTagQuery, When getTagsAsStream, Then tags found from dao`() = runTest {
        val (expectedTag, entity) = genTagAndEntity()
        val tagRepository = genTagRepository(
            tagDAO = mock<TagDAO> { whenever(mock.getAsStream()) doReturn flowOf(arrayOf(entity)) }
        )

        val actualTag = tagRepository
            .getTagsAsStream(GetTagQuery.All)
            .first()
            .first()
        assertThat(actualTag, equalTo(expectedTag))
    }

    @Test
    fun `Given byIds getTagQuery, When getTagsAsStream, Then tags found from dao`() = runTest {
        val (expectedTag, entity) = genTagAndEntity()
        val tagRepository = genTagRepository(
            tagDAO = mock {
                whenever(mock.findByIdsAsStream(tagIds = setOf(expectedTag.id.rawId))) doReturn flowOf(arrayOf(entity))
            }
        )
        val actualTag = tagRepository
            .getTagsAsStream(GetTagQuery.ByIds(setOf(expectedTag.id)))
            .first()
            .first()
        assertThat(actualTag, equalTo(expectedTag))
    }
}

private fun genTagRepository(
    tagDAO: TagDAO = mock(),
    replaceTag: ReplaceTagTransaction = mock()
): TagRepository = LocalTagRepository(tagDAO, replaceTag)