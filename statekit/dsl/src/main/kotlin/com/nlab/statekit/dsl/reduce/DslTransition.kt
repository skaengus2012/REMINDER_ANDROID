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

package com.nlab.statekit.dsl.reduce

import com.nlab.statekit.reduce.NodeStack
import com.nlab.statekit.reduce.Transition
import com.nlab.statekit.reduce.use

/**
 * @author Thalys
 */
internal sealed interface DslTransition {
    val scope: Any

    class Node<out R : Any, out A : Any, out S : R>(
        override val scope: Any,
        val next: (DslTransitionScope<@UnsafeVariance A, @UnsafeVariance S>) -> R
    ) : DslTransition

    class Composite(
        override val scope: Any,
        val head: DslTransition,
        val tails: List<DslTransition>,
    ) : DslTransition

    class PredicateScope<out A : Any, out S : Any>(
        override val scope: Any,
        val isMatch: (UpdateSource<@UnsafeVariance A, @UnsafeVariance S>) -> Boolean,
        val transition: DslTransition
    ) : DslTransition

    class TransformSourceScope<out A : Any, out S : Any, out T : Any, out U : Any>(
        override val scope: Any,
        val subScope: Any,
        val transformSource: (UpdateSource<@UnsafeVariance A, @UnsafeVariance S>) -> UpdateSource<@UnsafeVariance T, @UnsafeVariance U>?,
        val transition: DslTransition
    ) : DslTransition
}

internal fun <A : Any, S : Any> transitionOf(
    dslTransition: DslTransition,
): Transition<A, S> = Transition.LifecycleNode { action, current, context ->
    val nodeStackPool = context.nodeStackPool
    val newValue = nodeStackPool.use { accTransition: NodeStack<DslTransition> ->
        nodeStackPool.use { accScope: NodeStack<Any> ->
            nodeStackPool.use { accDslTransitionScope: NodeStack<DslTransitionScope<Any, Any>> ->
                transition(
                    node = dslTransition,
                    scope = dslTransition.scope,
                    dslTransitionScope = DslTransitionScope(UpdateSource(action, current)),
                    accTransition = accTransition,
                    accScope = accScope,
                    accDslTransitionScope = accDslTransitionScope
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    newValue?.let { it as S } ?: current
}

private tailrec fun transition(
    node: DslTransition?,
    scope: Any,
    dslTransitionScope: DslTransitionScope<Any, Any>,
    accTransition: NodeStack<DslTransition>,
    accScope: NodeStack<Any>,
    accDslTransitionScope: NodeStack<DslTransitionScope<Any, Any>>,
): Any? {
    node ?: return null

    if (scope !== node.scope) {
        return transition(
            node = node,
            scope = accScope.removeLast(),
            dslTransitionScope = accDslTransitionScope.removeLast(),
            accTransition = accTransition,
            accScope = accScope,
            accDslTransitionScope = accDslTransitionScope
        )
    }

    return when (node) {
        is DslTransition.Node<Any, Any, Any> -> {
            val next = node.next(dslTransitionScope)
            if (next != dslTransitionScope.current) next
            else transition(
                node = accTransition.removeLastOrNull(),
                scope = scope,
                dslTransitionScope = dslTransitionScope,
                accTransition = accTransition,
                accScope = accScope,
                accDslTransitionScope = accDslTransitionScope
            )
        }

        is DslTransition.Composite -> {
            transition(
                node = node.head,
                scope = scope,
                dslTransitionScope = dslTransitionScope,
                accTransition = accTransition.apply { addAllReversed(node.tails) },
                accScope = accScope,
                accDslTransitionScope = accDslTransitionScope
            )
        }

        is DslTransition.PredicateScope<Any, Any> -> {
            val nextNode = if (node.isMatch(dslTransitionScope)) node.transition else accTransition.removeLastOrNull()
            transition(
                node = nextNode,
                scope = scope,
                dslTransitionScope = dslTransitionScope,
                accTransition = accTransition,
                accScope = accScope,
                accDslTransitionScope = accDslTransitionScope
            )
        }

        is DslTransition.TransformSourceScope<Any, Any, Any, Any> -> {
            val newSource = node.transformSource(dslTransitionScope)
            if (newSource == null) {
                transition(
                    node = accTransition.removeLastOrNull(),
                    scope = scope,
                    dslTransitionScope = dslTransitionScope,
                    accTransition = accTransition,
                    accScope = accScope,
                    accDslTransitionScope = accDslTransitionScope
                )
            } else {
                accScope.add(scope)
                accDslTransitionScope.add(dslTransitionScope)
                transition(
                    node = node.transition,
                    scope = node.subScope,
                    dslTransitionScope = DslTransitionScope(updateSource = newSource),
                    accTransition = accTransition,
                    accScope = accScope,
                    accDslTransitionScope = accDslTransitionScope
                )
            }
        }
    }
}