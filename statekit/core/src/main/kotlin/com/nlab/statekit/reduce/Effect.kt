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
import kotlinx.coroutines.launch


/**
 * @author Doohyun
 */
sealed interface Effect<A : Any, S : Any> {
    fun interface NodeEffect<A : Any, S : Any> : Effect<A, S> {
        suspend fun invoke(action: A, current: S, actionDispatcher: ActionDispatcher<A>)
    }

    fun interface LifecycleNodeEffect<A : Any, S : Any> : Effect<A, S> {
        fun invoke(
            action: A,
            current: S,
            actionDispatcher: ActionDispatcher<A>,
            coroutineScope: CoroutineScope,
            accumulatorPool: AccumulatorPool
        )
    }

    class CompositeEffect<A : Any, S : Any>(
        val head: Effect<A, S>,
        val tails: List<Effect<A, S>>
    ) : Effect<A, S> {
        companion object {
            operator fun <A : Any, S : Any> invoke(
                head: Effect<A, S>,
                vararg tails: Effect<A, S>
            ): CompositeEffect<A, S> = CompositeEffect(head, tails.toList())
        }
    }
}

fun <A : Any, S : Any> Effect<A, S>.launch(
    action: A,
    current: S,
    actionDispatcher: ActionDispatcher<A>,
    accumulatorPool: AccumulatorPool,
    coroutineScope: CoroutineScope,
) {
    tailrec fun <A : Any, S : Any> launchInternal(
        action: A,
        current: S,
        node: Effect<A, S>?,
        actionDispatcher: ActionDispatcher<A>,
        acc: Accumulator<Effect<A, S>>,
        accPool: AccumulatorPool,
        coroutineScope: CoroutineScope,
    ) {
        if (node == null) return

        val nextNode = when (node) {
            is Effect.NodeEffect -> {
                coroutineScope.launch { node.invoke(action, current, actionDispatcher) }
                acc.removeLastOrNull()
            }

            is Effect.LifecycleNodeEffect -> {
                node.invoke(action, current, actionDispatcher, coroutineScope, accPool)
                acc.removeLastOrNull()
            }

            is Effect.CompositeEffect -> {
                acc.apply { addAll(node.tails) }
                node.head
            }
        }
        launchInternal(
            action,
            current,
            nextNode,
            actionDispatcher,
            acc,
            accPool,
            coroutineScope,
        )
    }

    accumulatorPool.use { acc ->
        launchInternal(
            action,
            current,
            node = this,
            actionDispatcher,
            acc,
            accumulatorPool,
            coroutineScope,
        )
    }
}