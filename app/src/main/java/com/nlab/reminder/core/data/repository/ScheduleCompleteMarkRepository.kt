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

import com.nlab.reminder.core.data.model.ScheduleId
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author thalys
 */
@ViewModelScoped
class ScheduleCompleteMarkRepository @Inject constructor() {
    private val chunkRequests = MutableStateFlow(persistentHashMapOf<ScheduleId, Boolean>())
    fun getStream(): StateFlow<Map<ScheduleId, Boolean>> = chunkRequests.asStateFlow()
    fun add(scheduleId: ScheduleId, isComplete: Boolean) {
        chunkRequests.update { old -> old.put(scheduleId, isComplete) }
    }
}

fun ScheduleCompleteMarkRepository.getSnapshot(): Map<ScheduleId, Boolean> =
    getStream().value