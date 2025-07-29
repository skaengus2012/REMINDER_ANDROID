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

/**
 * @author Doohyun
 */
internal class ReduceBuilderDelegate(
    private val transitionBuilder: DslTransitionBuilder,
    private val effectBuilder: DslEffectBuilder
) {
    fun buildTransition(): DslTransition? = transitionBuilder.build()
    fun buildEffect(): DslEffect? = effectBuilder.build()

    fun <R : Any, A : Any, S : R> addTransitionNode(block: (DslTransitionScope<A, S>) -> R) {
        transitionBuilder.addNode(block)
    }

    fun <A : Any, S : Any> addEffectNode(block: (DslEffectScope<A, S>) -> Unit) {
        effectBuilder.addNode(block)
    }

    fun <R : Any, A : Any, S : Any> addSuspendEffectNode(block: suspend (DslSuspendEffectScope<R, A, S>) -> Unit) {
        effectBuilder.addSuspendNode(block)
    }

    private inline fun addScopeInternal(
        child: ReduceBuilderDelegate,
        addTransition: (DslTransition) -> Unit,
        addEffect: (DslEffect) -> Unit
    ) {
        child.buildTransition()?.let(addTransition)
        child.buildEffect()?.let(addEffect)
    }

    fun addScope(child: ReduceBuilderDelegate) {
        addScopeInternal(
            child,
            addTransition = transitionBuilder::addTransition,
            addEffect = effectBuilder::addEffect
        )
    }

    fun <A : Any, S : Any> addPredicateScope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        child: ReduceBuilderDelegate
    ) {
        addScopeInternal(
            child,
            addTransition = { transitionBuilder.addPredicateScope(isMatch, transition = it) },
            addEffect = { effectBuilder.addPredicateScope(isMatch, effect = it) }
        )
    }

    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    // This method is used internally, it is possible to fill it with coverage.
    inline fun <RS : Any, A : Any, S : RS, T : Any, U : RS> addTransformSourceScope(
        noinline transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        crossinline child: (subScope: Any) -> ReduceBuilderDelegate,
    ) {
        val subScope = Any()
        addScopeInternal(
            child = child(subScope),
            addTransition = { transitionBuilder.addTransformSourceScope(subScope, transformSource, transition = it) },
            addEffect = { effectBuilder.addTransformSourceScope(subScope, transformSource, effect = it) }
        )
    }
}

internal fun ReduceBuilderDelegate(scope: Any) = ReduceBuilderDelegate(
    transitionBuilder = DslTransitionBuilder(scope),
    effectBuilder = DslEffectBuilder(scope)
)