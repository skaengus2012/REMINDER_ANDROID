/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class UserSelectedSchedulesStoreTest {
    @Test
    fun `Given store created, When state observed, Then selected is empty`() {
        val store = UserSelectedSchedulesStore()
        assertThat(store.selectedIds.value, equalTo(emptySet()))
    }

    @Test
    fun `Given scheduleIds, When replace invoked, Then selectedIds is updated`() {
        val store = UserSelectedSchedulesStore()
        val scheduleIds = List(size = genInt(min = 5, max = 10)) { index -> ScheduleId(rawId = index.toLong() + 1) }

        store.replace(selectedIds = scheduleIds.shuffled().toSet())
        assertThat(store.selectedIds.value, equalTo(scheduleIds.toSet()))

        store.replace(selectedIds = emptySet())
        assertThat(store.selectedIds.value, equalTo(emptySet()))
    }
}