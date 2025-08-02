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
class StateScopeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS> internal constructor(
    private val scope: Any,
    internal val delegate: ReduceBuilderDelegate = ReduceBuilderDelegate(scope)
) : NodeReduceBuilder<RA, RS, A, S> by InternalNodeReduceBuilder(reduceBuilderDelegate = delegate) {
    fun <T : A> transition(
        actionType: KClass<T>,
        block: DslTransitionScope<T, S>.() -> RS
    ) {
        transition {
            if (actionType.isInstance(action)) {
                @Suppress("UNCHECKED_CAST")
                val newScope = this as DslTransitionScope<T, S>
                block(newScope)
            } else {
                current
            }
        }
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "transitionWithActionType")
    inline fun <reified T : A> transition(noinline block: DslTransitionScope<T, S>.() -> RS) {
        transition(actionType = T::class, block = block)
    }

    fun <T : A> effect(
        actionType: KClass<T>,
        block: DslEffectScope<T, S>.() -> Unit
    ) {
        effect {
            if (actionType.isInstance(action)) {
                @Suppress("UNCHECKED_CAST")
                val newScope = this as DslEffectScope<T, S>
                block(newScope)
            }
        }
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "effectWithActionType")
    inline fun <reified T : A> effect(noinline block: DslEffectScope<T, S>.() -> Unit) {
        effect(actionType = T::class, block)
    }

    fun <T : A> suspendEffect(
        actionType: KClass<T>,
        block: suspend DslSuspendEffectScope<RA, T, S>.() -> Unit
    ) {
        delegate.addPredicateScope<A, S>(
            isMatch = { actionType.isInstance(action) },
            child = ReduceBuilderDelegate(scope).apply { addSuspendEffectNode(block) }
        )
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "suspendEffectWithActionType")
    inline fun <reified T : A> suspendEffect(noinline block: suspend DslSuspendEffectScope<RA, T, S>.() -> Unit) {
        suspendEffect(actionType = T::class, block)
    }

    @JvmName(name = "scopeWithPredicate")
    fun scope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        block: StateScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addPredicateScope(
            isMatch,
            child = StateScopeReduceBuilder<RA, RS, A, S>(scope)
                .apply(block)
                .delegate
        )
    }

    @JvmName(name = "scopeWithTransformSource")
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: StateScopeReduceBuilder<RA, RS, T, U>.() -> Unit
    ) {
        delegate.addTransformSourceScope(
            transformSource,
            child = { subScope ->
                StateScopeReduceBuilder<RA, RS, T, U>(subScope)
                    .apply(block)
                    .delegate
            }
        )
    }

    fun <T : S> scope(
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
    @JvmName(name = "scopeWithStateType")
    inline fun <reified T : S> scope(noinline block: StateScopeReduceBuilder<RA, RS, A, T>.() -> Unit) {
        scope(stateType = T::class, block)
    }
}