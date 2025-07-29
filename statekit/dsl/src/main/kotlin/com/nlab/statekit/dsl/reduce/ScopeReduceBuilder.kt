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
import com.nlab.statekit.dsl.internal.ExcludeFromGeneratedTestReport
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
@DslReduceMarker
class ScopeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS> internal constructor(
    private val scope: Any,
    internal val delegate: ReduceBuilderDelegate = ReduceBuilderDelegate(scope)
) : NodeReduceBuilder<RA, RS, A, S> by InternalNodeReduceBuilder(reduceBuilderDelegate = delegate) {
    @JvmName(name = "scopeWithPredicate")
    fun scope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        block: ScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addPredicateScope(
            isMatch = isMatch,
            child = ScopeReduceBuilder<RA, RS, A, S>(scope)
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
            child = { subScope ->
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
            child = ActionScopeReduceBuilder<RA, RS, A, S>(scope)
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
            child = ActionScopeReduceBuilder<RA, RS, T, S>(scope)
                .apply(block)
                .delegate
        )
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "actionScopeWithActionType")
    inline fun <reified T : A> actionScope(noinline block: ActionScopeReduceBuilder<RA, RS, T, S>.() -> Unit) {
        actionScope(actionType = T::class, block)
    }

    fun stateScope(
        block: StateScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addScope(
            child = StateScopeReduceBuilder<RA, RS, A, S>(scope)
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
            child = StateScopeReduceBuilder<RA, RS, A, T>(scope)
                .apply(block)
                .delegate
        )
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "stateScopeWithStateType")
    inline fun <reified T : S> stateScope(noinline block: StateScopeReduceBuilder<RA, RS, A, T>.() -> Unit) {
        stateScope(T::class, block)
    }
}