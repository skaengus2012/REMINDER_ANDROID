/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.tag

import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.internal.common.android.database.ScheduleTagListDao
import com.nlab.reminder.internal.common.android.database.TagDao
import com.nlab.reminder.internal.common.database.from
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalTagRepositoryTest {
    private fun createTagRepository(
        tagDao: TagDao = mock(),
        scheduleTagListDao: ScheduleTagListDao = mock(),
        dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
    ): TagRepository = LocalTagRepository(tagDao, scheduleTagListDao, dispatcher)

    @Test
    fun `tagDao found when get`() {
        val tagDao: TagDao = mock()
        val tagRepository: TagRepository = createTagRepository(tagDao = tagDao)
        tagRepository.get()
        verify(tagDao, times(1)).find()
    }

    @Test
    fun `notify tag list when dao updated`() = runTest {
        val firstTags: List<Tag> = listOf(genTag())
        val secondTags: List<Tag> = listOf(genTag(), genTag(), genTag()).sortedBy { it.name }.reversed()
        val tagDao: TagDao = mock {
            whenever(mock.find()) doReturn flow {
                emit(firstTags.map { from(it) })
                delay(500)
                emit(secondTags.map { from(it) })
            }
        }
        val actualTags = mutableListOf<List<Tag>>()
        val tagRepository: TagRepository = createTagRepository(
            tagDao = tagDao,
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        CoroutineScope(Dispatchers.Unconfined).launch {
            tagRepository.get().collect { actualTags += it }
        }
        advanceTimeBy(1_000)
        assertThat(actualTags, equalTo(listOf(firstTags, secondTags)))
    }

    @Test
    fun `scheduleListDao found tag usage count when repository request findUsageCount`() = runTest {
        val testTag: Tag = genTag()
        val scheduleTagListDao: ScheduleTagListDao = mock()
        val tagRepository: TagRepository = createTagRepository(scheduleTagListDao = scheduleTagListDao)

        tagRepository.getUsageCount(testTag)
        verify(scheduleTagListDao, times(1)).findTagUsageCount(tagId = testTag.tagId)
    }
}