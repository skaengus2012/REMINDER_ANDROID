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
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.reminder.internal.common.android.database.ScheduleTagListDao
import com.nlab.reminder.internal.common.android.database.TagDao
import com.nlab.reminder.internal.common.android.database.toEntity
import com.nlab.reminder.internal.common.database.toEntities
import com.nlab.reminder.test.genFlowExecutionDispatcher
import com.nlab.reminder.test.genFlowObserveDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        scheduleTagListDao: ScheduleTagListDao = mock()
    ): TagRepository = LocalTagRepository(tagDao, scheduleTagListDao)

    @Test
    fun `tagDao found when get`() {
        val tagDao: TagDao = mock()
        val tagRepository: TagRepository = createTagRepository(tagDao = tagDao)
        tagRepository.get()
        verify(tagDao, times(1)).find()
    }

    @Test
    fun `notify tag list when dao updated`() = runTest {
        val executeDispatcher = genFlowExecutionDispatcher(testScheduler)
        val actualTags = mutableListOf<List<Tag>>()
        val firstTags: List<Tag> = listOf(genTag())
        val secondTags: List<Tag> = genTags().sortedBy { it.name }.reversed()
        val tagDao: TagDao = mock {
            val mockFlow = flow {
                emit(firstTags.toEntities())

                delay(500)
                emit(secondTags.toEntities())
            }
            whenever(mock.find()) doReturn mockFlow.flowOn(executeDispatcher)
        }

        createTagRepository(tagDao = tagDao)
            .get()
            .onEach(actualTags::add)
            .launchIn(genFlowObserveDispatcher())

        advanceTimeBy(1_000)
        assertThat(actualTags, equalTo(listOf(firstTags, secondTags)))
    }

    @Test
    fun `scheduleListDao found tag usage count when repository request findUsageCount`() = runTest {
        val input: Tag = genTag()
        val scheduleTagListDao: ScheduleTagListDao = mock()
        val tagRepository: TagRepository = createTagRepository(scheduleTagListDao = scheduleTagListDao)

        tagRepository.getUsageCount(input)
        verify(scheduleTagListDao, times(1)).findTagUsageCount(tagId = input.tagId)
    }

    @Test
    fun `tagDao delete tag when repository deleting requested`() = runTest {
        val input: Tag = genTag()
        val tagDao: TagDao = mock()
        val tagRepository: TagRepository = createTagRepository(tagDao = tagDao)

        tagRepository.delete(input)
        verify(tagDao, times(1)).delete(input.toEntity())
    }
}