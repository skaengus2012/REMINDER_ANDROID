/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.statekit.checkWhileStateUsed
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.reminder.domain.common.data.model.genTags
import com.nlab.reminder.test.unconfinedCoroutineScope
import com.nlab.statekit.util.buildDslInterceptor
import com.nlab.statekit.util.createStore
import com.nlab.testkit.genInt
import com.nlab.testkit.genLong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class HomeEpicTest {
    @Test
    fun `Repository used while subscribed`() = runTest {
        val todaySchedulesCount = MutableStateFlow(0L)
        val timetableSchedulesCount = MutableStateFlow(0L)
        val allSchedulesCount = MutableStateFlow(0L)
        val tags = MutableStateFlow<List<Tag>>(emptyList())
        val store = createStore(
            unconfinedCoroutineScope(),
            HomeUiState.Loading,
            epic = HomeEpic(
                tagRepository = mock {
                    whenever(mock.get()) doReturn tags
                },
                scheduleRepository = mock {
                    whenever(mock.getTodaySchedulesCount()) doReturn todaySchedulesCount
                    whenever(mock.getTimetableSchedulesCount()) doReturn timetableSchedulesCount
                    whenever(mock.getAllSchedulesCount()) doReturn allSchedulesCount
                }
            )
        )
        store.checkWhileStateUsed(
            todaySchedulesCount,
            timetableSchedulesCount,
            allSchedulesCount,
            tags,
        )
    }

    @Test
    fun `Summary Loaded, when dependencies data changed`() = runTest {
        val todaySchedulesCount = genLong()
        val timetableSchedulesCount = genLong()
        val allSchedulesCount = genLong()
        val tags = genTags(count = genInt(min = 1, max = 3))

        val awaitSummaryLoadedReceived = CompletableDeferred<HomeAction.SummaryLoaded>()
        val store = createStore(
            unconfinedCoroutineScope(),
            HomeUiState.Loading,
            interceptor = buildDslInterceptor {
                action {
                    anyState { awaitSummaryLoadedReceived.complete(it.action) }
                }
            },
            epic = HomeEpic(
                tagRepository = mock {
                    whenever(mock.get()) doReturn flowOf(tags)
                },
                scheduleRepository = mock {
                    whenever(mock.getTodaySchedulesCount()) doReturn flowOf(todaySchedulesCount)
                    whenever(mock.getTimetableSchedulesCount()) doReturn flowOf(timetableSchedulesCount)
                    whenever(mock.getAllSchedulesCount()) doReturn flowOf(allSchedulesCount)
                }
            )
        )
        val collectJob = launch { store.state.collect() }
        val result = awaitSummaryLoadedReceived.await()

        assertThat(
            result,
            equalTo(
                HomeAction.SummaryLoaded(
                    todaySchedulesCount = todaySchedulesCount,
                    timetableSchedulesCount = timetableSchedulesCount,
                    allSchedulesCount = allSchedulesCount,
                    tags = tags
                )
            )
        )
        collectJob.cancelAndJoin()
    }
}