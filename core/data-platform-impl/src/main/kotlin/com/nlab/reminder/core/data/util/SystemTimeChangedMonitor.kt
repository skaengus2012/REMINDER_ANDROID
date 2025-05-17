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

package com.nlab.reminder.core.data.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn

/**
 * @author Doohyun
 */
class SystemTimeChangedMonitor(
    context: Context,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) {
    val timeChangedEvent: SharedFlow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_TIME_CHANGED) {
                    trySend(Unit)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_CHANGED))

        awaitClose { context.unregisterReceiver(receiver) }
    }.flowOn(dispatcher).shareIn(coroutineScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000))
}