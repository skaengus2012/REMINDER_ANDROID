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

package com.nlab.reminder.apps.startup.init

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.startup.Initializer
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nlab.reminder.apps.startup.dependenciesOf
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.UpdateAllScheduleQuery
import com.nlab.reminder.core.kotlin.getOrElse
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.onFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author Doohyun
 */
private const val KEY_REINDEX_VISIBLE_PRIORITY_WORK_NAME = "key_reindex_visible_priority_work_name"

class DataCleanupInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .setRequiresCharging(true)
            .build()
        registerWork<ReindexVisiblePriorityWorker>(
            context = context,
            uniqueWorkName = KEY_REINDEX_VISIBLE_PRIORITY_WORK_NAME,
            constraints = constraints
        )
    }

    private inline fun <reified T : ListenableWorker> registerWork(
        context: Context,
        uniqueWorkName: String,
        constraints: Constraints
    ) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request = PeriodicWorkRequestBuilder<T>(repeatInterval = 3, repeatIntervalTimeUnit = TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
        )
    }

    override fun dependencies() = dependenciesOf(WorkManagerInitializer::class)
}

@HiltWorker
internal class ReindexVisiblePriorityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepository: ScheduleRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            return scheduleRepository.updateAll(UpdateAllScheduleQuery.ReindexVisiblePriorities)
                .map { Result.success() }
                .onFailure { Timber.e(it, "Reindexing failed") }
                .getOrElse { Result.retry() }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error in Worker")
            return Result.failure()
        }
    }
}