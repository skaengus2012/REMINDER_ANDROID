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

package com.nlab.statekit.store

import com.nlab.statekit.dispatch.ActionDispatcher
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.EffectContext
import com.nlab.statekit.reduce.NodeStackPool
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.reduce.ThrowableCollector
import com.nlab.statekit.reduce.Transition
import com.nlab.statekit.reduce.TransitionContext
import com.nlab.statekit.reduce.launch
import com.nlab.statekit.reduce.transitionTo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * @author Doohyun
 */
internal class RootActionDispatcher<A : Any, S : Any>(
    reduce: Reduce<A, S>,
    private val baseState: MutableStateFlow<S>,
    private val nodeStackPool: NodeStackPool
) : ActionDispatcher<A> {
    private val transition = reduce.transition
    private val effect = reduce.effect

    override suspend fun dispatch(action: A) {
        val transitionContext = TransitionContext(nodeStackPool)
        val current = baseState.getAndTransition(action, transition, transitionContext)
        if (effect == null) return

        val throwableCollector = ThrowableCollector()

        val block = { coroutineScope: CoroutineScope ->
            val effectContext = EffectContext(
                coroutineScope = coroutineScope,
                nodeStackPool = nodeStackPool,
                throwableCollector = throwableCollector
            )
            val nextActionDispatcher = ChildActionDispatcher(
                baseState,
                transition,
                effect,
                transitionContext,
                effectContext
            )
            effect.launch(action, current, effectContext, nextActionDispatcher)
        }
        coroutineScope(block)

        val throwableSnapshots = throwableCollector.snapshot()
        if (throwableSnapshots.isNotEmpty()) {
            val currentCoroutineContext = currentCoroutineContext()
            val exceptionHandler = currentCoroutineContext[CoroutineExceptionHandler]
            if (exceptionHandler == null) throw throwableSnapshots.first()
            else throwableSnapshots.forEach { t -> exceptionHandler.handleException(currentCoroutineContext, t) }
        }
    }
}

private class ChildActionDispatcher<A : Any, S : Any>(
    private val state: MutableStateFlow<S>,
    private val transition: Transition<A, S>?,
    private val effect: Effect<A, S>,
    private val transitionContext: TransitionContext,
    private val effectContext: EffectContext,
) : ActionDispatcher<A> {
    override suspend fun dispatch(action: A) {
        val current = state.getAndTransition(action, transition, transitionContext)
        effect.launch(
            action,
            current,
            effectContext,
            actionDispatcher = this,
        )
    }
}

private fun <A : Any, S : Any> MutableStateFlow<S>.getAndTransition(
    action: A,
    transition: Transition<A, S>?,
    context: TransitionContext
): S = if (transition == null) value else getAndUpdate { current -> transition.transitionTo(action, current, context) }