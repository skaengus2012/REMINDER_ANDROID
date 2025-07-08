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

package com.nlab.reminder.core.network.datasource

import android.content.Context
import com.google.android.gms.time.TrustedTime
import com.google.android.gms.time.TrustedTimeClient
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Instant

/**
 * @author Thalys
 */
fun interface TrustedTimeDataSource {
    suspend fun getCurrentTime(): Result<Instant>
}

class TrustedTimeDataSourceImpl(
    context: Context,
    coroutineScope: CoroutineScope
) : TrustedTimeDataSource {
    private val trustedTimeClientDeferred = coroutineScope.async {
        suspendCancellableCoroutine<TrustedTimeClient> { continuation ->
            TrustedTime.createClient(context).addOnCompleteListener { task ->
                if (task.isSuccessful) continuation.resume(task.result)
                else continuation.resumeWithException(task.exception ?: IllegalStateException())
            }
        }
    }

    override suspend fun getCurrentTime(): Result<Instant> = catching {
        trustedTimeClientDeferred
            .await()
            .computeCurrentUnixEpochMillis()
            ?.let { Instant.fromEpochMilliseconds(it) }
            ?: error("Cannot get current trusted time.")
    }
}