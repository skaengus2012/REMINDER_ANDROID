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
import com.nlab.reminder.test.genBoolean
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScopedSelectionRepositoryTest {
    @Test
    fun `notify selectionTable when inputted`() = runTest {
        val schedule: Schedule = genSchedule()
        val isSelect: Boolean = genBoolean()
        val repository = ScopedSelectionRepository()
        val notEmptySelectionTable = async {
            repository.selectionTableStream()
                .filter { it.isNotEmpty() }
                .take(1)
                .first()
        }

        repository.setSelected(schedule.id, isSelect)
        assertThat(notEmptySelectionTable.await(), equalTo(mapOf(schedule.id to isSelect)))
    }

    @Test
    fun `set empty selectionTable when cleared`() = runTest {
        val selectedTable: SelectionTable =
            genScheduleUiStates(isSelected = true).associate { it.id to it.isSelected }
        val unSelectedTable: SelectionTable =
            genScheduleUiStates(isSelected = false).associate { it.id to it.isSelected }
        val repository = ScopedSelectionRepository(initTable = selectedTable + unSelectedTable)
        repository.clearSelected()

        assertThat(repository.selectionTableStream().value, equalTo(emptyMap()))
    }
}