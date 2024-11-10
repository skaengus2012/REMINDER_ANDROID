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

import com.nlab.statekit.dsl.internal.Generated

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

    fun <R : Any, A : Any, S : Any> addEffectNode(block: suspend (DslEffectScope<R, A, S>) -> Unit) {
        effectBuilder.addNode(block)
    }

    // Test OK @see {com.nlab.statekit.dsl.reduce.ReduceBuilderDelegateTest}
    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/pull/1670
    @Generated
    private inline fun addScopeInternal(
        from: ReduceBuilderDelegate,
        addTransition: (DslTransition) -> Unit,
        addEffect: (DslEffect) -> Unit
    ) {
        from.buildTransition()?.let(addTransition)
        from.buildEffect()?.let(addEffect)
    }

    fun addScope(from: ReduceBuilderDelegate) {
        addScopeInternal(
            from,
            addTransition = transitionBuilder::addTransition,
            addEffect = effectBuilder::addEffect
        )
    }

    fun <A : Any, S : Any> addPredicateScope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        from: ReduceBuilderDelegate
    ) {
        addScopeInternal(
            from,
            addTransition = { transitionBuilder.addPredicateScope(isMatch, it) },
            addEffect = { effectBuilder.addPredicateScope(isMatch, it) }
        )
    }

    // Test OK @see {com.nlab.statekit.dsl.reduce.ReduceBuilderDelegateTest}
    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/pull/1670
    @Generated
    inline fun <RS : Any, A : Any, S : RS, T : Any, U : RS> addTransformSourceScope(
        noinline transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        crossinline from: (subScope: Any) -> ReduceBuilderDelegate,
    ) {
        val subScope = Any()
        addScopeInternal(
            from(subScope),
            addTransition = { transitionBuilder.addTransformSourceScope(subScope, transformSource, it) },
            addEffect = { effectBuilder.addTransformSourceScope(subScope, transformSource, it) }
        )
    }
}

internal fun ReduceBuilderDelegate(scope: Any) = ReduceBuilderDelegate(
    DslTransitionBuilder(scope),
    DslEffectBuilder(scope)
)