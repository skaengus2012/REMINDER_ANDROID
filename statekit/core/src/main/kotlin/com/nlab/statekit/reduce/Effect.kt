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

import com.nlab.statekit.dispatch.ActionDispatcher
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
sealed interface Effect<A : Any, S : Any> {
    fun interface Node<A : Any, S : Any> : Effect<A, S> {
        fun invoke(action: A, current: S)
    }

    fun interface SuspendNode<A : Any, S : Any> : Effect<A, S> {
        suspend fun invoke(action: A, current: S, actionDispatcher: ActionDispatcher<A>)
    }

    fun interface LifecycleNode<A : Any, S : Any> : Effect<A, S> {
        fun invoke(
            action: A,
            current: S,
            context: EffectContext,
            actionDispatcher: ActionDispatcher<A>,
        )
    }

    class Composite<A : Any, S : Any>(
        val head: Effect<A, S>,
        val tails: List<Effect<A, S>>
    ) : Effect<A, S>
}

fun <A : Any, S : Any> Effect<A, S>.launch(
    action: A,
    current: S,
    context: EffectContext,
    actionDispatcher: ActionDispatcher<A>
) {
    context.nodeStackPool.use { nodeStack ->
        launchInternal(node = this, action, current, context, actionDispatcher, nodeStack)
    }
}

private tailrec fun <A : Any, S : Any> launchInternal(
    node: Effect<A, S>?,
    action: A,
    current: S,
    context: EffectContext,
    actionDispatcher: ActionDispatcher<A>,
    nodeStack: NodeStack<Effect<A, S>>,
) {
    if (node == null) return
    val nextNode = when (node) {
        is Effect.Node -> {
            try {
                node.invoke(action, current)
            } catch (t: Throwable) {
                context.throwableCollector.collect(t)
            }
            nodeStack.removeLastOrNull()
        }

        is Effect.SuspendNode -> {
            context.coroutineScope.launch {
                try {
                    node.invoke(action, current, actionDispatcher)
                } catch (t: Throwable) {
                    context.throwableCollector.collect(t)
                }
            }
            nodeStack.removeLastOrNull()
        }

        is Effect.LifecycleNode -> {
            node.invoke(action, current, context, actionDispatcher)
            nodeStack.removeLastOrNull()
        }

        is Effect.Composite -> {
            nodeStack.addAllReversed(node.tails)
            node.head
        }
    }
    launchInternal(nextNode, action, current, context, actionDispatcher, nodeStack)
}