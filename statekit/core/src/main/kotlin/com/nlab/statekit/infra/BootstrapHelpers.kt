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

package com.nlab.statekit.infra

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 *
 * Wait until the first subscriber is created, then run it once.
 */
// NOTE Because Jacoco recognizes the inline function safeCollector, it is extracted as an infra function.
internal fun awaitOnceUntilSubscribed(
    coroutineScope: CoroutineScope,
    subscriptionCount: StateFlow<Int>,
    onSubscribed: suspend () -> Unit
): Job = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
    subscriptionCount
        .filter { it > 0 }
        .take(1)
        .collect { onSubscribed() }
}

internal fun <A> collectUntilSubscribed(
    coroutineScope: CoroutineScope,
    subscriptionCount: StateFlow<Int>,
    actionStream: Flow<A>,
    collect: suspend (A) -> Unit
): Job = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
    subscriptionCount
        .map { it > 0 }
        .distinctUntilChanged()
        .collectLatest { isActive -> if (isActive) actionStream.collect(collect) }
}