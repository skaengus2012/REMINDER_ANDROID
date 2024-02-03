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

package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.testkit.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class ScheduleSelectionRepositoryTest {
    @Test
    fun `When selected, Then return added id`() {
        val repository = ScheduleSelectionRepository()
        val id = genScheduleId()
        repository.selected(id)

        assertThat(
            repository.getSelectedIdsStream().value,
            equalTo(setOf(id))
        )
    }

    @Test
    fun `When unselected, Then return deleted id`() {
        val ids = List(genInt(min = 2, max = 10)) { genScheduleId(it.toLong()) }
        val repository = ScheduleSelectionRepository().apply {
            ids.forEach { selected(it) }
        }
        val deleteTarget = ids.first()

        repository.unselected(deleteTarget)
        assertThat(
            repository.getSelectedIdsStream().value,
            equalTo(ids.subList(1, ids.size).toSet())
        )
    }

    @Test
    fun `When cleared, Then return empty`() {
        val ids = List(genInt(min = 2, max = 10)) { genScheduleId(it.toLong()) }
        val repository = ScheduleSelectionRepository().apply {
            ids.forEach { selected(it) }
        }

        repository.clear()
        assertThat(
            repository.getSelectedIdsStream().value,
            equalTo(emptySet())
        )
    }

}