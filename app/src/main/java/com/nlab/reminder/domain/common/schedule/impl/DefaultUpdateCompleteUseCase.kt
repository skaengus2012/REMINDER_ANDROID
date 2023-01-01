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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.math.max

/**
 * @author Doohyun
 */
class DefaultUpdateCompleteUseCase(
    private val transactionIdGenerator: TransactionIdGenerator,
    private val scheduleRepository: ScheduleRepository,
    private val completeMarkRepository: CompleteMarkRepository,
    private val delayUntilTransactionPeriod: Delay,
) : UpdateCompleteUseCase {
    private val requestCount = MutableStateFlow(0)

    // Because of jacoco, I used coroutineScope.
    override suspend fun invoke(scheduleId: ScheduleId, isComplete: Boolean): Result<Unit> = coroutineScope {
        completeMarkRepository.insert(completeMarkTableOf(scheduleId, isComplete))
        requestCount.update { count -> count + 1 }
        delayUntilTransactionPeriod()

        val hasPendingRequest: Boolean =
            requestCount.updateAndGet { count -> max(count - 1, 0) } > 0
        if (hasPendingRequest) Result.Success(Unit)
        else commitCompleteMarksToSchedule()
    }

    private fun completeMarkTableOf(scheduleId: ScheduleId, isComplete: Boolean): CompleteMarkTable =
        mapOf(
            scheduleId to CompleteMark(
                isComplete,
                isApplied = false,
                transactionId = transactionIdGenerator.generate()
            )
        )

    private suspend fun commitCompleteMarksToSchedule(): Result<Unit> =
        completeMarkRepository.get()
            .value
            .filterNot { it.value.isApplied }
            .also { table -> completeMarkRepository.updateToApplied(table) }
            .let { table ->
                if (table.isEmpty()) Result.Success(Unit)
                else scheduleRepository.update(
                    UpdateRequest.Completes(table.map { ModifyCompleteRequest(it.key, it.value.isComplete) })
                )
            }
}