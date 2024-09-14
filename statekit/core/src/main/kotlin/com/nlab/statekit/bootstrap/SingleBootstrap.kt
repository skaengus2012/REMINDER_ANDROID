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

import com.nlab.statekit.infra.awaitOnceUntilSubscribed
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
internal class SingleBootstrap<out A : Any>(
    private val action: A,
    private val isPendingBootUntilSubscribed: Boolean
) : Bootstrap<A>() {
    override fun onFetched(
        coroutineScope: CoroutineScope,
        actionDispatcher: ActionDispatcher<in A>,
        stateSubscriptionCount: StateFlow<Int>
    ): List<Job> {
        val initJob = if (isPendingBootUntilSubscribed) {
            awaitOnceUntilSubscribed(
                coroutineScope,
                stateSubscriptionCount,
                onSubscribed = { actionDispatcher.dispatch(action) }
            )
        } else {
            coroutineScope.launch { actionDispatcher.dispatch(action) }
        }
        return listOf(initJob)
    }
}