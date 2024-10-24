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

package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.reduce.Accumulator
import com.nlab.statekit.reduce.Transition
import com.nlab.statekit.reduce.use

/**
 * @author Thalys
 */
internal sealed interface DslTransition {
    val scope: Any

    class NodeTransition<out R : Any, out A : Any, out S : R>(
        override val scope: Any,
        val next: (DslTransitionScope<@UnsafeVariance A, @UnsafeVariance S>) -> R
    ) : DslTransition

    class CompositeTransition(
        override val scope: Any,
        val transitions: List<DslTransition>,
    ) : DslTransition {
        init {
            check(transitions.size >= 2)
        }
    }

    class PredicateScopeTransition<out A : Any, out S : Any>(
        override val scope: Any,
        val isMatch: (UnsafeUpdateSource<A, S>) -> Boolean,
        val transition: DslTransition
    ) : DslTransition

    class TransformSourceScopeTransition<out A : Any, out S : Any, out T : Any, out U : Any>(
        override val scope: Any,
        val subScope: Any,
        val transformSource: (UnsafeUpdateSource<A, S>) -> UnsafeUpdateSource<T, U>?,
        val transition: DslTransition
    ) : DslTransition
}

@Suppress("UNCHECKED_CAST")
internal fun <A : Any, S : Any> Transition(
    dslTransition: DslTransition,
): Transition<A, S> = Transition.LifecycleNodeTransition { action, current, accumulatorPool ->
    val newValue = accumulatorPool.use { accTransition: Accumulator<DslTransition> ->
        accumulatorPool.use { accScope: Accumulator<Any> ->
            accumulatorPool.use { accDslTransitionScope: Accumulator<DslTransitionScope<Any, Any>> ->
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

    newValue?.let { it as S } ?: current
}

private tailrec fun transition(
    node: DslTransition?,
    scope: Any,
    dslTransitionScope: DslTransitionScope<Any, Any>,
    accTransition: Accumulator<DslTransition>,
    accScope: Accumulator<Any>,
    accDslTransitionScope: Accumulator<DslTransitionScope<Any, Any>>,
): Any? {
    node ?: return null

    if (scope !== node.scope) {
        return transition(
            node,
            accScope.removeLast(),
            accDslTransitionScope.removeLast(),
            accTransition,
            accScope,
            accDslTransitionScope
        )
    }

    return when (node) {
        is DslTransition.NodeTransition<Any, Any, Any> -> {
            val next = node.next(dslTransitionScope)
            if (next != dslTransitionScope.current) next
            else transition(
                accTransition.removeLastOrNull(),
                scope,
                dslTransitionScope,
                accTransition,
                accScope,
                accDslTransitionScope
            )
        }

        is DslTransition.CompositeTransition -> {
            val childTransitions = node.transitions
            transition(
                node = childTransitions.first(),
                scope,
                dslTransitionScope,
                accTransition = accTransition.apply {
                    for (index in childTransitions.size - 1 downTo 1) add(childTransitions[index])
                },
                accScope,
                accDslTransitionScope
            )
        }

        is DslTransition.PredicateScopeTransition<Any, Any> -> {
            transition(
                node = if (node.isMatch(dslTransitionScope)) node.transition else accTransition.removeLastOrNull(),
                scope,
                dslTransitionScope,
                accTransition,
                accScope,
                accDslTransitionScope
            )
        }

        is DslTransition.TransformSourceScopeTransition<Any, Any, Any, Any> -> {
            val newSource = node.transformSource(dslTransitionScope)
            if (newSource == null) {
                transition(
                    node = accTransition.removeLastOrNull(),
                    scope,
                    dslTransitionScope,
                    accTransition,
                    accScope,
                    accDslTransitionScope
                )
            } else {
                transition(
                    node = node.transition,
                    scope = node.subScope,
                    dslTransitionScope = DslTransitionScope(newSource),
                    accTransition = accTransition,
                    accScope = accScope.apply { add(scope) },
                    accDslTransitionScope = accDslTransitionScope.apply { add(dslTransitionScope) }
                )
            }
        }
    }
}