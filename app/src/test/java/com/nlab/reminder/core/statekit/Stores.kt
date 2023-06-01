/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.statekit

import com.nlab.statekit.Store
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * @author Doohyun
 */
suspend fun Store<*, *>.checkWhileStateUsed(
    vararg flows: MutableStateFlow<*>,
    timeout: Long = 2_000L
) {
    val flowsToList = flows.toList()
    withTimeout(timeout) {
        val collectJob = launch { state.collect() }
        // wait until all flows subscribed
        flowsToList.waitWithsubscriptionCounts { subscriptionCounts -> subscriptionCounts.all { it > 0 } }
        // cancel collect
        collectJob.cancelAndJoin()
        // wait until all flows unsubscribed
        flowsToList.waitWithsubscriptionCounts { subscriptionCounts -> subscriptionCounts.all { it == 0 } }
    }
}

private suspend fun List<MutableStateFlow<*>>.waitWithsubscriptionCounts(
    transform: (Array<Int>) -> Boolean
) {
    combine(map { it.subscriptionCount }, transform)
        .distinctUntilChanged()
        .filter { it }
        .take(1)
        .first()
}