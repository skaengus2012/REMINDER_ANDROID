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
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nlab.reminder.core.component.schedule.RegisterScheduleCompleteJobUseCase
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.kotlin.getOrElse
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.onFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * @author Thalys
 */
private const val KEY_SCHEDULE_COMPLETION_WORK_NAME = "key_schedule_completion_work_name"

internal class RegisterScheduleCompleteJobUseCaseImpl(
    private val context: Context
) : RegisterScheduleCompleteJobUseCase {
    override operator fun invoke(delayTime: Duration) {
        val workRequestBuilder = OneTimeWorkRequestBuilder<ScheduleCompletionWork>().apply {
            if (delayTime.isPositive()) {
                setInitialDelay(delayTime.toJavaDuration())
            }
        }
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName = KEY_SCHEDULE_COMPLETION_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequestBuilder.build()
        )
    }
}

@HiltWorker
internal class ScheduleCompletionWork @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val currentBacklogs = scheduleCompletionBacklogRepository
            .getBacklogs()
            .onFailure { Timber.e(it) }
            .getOrElse { return Result.failure() }

        if (currentBacklogs.isEmpty()) return Result.success()

        // TODO use alarmManager

        return scheduleRepository
            .updateAll(
                query = UpdateAllScheduleQuery.Completes(
                    idToCompleteTable = buildMap {
                        currentBacklogs.sortedByDescending { it.priority.value }.forEach { backlog ->
                            putIfAbsent(backlog.scheduleId, backlog.targetCompleted)
                        }
                    }
                )
            )
            .map { Result.success() }
            .onFailure { Timber.e(it) }
            .getOrElse { Result.failure() }
    }
}