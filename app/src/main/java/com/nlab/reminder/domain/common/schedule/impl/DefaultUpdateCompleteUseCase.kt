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
import com.nlab.reminder.core.util.transaction.TransactionIdGenerator
import com.nlab.reminder.domain.common.schedule.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * @author Doohyun
 */
class DefaultUpdateCompleteUseCase(
    private val transactionIdGenerator: TransactionIdGenerator,
    private val scheduleRepository: ScheduleRepository,
    private val completeMarkRepository: CompleteMarkRepository,
    private val delayUntilTransactionPeriod: Delay,
    private val dispatcher: CoroutineDispatcher
) : UpdateCompleteUseCase {
    override suspend fun invoke(scheduleId: ScheduleId, isComplete: Boolean): Result<Unit> = withContext(dispatcher) {
        completeMarkRepository.insert(completeMarkGroupOf(scheduleId, isComplete))
        delayUntilTransactionPeriod()

        getNotAppliedCompleteMarkSnapshot()
            .also { snapshot -> completeMarkRepository.updateToApplied(snapshot) }
            .let { snapshot -> commitCompleteMarkSnapshotToSchedule(snapshot) }
    }

    private fun completeMarkGroupOf(scheduleId: ScheduleId, isComplete: Boolean): Map<ScheduleId, CompleteMark> =
        mapOf(
            scheduleId to CompleteMark(
                isComplete,
                isApplied = false,
                transactionId = transactionIdGenerator.generate()
            )
        )

    private suspend fun getNotAppliedCompleteMarkSnapshot(): Map<ScheduleId, CompleteMark> =
        completeMarkRepository.get()
            .firstOrNull()
            ?.filterNot { it.value.isApplied }
            ?: emptyMap()

    private suspend fun commitCompleteMarkSnapshotToSchedule(snapshot: Map<ScheduleId, CompleteMark>): Result<Unit> =
        if (snapshot.isEmpty()) Result.Success(Unit)
        else scheduleRepository.updateComplete(
            requests = snapshot
                .map { ScheduleCompleteRequest(it.key, it.value.isComplete) }
                .toSet()
        )
}