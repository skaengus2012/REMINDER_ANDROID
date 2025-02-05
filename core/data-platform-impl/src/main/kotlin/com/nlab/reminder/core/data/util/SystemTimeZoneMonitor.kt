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
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinTimeZone
import java.time.ZoneId

/**
 * @author Thalys
 */
class SystemTimeZoneMonitor(
    context: Context,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
) : TimeZoneMonitor {
    override val currentTimeZone: SharedFlow<TimeZone> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return
                val zoneIdFromIntent = if (VERSION.SDK_INT < VERSION_CODES.R) null
                else {
                    // Starting Android R we also get the new TimeZone.
                    intent.getStringExtra(Intent.EXTRA_TIMEZONE)?.let { timeZoneId ->
                        // We need to convert it from java.util.Timezone to java.time.ZoneId
                        val zoneId = ZoneId.of(timeZoneId, ZoneId.SHORT_IDS)
                        // Convert to kotlinx.datetime.TimeZone
                        zoneId.toKotlinTimeZone()
                    }
                }
                // If there isn't a zoneId in the intent, fallback to the systemDefault, which should also reflect the change
                trySend(zoneIdFromIntent ?: TimeZone.currentSystemDefault())
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))

        // Send the default time zone first.

        // Fix thalys
        // NIA is transmitted before and after registering Boardcast,
        // Since the thread is not blocked for the broadcast registration, it is sent only once after registering.
        // @see https://github.com/android/nowinandroid/blob/e308246131a44cd9a4caf700bee26e8351d06090/core/data/src/main/kotlin/com/google/samples/apps/nowinandroid/core/data/util/TimeZoneMonitor.kt#L95
        trySend(TimeZone.currentSystemDefault())

        awaitClose { context.unregisterReceiver(receiver) }
    }
        // We use to prevent multiple emissions of the same type, because we use trySend multiple times.
        .distinctUntilChanged()
        .conflate()
        .flowOn(dispatcher)
        // Sharing the callback to prevent multiple BroadcastReceivers being registered
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}