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

import com.nlab.statekit.dsl.annotation.DslReduceMarker
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
@DslReduceMarker
class ScopeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS> internal constructor(
    private val scope: Any,
) {
    internal val delegate: ReduceBuilderDelegate = ReduceBuilderDelegate(scope)

    fun transition(block: DslTransitionScope<A, S>.() -> RS) {
        delegate.addTransitionNode(block)
    }

    fun effect(block: suspend DslEffectScope<RA, A, S>.()-> Unit) {
        delegate.addEffectNode(block)
    }

    @JvmName(name = "scopeWithPredicate")
    fun scope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        block: ScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addPredicateScope(
            isMatch,
            from = ScopeReduceBuilder<RA, RS, A, S>(scope)
                .apply(block)
                .delegate
        )
    }

    @JvmName(name = "scopeWithTransformSource")
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: ScopeReduceBuilder<RA, RS, T, U>.() -> Unit
    ) {
        delegate.addTransformSourceScope(
            transformSource,
            from = { subScope ->
                ScopeReduceBuilder<RA, RS, T, U>(subScope)
                    .apply(block)
                    .delegate
            }
        )
    }

    fun actionScope(
        block: ActionScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addScope(
            from = ActionScopeReduceBuilder<RA, RS, A, S>(scope)
                .apply(block)
                .delegate
        )
    }

    fun <T : A> actionScope(
        actionType: KClass<T>,
        block: ActionScopeReduceBuilder<RA, RS, T, S>.() -> Unit
    ) {
        delegate.addPredicateScope<A, S>(
            isMatch = { actionType.isInstance(action) },
            from = ActionScopeReduceBuilder<RA, RS, T, S>(scope)
                .apply(block)
                .delegate
        )
    }

    @JvmName(name = "actionScopeWithActionType")
    inline fun <reified T : A> actionScope(noinline block: ActionScopeReduceBuilder<RA, RS, T, S>.() -> Unit) {
        actionScope(actionType = T::class, block)
    }

    fun stateScope(
        block: StateScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addScope(
            from = StateScopeReduceBuilder<RA, RS, A, S>(scope)
                .apply(block)
                .delegate
        )
    }

    fun <T : S> stateScope(
        stateType: KClass<T>,
        block: StateScopeReduceBuilder<RA, RS, A, T>.() -> Unit
    ) {
        delegate.addPredicateScope<A, S>(
            isMatch = { stateType.isInstance(current) },
            from = StateScopeReduceBuilder<RA, RS, A, T>(scope)
                .apply(block)
                .delegate
        )
    }

    @JvmName(name = "stateScopeWithStateType")
    inline fun <reified T : S> stateScope(noinline block: StateScopeReduceBuilder<RA, RS, A, T>.() -> Unit) {
        stateScope(T::class, block)
    }
}