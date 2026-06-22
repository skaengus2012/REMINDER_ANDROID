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

import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.nlab.reminder.core.component.schedule.ScheduleJobResult
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

internal fun <W : WorkRequest.Builder<W, *>> WorkRequest.Builder<W, *>.setBackoffCriteria(): W {
    return setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        backoffDelay = 10,
        TimeUnit.SECONDS
    )
}

internal suspend fun WorkManager.awaitJobResult(
    workRequestId: UUID,
    keyErrorMsg: String
): ScheduleJobResult = getWorkInfoByIdFlow(workRequestId).filterNotNull()
    .mapNotNull { workInfo ->
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> ScheduleJobResult.Success
            WorkInfo.State.CANCELLED -> ScheduleJobResult.Cancelled
            WorkInfo.State.FAILED -> {
                val errorMsg = workInfo.outputData.getString(keyErrorMsg) ?: "Unknown failure"
                ScheduleJobResult.Failure(IllegalStateException(errorMsg))
            }
            WorkInfo.State.ENQUEUED -> {
                // When the job is first enqueued, runAttemptCount is 0.
                // We return null to keep waiting for the job to start and finish.
                // If runAttemptCount > 0, it means the job has failed at least once
                // and entered the exponential backoff retry phase.
                if (workInfo.runAttemptCount > 0) {
                    ScheduleJobResult.Retrying
                } else {
                    null
                }
            }
            WorkInfo.State.RUNNING,
            WorkInfo.State.BLOCKED -> null
        }
    }
    .first()

/**
 * Common extension function to run worker task with exponential backoff retry.
 * Handles transient/permanent errors and enforces a maximum retry limit.
 *
 * @author Doohyun
 */
internal inline fun CoroutineWorker.runWithRetry(
    keyErrorMessage: String,
    maxAttempts: Int = 3,
    block: () -> Unit,
): Result {
    return try {
        block()
        Result.success()
    } catch (t: Throwable) {
        Timber.e(t, "Work failed on attempt $runAttemptCount")
        if (runAttemptCount < maxAttempts && isTransientThrowable(t)) {
            Timber.w("Transient error occurred. Retrying... ($runAttemptCount/$maxAttempts)")
            Result.retry()
        } else {
            Timber.e("Permanent failure or max attempts reached.")

            val errorMessage = t.message ?: "Unknown error"
            Result.failure(workDataOf(keyErrorMessage to errorMessage))
        }
    }
}

private fun isTransientThrowable(t: Throwable): Boolean {
    return t is java.io.IOException || t is android.database.sqlite.SQLiteException
}