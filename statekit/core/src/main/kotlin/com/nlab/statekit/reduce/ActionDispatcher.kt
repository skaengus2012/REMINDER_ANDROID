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

package com.nlab.statekit.reduce

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * @author Doohyun
 */
interface ActionDispatcher<in A : Any> {
    suspend fun dispatch(action: A)
}

internal class DefaultActionDispatcher<A : Any, S : Any>(
    private val reduce: Reduce<A, S>,
    private val baseState: MutableStateFlow<S>,
    private val accPool: AccumulatorPool
) : ActionDispatcher<A> {
    override suspend fun dispatch(action: A) {
        val transition = reduce.transition
        val current = baseState.getAndTransitionTo(action, transition, accPool)
        reduce.effect?.let { effect ->
            val block = { coroutineScope: CoroutineScope ->
                effect.launch(
                    action,
                    current,
                    actionDispatcher = ChildActionDispatcher(
                        baseState,
                        transition,
                        effect,
                        coroutineScope,
                        accPool
                    ),
                    coroutineScope = coroutineScope,
                    accPool = accPool,
                )
            }
            coroutineScope(block)
        } ?: Unit
    }
}

private class ChildActionDispatcher<A : Any, S : Any>(
    private val state: MutableStateFlow<S>,
    private val transition: Transition<A, S>?,
    private val effect: Effect<A, S>,
    private val coroutineScope: CoroutineScope,
    private val accPool: AccumulatorPool,
) : ActionDispatcher<A> {
    override suspend fun dispatch(action: A) {
        val current = state.getAndTransitionTo(action, transition, accPool)
        effect.launch(
            action,
            current,
            actionDispatcher = this,
            coroutineScope = coroutineScope,
            accPool = accPool
        )
    }
}

private fun <A : Any, S : Any> MutableStateFlow<S>.getAndTransitionTo(
    action: A,
    transition: Transition<A, S>?,
    accumulatorPool: AccumulatorPool,
): S = when (transition) {
    null -> value
    else -> getAndUpdate { old -> transition.transitionTo(action, old, accumulatorPool) }
}