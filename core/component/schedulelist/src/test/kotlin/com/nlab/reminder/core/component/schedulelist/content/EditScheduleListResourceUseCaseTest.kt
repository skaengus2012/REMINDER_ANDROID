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

import com.nlab.reminder.core.data.model.ScheduleContent
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genSchedule
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveBulkTagQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.toNonBlankString
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
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
class EditScheduleListResourceUseCaseTest {
    @Test
    fun `Given content is unchanged, when use case invoked, then repositories should not be called`() = runTest {
        suspend fun verify(originResource: ScheduleListResource) {
            val scheduleRepository: ScheduleRepository = mockk()
            val tagRepository: TagRepository = mockk()
            val useCase = genEditScheduleListResourceUseCase(
                scheduleRepository = scheduleRepository,
                tagRepository = tagRepository
            )
            val result = useCase.invoke(
                originResource = originResource,
                title = originResource.title,
                note = originResource.note,
                tagNames = originResource.tags.toSet { it.name }
            )
            assertThat(result.isSuccess, equalTo(true))
            coVerify { scheduleRepository wasNot Called }
            coVerify { tagRepository wasNot Called }
        }

        verify(originResource = genScheduleListResource())
        verify(originResource = genScheduleListResource(note = null))
    }

    @Test
    fun `Given new data, when use case invoked, then only title, note, and tags should be updated`() = runTest {
        suspend fun verify(
            originResource: ScheduleListResource,
            title: NonBlankString = originResource.title,
            note: NonBlankString? = originResource.note,
            tags: Set<Tag> = originResource.tags.toSet()
        ) {
            val tagNames = tags.toSet { it.name }
            val scheduleModifyQuerySlot = slot<SaveScheduleQuery.Modify>()
            val useCase = genEditScheduleListResourceUseCase(
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
                originResource = originResource,
                title = title,
                note = note,
                tagNames = tagNames
            )

            val capturedContent = scheduleModifyQuerySlot.captured.content
            assertThat(capturedContent.title, equalTo(title))
            assertThat(capturedContent.note, equalTo(note))
            assertThat(capturedContent.tagIds, equalTo(tags.toSet { it.id }))

            val restoreContent = capturedContent.copy(
                title = originResource.title,
                note = originResource.note,
                tagIds = originResource.tags.toSet { it.id }
            )
            assertThat(
                restoreContent,
                equalTo(
                    ScheduleContent(
                        title = originResource.title,
                        note = originResource.note,
                        tagIds = originResource.tags.toSet { it.id },
                        link = originResource.link,
                        timing = originResource.timing
                    )
                )
            )
        }

        val originResource = genScheduleListResource()
        verify(originResource = originResource, title = originResource.newTitle())
        verify(originResource = originResource, note = null)
        verify(originResource = originResource.copy(note = null), note = genNonBlankString())
        verify(originResource = originResource.copy(tags = emptyList()), tags = originResource.tags.toSet())
    }

    @Test
    fun `Given tag repository returns failure, when use case invoked, then it should return failure`() = runTest {
        val expectedException = RuntimeException()
        val useCase = genEditScheduleListResourceUseCase(
            tagRepository = mockk {
                coEvery { saveBulk(any()) } returns Result.Failure(expectedException)
            }
        )
        val originResource = genScheduleListResource()
        val ret = useCase.invoke(
            originResource = originResource,
            title = originResource.newTitle(),
            note = originResource.newNote(),
            tagNames = emptySet()
        )
        assertThat(ret.exceptionOrNull(), sameInstance(expectedException))
    }

    @Test
    fun `Given schedule repository returns failure, when use case invoked, then it should return failure`() = runTest {
        val expectedException = RuntimeException()
        val useCase = genEditScheduleListResourceUseCase(
            scheduleRepository = mockk {
                coEvery { save(any()) } returns Result.Failure(expectedException)
            }
        )
        val originResource = genScheduleListResource()
        val ret = useCase.invoke(
            originResource = genScheduleListResource(),
            title = originResource.newTitle(),
            note = originResource.newNote(),
            tagNames = emptySet()
        )
        assertThat(ret.exceptionOrNull(), sameInstance(expectedException))
    }
}

private fun genEditScheduleListResourceUseCase(
    scheduleRepository: ScheduleRepository = mockk {
        coEvery { save(any()) } returns Result.Success(genSchedule())
    },
    tagRepository: TagRepository = mockk {
        coEvery { saveBulk(any()) } returns Result.Success(genTags())
    }
) = EditScheduleListResourceUseCase(
    scheduleRepository = scheduleRepository,
    tagRepository = tagRepository
)

private fun ScheduleListResource.newTitle(): NonBlankString {
    return title.let { "new_${it.value}".toNonBlankString() }
}

private fun ScheduleListResource.newNote(): NonBlankString {
    return note.let { "new_${it?.value}".toNonBlankString() }
}

