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

/**
 * @author Doohyun
 */
sealed interface Transition<A : Any, S : Any> {
    fun interface Node<A : Any, S : Any> : Transition<A, S> {
        fun next(action: A, current: S): S
    }

    fun interface LifecycleNode<A : Any, S : Any> : Transition<A, S> {
        fun next(action: A, current: S, context: TransitionContext): S
    }

    class Composite<A : Any, S : Any>(
        val head: Transition<A, S>,
        val tails: List<Transition<A, S>>
    ) : Transition<A, S>
}

fun <A : Any, S : Any> Transition<A, S>.transitionTo(
    action: A,
    current: S,
    context: TransitionContext
): S = context.nodeStackPool.use { nodeStack ->
    transitionInternal(node = this, action, current, context, nodeStack) ?: current
}

private tailrec fun <A : Any, S : Any> transitionInternal(
    node: Transition<A, S>?,
    action: A,
    current: S,
    context: TransitionContext,
    nodeStack: NodeStack<Transition<A, S>>
): S? {
    if (node == null) return null

    return when (node) {
        is Transition.Node -> {
            val next = node.next(action, current)
            if (next != current) next
            else transitionInternal(node = nodeStack.removeLastOrNull(), action, current, context, nodeStack)
        }

        is Transition.LifecycleNode -> {
            val next = node.next(action, current, context)
            if (next != current) next
            else transitionInternal(node = nodeStack.removeLastOrNull(), action, current, context, nodeStack)
        }

        is Transition.Composite -> {
            transitionInternal(
                node = node.head,
                action,
                current,
                context,
                nodeStack = nodeStack.apply { addAllReversed(node.tails) },
            )
        }
    }
}