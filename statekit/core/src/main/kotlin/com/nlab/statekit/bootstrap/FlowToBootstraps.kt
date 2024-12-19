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

package com.nlab.statekit.bootstrap

import com.nlab.statekit.internal.infra.collectWhileSubscribed
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
sealed interface DeliveryStarted {
    data object Eagerly : DeliveryStarted
    data object Lazily : DeliveryStarted
    class WhileSubscribed(val stopTimeoutMillis: Long = 0) : DeliveryStarted {
        init {
            require(stopTimeoutMillis >= 0)
        }
    }
}

internal class FlowBootstrap<out A : Any>(
    private val actionStream: Flow<A>,
    private val started: DeliveryStarted,
) : Bootstrap<A>() {
    override fun onFetched(
        coroutineScope: CoroutineScope,
        actionDispatcher: ActionDispatcher<A>,
        stateSubscriptionCount: StateFlow<Int>
    ): Set<Job> {
        val flowCollector = actionDispatcher::dispatch
        val job = when (started) {
            is DeliveryStarted.Eagerly -> coroutineScope.launch {
                actionStream.collect(flowCollector)
            }

            is DeliveryStarted.Lazily -> coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                suspend fun Flow<Int>.awaitUntilPositive(): Int {
                    val positivePredicate = { num: Int -> num > 0 }
                    return first(positivePredicate)
                }

                stateSubscriptionCount.awaitUntilPositive()
                actionStream.collect(flowCollector)
            }

            is DeliveryStarted.WhileSubscribed -> collectWhileSubscribed(
                coroutineScope,
                stateSubscriptionCount,
                started.stopTimeoutMillis,
                collector = { actionStream.collect(flowCollector) }
            )
        }
        return setOf(job)
    }
}