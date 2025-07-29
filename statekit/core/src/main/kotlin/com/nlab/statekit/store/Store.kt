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

package com.nlab.statekit.store

import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.reduce.NodeStackPool
import com.nlab.statekit.dispatch.ActionDispatcher
import com.nlab.statekit.reduce.Reduce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
interface Store<A : Any, S : Any> {
    val state: StateFlow<S>
    fun dispatch(action: A): Job
}

internal class StoreImpl<A : Any, S : Any>(
    override val state: StateFlow<S>,
    private val coroutineScope: CoroutineScope,
    private val actionDispatcher: ActionDispatcher<A>,
    @Suppress("unused") private val initJobs: Collection<Job>  // for strong reference
) : Store<A, S> {
    override fun dispatch(action: A): Job = coroutineScope.launch { actionDispatcher.dispatch(action) }
}

internal class StoreFactory(private val nodeStackPool: NodeStackPool) {
    fun <A : Any, S : Any> create(
        coroutineScope: CoroutineScope,
        initState: S,
        reduce: Reduce<A, S>,
        bootstrap: Bootstrap<A>
    ): Store<A, S> {
        val baseState = MutableStateFlow(initState)
        val actionDispatcher = RootActionDispatcher(reduce, baseState, nodeStackPool)
        return StoreImpl(
            state = baseState.asStateFlow(),
            coroutineScope = coroutineScope,
            actionDispatcher = actionDispatcher,
            initJobs = bootstrap.fetch(
                coroutineScope,
                actionDispatcher,
                stateSubscriptionCount = baseState.subscriptionCount
            )
        )
    }
}