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

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class InMemoryScheduleDetailsSelectedIdRepositoryTest {
    @Test
    fun `When selected, Then return added id`() = runTest {
        val repository = InMemoryScheduleSelectedIdRepository()
        val id = genScheduleId()
        repository.update(id, isSelected = true)

        assertThat(
            repository.getStream().value,
            equalTo(setOf(id))
        )
    }

    @Test
    fun `When unselected, Then return deleted id`() = runTest {
        val ids = List(genInt(min = 2, max = 10)) { ScheduleId(it.toLong()) }
        val repository = InMemoryScheduleSelectedIdRepository().apply {
            ids.forEach { update(it, isSelected = true) }
        }
        val deleteTarget = ids.first()

        repository.update(deleteTarget, isSelected = false)
        assertThat(
            repository.getStream().value,
            equalTo(ids.subList(1, ids.size).toSet())
        )
    }

    @Test
    fun `When cleared, Then return empty`() = runTest {
        val ids = List(genInt(min = 2, max = 10)) { ScheduleId(it.toLong()) }
        val repository = InMemoryScheduleSelectedIdRepository().apply {
            ids.forEach { update(it, isSelected = true) }
        }

        repository.clear()
        assertThat(
            repository.getStream().value,
            equalTo(emptySet())
        )
    }
}