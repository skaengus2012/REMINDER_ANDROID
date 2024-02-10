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

package com.nlab.reminder.core.data.repository

import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author thalys
 */
class InMemoryScheduleSelectedIdRepository @Inject constructor() : ScheduleSelectedIdRepository {
    private val selectedIds = MutableStateFlow(persistentHashSetOf<ScheduleId>())

    override fun getStream(): StateFlow<Set<ScheduleId>> = selectedIds.asStateFlow()
    override suspend fun update(id: ScheduleId, isSelected: Boolean) {
        selectedIds.update { old -> if (isSelected) old.add(id) else old.remove(id) }
    }

    override suspend fun clear() {
        selectedIds.update { it.clear() }
    }
}