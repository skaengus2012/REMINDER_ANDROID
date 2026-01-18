/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genScheduleContent
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveBulkTagQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class EditScheduleContentInListUseCaseTest {
    @Test
    fun `Given new content, when use case invoked, then only title, note, and tags should be updated`() = runTest {
        val id = genScheduleId()
        val title = genNonBlankString()
        val note = genNonBlankString()
        val originalContent = genScheduleContent(tagIds = emptySet())
        val tags = genTags()
        val tagNames = tags.toSet { it.name }
        val scheduleModifyQuerySlot = slot<SaveScheduleQuery.Modify>()
        val useCase = genEditScheduleContentInListUseCase(
            scheduleRepository = mockk {
                coEvery {
                    save(capture(scheduleModifyQuerySlot))
                } returns Result.Success(genSchedule())
            },
            tagRepository = mockk {
                coEvery { saveBulk(query = SaveBulkTagQuery.Add(tagNames)) } returns Result.Success(tags)
            }
        )
        useCase.invoke(
            id = id,
            originContent = originalContent,
            title = title,
            note = note,
            tagNames = tagNames
        )

        val capturedContent = scheduleModifyQuerySlot.captured.content
        assertThat(capturedContent.title, equalTo(title))
        assertThat(capturedContent.note, equalTo(note))
        assertThat(capturedContent.tagIds, equalTo(tags.toSet { it.id }))

        val restoreContent = capturedContent.copy(
            title = originalContent.title,
            note = originalContent.note,
            tagIds = originalContent.tagIds
        )
        assertThat(restoreContent, equalTo(originalContent))
    }

    @Test
    fun `Given tag repository returns failure, when use case invoked, then it should return failure`() = runTest {
        val expectedException = RuntimeException()
        val useCase = genEditScheduleContentInListUseCase(
            tagRepository = mockk {
                coEvery { saveBulk(any()) } returns Result.Failure(expectedException)
            }
        )
        val ret = useCase.invoke(
            id = genScheduleId(),
            originContent = genScheduleContent(),
            title = genNonBlankString(),
            note = genNonBlankString(),
            tagNames = emptySet()
        )
        assertThat(ret.exceptionOrNull(), sameInstance(expectedException))
    }

    @Test
    fun `Given schedule repository returns failure, when use case invoked, then it should return failure`() = runTest {
        val expectedException = RuntimeException()
        val useCase = genEditScheduleContentInListUseCase(
            scheduleRepository = mockk {
                coEvery { save(any()) } returns Result.Failure(expectedException)
            }
        )
        val ret = useCase.invoke(
            id = genScheduleId(),
            originContent = genScheduleContent(),
            title = genNonBlankString(),
            note = genNonBlankString(),
            tagNames = emptySet()
        )
        assertThat(ret.exceptionOrNull(), sameInstance(expectedException))
    }
}

private fun genEditScheduleContentInListUseCase(
    scheduleRepository: ScheduleRepository = mockk {
        coEvery { save(any()) } returns Result.Success(genSchedule())
    },
    tagRepository: TagRepository = mockk {
        coEvery { saveBulk(any()) } returns Result.Success(genTags())
    }
) = EditScheduleContentInListUseCase(
    scheduleRepository = scheduleRepository,
    tagRepository = tagRepository
)

