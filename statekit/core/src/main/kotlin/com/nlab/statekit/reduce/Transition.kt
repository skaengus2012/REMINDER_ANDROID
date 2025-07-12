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
        fun next(action: A, current: S, nodeStackPool: NodeStackPool): S
    }

    class Composite<A : Any, S : Any>(
        val head: Transition<A, S>,
        val tails: List<Transition<A, S>>
    ) : Transition<A, S> {
        companion object {
            operator fun <A : Any, S : Any> invoke(
                head: Transition<A, S>,
                vararg tails: Transition<A, S>
            ): Composite<A, S> = Composite(head, tails.toList())
        }
    }
}

fun <A : Any, S : Any> Transition<A, S>.transitionTo(
    action: A,
    current: S,
    nodeStackPool: NodeStackPool
): S = nodeStackPool.use { nodeStack ->
    transitionInternal(action, current, node = this, nodeStack, nodeStackPool) ?: current
}

private tailrec fun <A : Any, S : Any> transitionInternal(
    action: A,
    current: S,
    node: Transition<A, S>?,
    nodeStack: NodeStack<Transition<A, S>>,
    nodeStackPool: NodeStackPool,
): S? = if (node == null) null else when (node) {
    is Transition.Node -> {
        val next = node.next(action, current)
        if (next != current) next
        else transitionInternal(action, current, node = nodeStack.removeLastOrNull(), nodeStack, nodeStackPool)
    }

    is Transition.LifecycleNode -> {
        val next = node.next(action, current, nodeStackPool)
        if (next != current) next
        else transitionInternal(action, current, node = nodeStack.removeLastOrNull(), nodeStack, nodeStackPool)
    }

    is Transition.Composite -> {
        transitionInternal(
            action,
            current,
            node.head,
            nodeStack.addAllReversed(node.tails),
            nodeStackPool
        )
    }
}