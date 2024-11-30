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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Thalys
 */
class TryUpdateTagNameUseCaseTest {
    private val inputTagId: TagId = TagId(1)
    private val inputName: NonBlankString = "A".toNonBlankString()

    @Test
    fun `Given id, unique name and snapshot tag group, When invoke, Then return success if repository save successfully`() = runTest {
        val expectedChangeTag = genTag(inputTagId, inputName)
        val tagGroups = List(genInt(min = 5, max = 10)) {
            genTag(id = TagId(it.toLong()), name = it.toString().toNonBlankString())
        }
        val tagRepository: TagRepository = mock {
            whenever(mock.save(SaveTagQuery.Modify(inputTagId, inputName))) doReturn Result.Success(expectedChangeTag)
        }
        val result = TryUpdateTagNameUseCase(tagRepository).invoke(
            inputTagId,
            inputName,
            TagGroupSource.Snapshot(tagGroups)
        )
        result as TryUpdateTagNameResult.Success
        assertThat(result.tag, equalTo(expectedChangeTag))
    }

    @Test
    fun `Given id, unique name and tag group, When invoke, Then return error if repository save failure`() = runTest {
       val expectedException = RuntimeException()
        val tagGroups = List(genInt(min = 5, max = 10)) {
            genTag(id = TagId(it.toLong()), name = it.toString().toNonBlankString())
        }
        val tagRepository: TagRepository = mock {
            whenever(mock.save(SaveTagQuery.Modify(inputTagId, inputName))) doReturn Result.Failure(expectedException)
        }
        val result = TryUpdateTagNameUseCase(tagRepository).invoke(
            inputTagId,
            inputName,
            TagGroupSource.Snapshot(tagGroups)
        )
        assertThat(result, equalTo(TryUpdateTagNameResult.UnknownError))
    }

    @Test
    fun `Given id, name and same input tag group, When invoke, Then return not changed`() = runTest {
        val expectedChangeTag = genTag(inputTagId, inputName)
        val tagGroups = listOf(expectedChangeTag)
        val result = TryUpdateTagNameUseCase(tagRepository = mock()).invoke(
            inputTagId,
            inputName,
            TagGroupSource.Snapshot(tagGroups)
        )
        assertThat(result, equalTo(TryUpdateTagNameResult.NotChanged))
    }

    @Test
    fun `Given id, duplicate name and tag group, When invoke, Then return duplicate name error`() = runTest {
        val expectedDuplicateTag = genTag(id = TagId(inputTagId.rawId + 1), name = inputName)
        val result = TryUpdateTagNameUseCase(tagRepository = mock()).invoke(
            inputTagId,
            inputName,
            TagGroupSource.Snapshot(listOf(expectedDuplicateTag))
        ) as TryUpdateTagNameResult.DuplicateNameError

        assertThat(result.duplicateTag, equalTo(expectedDuplicateTag))
    }
}