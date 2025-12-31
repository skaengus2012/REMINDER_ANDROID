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

package com.nlab.reminder.core.component.schedulelist.content.ui

import android.os.SystemClock
import androidx.lifecycle.LifecycleCoroutineScope
import com.nlab.reminder.core.kotlinx.coroutines.flow.channelFlow
import com.nlab.reminder.core.kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal class SyncableState<T>(
    lifecycleScope: LifecycleCoroutineScope,
    initialValue: T,
    private val syncDuration: Long,
) {
    private val syncFlow =
        MutableStateFlow<StampedValue<T>>(StampedValue(initialValue, EMPTY_COMMIT_TIME))
    private val localFlow =
        MutableStateFlow<StampedValue<T>>(StampedValue(initialValue, EMPTY_COMMIT_TIME))

    private var latestLocalCommitTime = 0L
    private var latestLocalAppliedTime = 0L
    private var latestSyncAppliedTime = 0L

    val state = channelFlow {
        val combinedState = MutableIdentityStateFlow<T>()
        launch {
            combinedState.unwrap().collect { send(it) }
        }
        launch {
            combine(localFlow, syncFlow, ::Pair).collectLatest { (local, sync) ->
                var alreadyDelayed = false
                if (local.commitTime > latestLocalAppliedTime) {
                    latestLocalAppliedTime = local.commitTime
                    combinedState.update(local.data)
                    delay(syncDuration)
                    alreadyDelayed = true
                }

                if (sync.commitTime > latestSyncAppliedTime) {
                    if (latestSyncAppliedTime != EMPTY_COMMIT_TIME && alreadyDelayed.not()) {
                        delay(syncDuration)
                    }
                    latestSyncAppliedTime = sync.commitTime
                    combinedState.update(sync.data)
                }
            }
        }
    }.stateIn(scope = lifecycleScope, started = SharingStarted.Eagerly, initialValue = initialValue)

    fun set(value: T) {
        latestLocalCommitTime = SystemClock.elapsedRealtime()
        localFlow.value = StampedValue(value, commitTime = latestLocalCommitTime)
    }

    fun sync(value: T) {
        syncFlow.value = StampedValue(value, commitTime = SystemClock.elapsedRealtime())
    }

    fun snapshot(): T {
        if (latestLocalCommitTime > latestLocalAppliedTime) return localFlow.value.data
        return state.value
    }

    companion object {
        private const val EMPTY_COMMIT_TIME = 0L
    }

    private class StampedValue<T>(val data: T, val commitTime: Long)
}