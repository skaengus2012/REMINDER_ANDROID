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
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * @author thalys
 */
class InMemoryScheduleCompleteMarkRepository @Inject constructor() : ScheduleCompleteMarkRepository {
    private val chunkRequests = MutableStateFlow(emptyMap<ScheduleId, Boolean>())

    override fun getStream(): StateFlow<Map<ScheduleId, Boolean>> = chunkRequests.asStateFlow()

    override fun add(scheduleId: ScheduleId, isComplete: Boolean) {
        chunkRequests.update { old -> old + (scheduleId to isComplete) }
    }
}