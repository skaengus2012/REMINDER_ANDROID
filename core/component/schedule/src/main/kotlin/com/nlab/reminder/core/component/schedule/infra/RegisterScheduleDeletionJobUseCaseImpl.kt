/*
 * Copyright (C) 2026 The N's lab Open Source Project
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
import com.nlab.reminder.core.component.schedule.RegisterScheduleDeletionJobUseCase
import com.nlab.reminder.core.component.schedule.ScheduleJobResult
import com.nlab.reminder.core.data.repository.DeleteScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleDeletionBacklogRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.getOrThrow
import dagger.Reusable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Doohyun
 */
private const val KEY_WORK_NAME = "key_schedule_deletion_work_name"
private const val KEY_ERROR_MESSAGE = "key_schedule_deletion_error_message"

@Reusable
internal class RegisterScheduleDeletionJobUseCaseImpl @Inject constructor(
    private val workManager: WorkManager
) : RegisterScheduleDeletionJobUseCase {

    override suspend fun invoke(): ScheduleJobResult {
        val workRequest = OneTimeWorkRequestBuilder<ScheduleDeletionWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria()
            .build()
        // Use REPLACE to cancel any active deletion workers and queue the new one.
        // Since the worker processes all backlogs at once, cancelling prior jobs
        // and starting a new one is safe and avoids redundant work queue accumulation.
        workManager.enqueueUniqueWork(
            KEY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        return workManager.awaitJobResult(
            workRequestId = workRequest.id,
            keyErrorMsg = KEY_ERROR_MESSAGE
        )
    }
}

@HiltWorker
internal class ScheduleDeletionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleDeletionBacklogRepository: ScheduleDeletionBacklogRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = runWithRetry(keyErrorMessage = KEY_ERROR_MESSAGE) {
        Timber.d("Start schedule deletion work!")

        val currentBacklogs = scheduleDeletionBacklogRepository.getBacklogs().getOrThrow()
        if (currentBacklogs.isEmpty()) {
            Timber.d("Finished schedule deletion work without any backlogs")
            return@runWithRetry
        }

        // TODO clear AlarmManager metadata

        scheduleRepository
            .delete(DeleteScheduleQuery.ByIds(currentBacklogs.toSet { it.scheduleId }))
            .getOrThrow()

        Timber.d("Finished schedule deletion work")
    }
}
