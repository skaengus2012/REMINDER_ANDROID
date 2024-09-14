/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.statekit.internal.infra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
// After executing collectLatest, verification of the call cannot be performed.
internal fun collectWhileSubscribed(
    coroutineScope: CoroutineScope,
    stateSubscriptionCount: StateFlow<Int>,
    stopTimeoutMillis: Long,
    collector: suspend () -> Unit
): Job = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
    stateSubscriptionCount
        .mapLatest { count ->
            if (count > 0) true
            else {
                delay(stopTimeoutMillis)
                false
            }
        }
        .dropWhile { it.not() }
        .distinctUntilChanged()
        .collectLatest { canCollectable -> if (canCollectable) collector.invoke() }
}