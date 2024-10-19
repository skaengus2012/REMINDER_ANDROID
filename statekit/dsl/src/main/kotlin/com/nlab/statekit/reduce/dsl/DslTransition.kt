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

import com.nlab.statekit.reduce.Transition

/**
 * @author Thalys
 */
internal sealed interface DslTransition<R : Any, A : Any, S : R> {
    fun interface NodeTransition<R : Any, A : Any, S : R> : DslTransition<R, A, S> {
        fun next(scope: DslTransitionScope<A, S>): R
    }

    class PredicateScopeTransition<R : Any, A : Any, S : R>(
        val predicate: (UpdateSource<A, S>) -> Boolean,
        val transition: DslTransition<R, A, S>
    ) : DslTransition<R, A, S>

    class TransformSourceScopeTransition<R : Any, A : Any, S : R, T : Any, U : R>(
        val transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        val transition: DslTransition<R, T, U>
    ) : DslTransition<R, A, S>

    /**
     * @param transitions size must be 2 or more.
     */
    class CompositeTransition<R : Any, A : Any, S : R>(
        val transitions: List<DslTransition<R, A, S>>
    ) : DslTransition<R, A, S> {
        init {
            check(transitions.size >= 2)
        }
    }
}

internal fun <A : Any, S : Any> Transition(
    dslTransition: DslTransition<S, A, S>,
): Transition<A, S> = Transition.NodeTransition { action, current ->
    val next = transition(
        DslTransitionScope(UpdateSource(action, current)),
        dslTransition,
        ArrayDeque()
    )
    next ?: current
}

private tailrec fun <R : Any, A : Any, S : R> transition(
    scope: DslTransitionScope<A, S>,
    node: DslTransition<R, A, S>?,
    acc: MutableList<DslTransition<R, A, S>>
): R? = if (node == null) null else when (node) {
    is DslTransition.NodeTransition -> {
        val next = node.next(scope)
        if (next != scope.current) next
        else transition(scope, acc.removeLastOrNull(), acc)
    }

    is DslTransition.PredicateScopeTransition -> {
        transition(
            scope,
            node = when (node.predicate(scope)) {
                true -> node.transition
                false -> acc.removeLastOrNull()
            },
            acc
        )
    }

    is DslTransition.TransformSourceScopeTransition<R, A, S, *, *> -> {
        val newSource = node.transformSource(scope)
        if (newSource == null) {
            transition(scope, node = acc.removeLastOrNull(), acc)
        } else {

        }
    }

    is DslTransition.CompositeTransition -> {
        val childTransitions = node.transitions
        transition(
            scope,
            acc.first(),
            acc.apply {
                for (index in childTransitions.size - 1 downTo 1) {
                    add(childTransitions[index])
                }
            }
        )
    }
}

private fun <R : Any, A : Any, S : R, T : Any, U : R> subTransition(
    node: DslTransition.TransformSourceScopeTransition<R, A, S, T, U>,
    scope: DslTransitionScope<A, S>
): R? {
    val newSource = node.transformSource(scope)
    return if (newSource == null) null
    else transition(DslTransitionScope(newSource), node.transition, ArrayDeque())
}