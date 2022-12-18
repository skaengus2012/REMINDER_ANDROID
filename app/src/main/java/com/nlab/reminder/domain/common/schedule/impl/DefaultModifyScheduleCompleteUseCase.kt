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

import com.nlab.reminder.core.kotlin.coroutine.Delay
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.util.transaction.TransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * @author Doohyun
 */
class DefaultModifyScheduleCompleteUseCase(
    private val transactionIdGenerator: TransactionIdGenerator,
    private val scheduleRepository: ScheduleRepository,
    private val completeMarkRepository: CompleteMarkRepository,
    private val delayUntilTransactionPeriod: Delay,
    private val dispatcher: CoroutineDispatcher
) : ModifyScheduleCompleteUseCase {
    override suspend fun invoke(scheduleId: ScheduleId, isComplete: Boolean): Result<Unit> = withContext(dispatcher) {
        completeMarkRepository.insert(completeMarkTableOf(scheduleId, isComplete))
        delayUntilTransactionPeriod()

        completeMarkRepository.get()
            .value
            .filterNot { it.value.isApplied }
            .also { snapshot -> completeMarkRepository.updateToApplied(snapshot) }
            .let { snapshot -> commitCompleteMarkTableToSchedule(snapshot) }
    }

    private fun completeMarkTableOf(scheduleId: ScheduleId, isComplete: Boolean): CompleteMarkTable =
        mapOf(
            scheduleId to CompleteMark(
                isComplete,
                isApplied = false,
                transactionId = transactionIdGenerator.generate()
            )
        )

    private suspend fun commitCompleteMarkTableToSchedule(table: CompleteMarkTable): Result<Unit> =
        if (table.isEmpty()) Result.Success(Unit)
        else scheduleRepository.update(
            UpdateRequest.Completes(table.map { ModifyCompleteRequest(it.key, it.value.isComplete) })
        )
}