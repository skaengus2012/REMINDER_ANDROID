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

package com.nlab.reminder.core.component.schedule.infra

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nlab.reminder.core.component.schedule.RegisterScheduleCompleteJobUseCase
import com.nlab.reminder.core.component.schedule.ScheduleJobResult
import com.nlab.reminder.core.data.repository.GetScheduleCompletionBacklogQuery
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.kotlin.tryToNonNegativeLongOrNull
import dagger.Reusable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * @author Thalys
 */
private const val KEY_WORK_NAME = "key_schedule_completion_work_name"
private const val KEY_PROCESS_UNTIL_PRIORITY = "key_process_until_priority"
private const val KEY_ERROR_MESSAGE = "key_schedule_completion_error_message"

@Reusable
internal class RegisterScheduleCompleteJobUseCaseImpl @Inject constructor(
    private val workManager: WorkManager
) : RegisterScheduleCompleteJobUseCase {

    override suspend fun invoke(
        debounceTimeout: Duration,
        processUntilPriority: NonNegativeLong?
    ): ScheduleJobResult {
        val workRequestBuilder = OneTimeWorkRequestBuilder<ScheduleCompletionWorker>().apply {
            if (debounceTimeout.isPositive()) {
                setInitialDelay(debounceTimeout.toJavaDuration())
            } else {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            setBackoffCriteria()
            setInputData(
                workDataOf(KEY_PROCESS_UNTIL_PRIORITY to processUntilPriority?.value)
            )
        }
        val workRequest = workRequestBuilder.build()
        workManager.enqueueUniqueWork(
            uniqueWorkName = KEY_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest
        )
        return workManager.awaitJobResult(
            workRequestId = workRequest.id,
            keyErrorMsg = KEY_ERROR_MESSAGE
        )
    }
}

@HiltWorker
internal class ScheduleCompletionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = runWithRetry(keyErrorMessage = KEY_ERROR_MESSAGE) {
        Timber.d("Start schedule completion work!")
        val currentBacklogs = scheduleCompletionBacklogRepository
            .getBacklogs(
                query = inputData.getLong(KEY_PROCESS_UNTIL_PRIORITY, /* defaultValue = */ -1L)
                    .tryToNonNegativeLongOrNull()
                    ?.let { GetScheduleCompletionBacklogQuery.ByScheduleIdsUpToPriority(priority = it) }
                    ?: GetScheduleCompletionBacklogQuery.All
            )
            .getOrThrow()
        if (currentBacklogs.isEmpty()) {
            Timber.d("Finished schedule completion work without any backlogs")
            return@runWithRetry
        }

        // TODO use alarmManager

        scheduleRepository
            .updateAll(
                query = UpdateAllScheduleQuery.Completes(
                    idToCompleteTable = buildMap {
                        currentBacklogs.sortedByDescending { it.priority.value }.forEach { backlog ->
                            putIfAbsent(backlog.scheduleId, backlog.targetCompleted)
                        }
                    }
                )
            )
            .getOrThrow()

        scheduleCompletionBacklogRepository
            .delete(backlogIds = currentBacklogs.toSet { it.id })
            .getOrThrow()

        Timber.d("Finished schedule completion work")
    }
}