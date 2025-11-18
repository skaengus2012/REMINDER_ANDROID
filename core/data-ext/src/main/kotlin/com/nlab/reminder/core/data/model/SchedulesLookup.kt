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

package com.nlab.reminder.core.data.model

/**
 * Provides efficient lookup operations for schedules by their IDs.
 *
 * This class allows quick access to {@link Schedule} instances using their {@link ScheduleId}.
 * @author Thalys
 */
data class SchedulesLookup(val values: Set<Schedule>) {
    private val table = values.associateBy { it.id }

    operator fun contains(id: ScheduleId): Boolean {
        return id in table
    }

    fun requireValue(id: ScheduleId): Schedule {
        return table.getValue(id)
    }
}