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

package com.nlab.reminder.domain.common.schedule.impl

import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.test.genInt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCompleteMarkRepositoryTest {
    @Test
    fun `insert executed on io thread`() = runTest {
        val repository = DefaultCompleteMarkRepository()
        val count = genInt()
        (0 until count)
            .map { ScheduleId(it.toLong()) }
            .map { scheduleId ->
                launch(Dispatchers.IO) {
                    repository.insert(CompleteMarkTable(scheduleId to genCompleteMark()))
                }
            }
            .joinAll()

        assertThat(repository.get().first().size, equalTo(count))
    }

    @Test
    fun `after cleared, notify emptyMark`() = runTest {
        val repository = DefaultCompleteMarkRepository()
        List(genInt()) { index -> CompleteMarkTable(ScheduleId(index.toLong()) to genCompleteMark()) }
            .forEach { table -> repository.insert(table) }
        repository.clear()

        assertThat(repository.get().first().size, equalTo(0))
    }
}