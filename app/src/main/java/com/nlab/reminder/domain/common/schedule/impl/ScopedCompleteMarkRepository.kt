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

import com.nlab.reminder.core.util.transaction.TransactionId
import com.nlab.reminder.domain.common.schedule.CompleteMark
import com.nlab.reminder.domain.common.schedule.CompleteMarkRepository
import com.nlab.reminder.domain.common.schedule.ScheduleId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe CompleteMark Manager
 *
 * @author Doohyun
 */
class ScopedCompleteMarkRepository : CompleteMarkRepository {
    private val completeMarks: MutableStateFlow<Map<ScheduleId, CompleteMark>> = MutableStateFlow(emptyMap())

    override fun get(): Flow<Map<ScheduleId, CompleteMark>> = completeMarks.asStateFlow()

    override suspend fun insert(scheduleId: ScheduleId, completeMark: CompleteMark) {
        completeMarks.update { snapshot -> snapshot + (scheduleId to completeMark) }
    }

    override suspend fun delete(scheduleId: ScheduleId, transactionId: TransactionId) {
        completeMarks.update { snapshot ->
            if (snapshot[scheduleId]?.txId == transactionId) snapshot - scheduleId
            else snapshot
        }
    }
}