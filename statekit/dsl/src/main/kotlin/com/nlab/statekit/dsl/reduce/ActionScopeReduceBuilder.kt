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
class ActionScopeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS> internal constructor(
    private val scope: Any
) {
    internal val delegate: ReduceBuilderDelegate = ReduceBuilderDelegate(scope)

    fun transition(block: DslTransitionScope<A, S>.() -> RS) {
        delegate.addTransitionNode(block)
    }

    fun <T : S> transition(
        stateType: KClass<T>,
        block: DslTransitionScope<A, T>.() -> RS
    ) {
        transition {
            if (stateType.isInstance(current)) {
                @Suppress("UNCHECKED_CAST")
                val newScope = this as DslTransitionScope<A, T>
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
    @JvmName(name = "transitionWithStateType")
    inline fun <reified T : S> transition(noinline block: DslTransitionScope<A, T>.() -> RS) {
        transition(stateType = T::class, block = block)
    }

    fun effect(block: DslEffectScope<A, S>.() -> Unit) {
        delegate.addEffectNode(block)
    }

    fun <T : S> effect(
        stateType: KClass<T>,
        block: DslEffectScope<A, T>.() -> Unit
    ) {
        effect {
            if (stateType.isInstance(current)) {
                @Suppress("UNCHECKED_CAST")
                val newScope = this as DslEffectScope<A, T>
                block(newScope)
            }
        }
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "effectWithStateType")
    inline fun <reified T : S> effect(noinline block: DslEffectScope<A, T>.() -> Unit) {
        effect(stateType = T::class, block)
    }

    fun suspendEffect(block: suspend DslSuspendEffectScope<RA, A, S>.() -> Unit) {
        delegate.addSuspendEffectNode(block)
    }

    fun <T : S> suspendEffect(
        stateType: KClass<T>,
        block: suspend DslSuspendEffectScope<RA, A, T>.() -> Unit
    ) {
        delegate.addPredicateScope<A, S>(
            isMatch = { stateType.isInstance(current) },
            from = ReduceBuilderDelegate(scope).apply { addSuspendEffectNode(block) }
        )
    }

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "suspendEffectWithStateType")
    inline fun <reified T : S> suspendEffect(noinline block: suspend DslSuspendEffectScope<RA, A, T>.() -> Unit) {
        suspendEffect(stateType = T::class, block)
    }

    @JvmName(name = "scopeWithPredicate")
    fun scope(
        isMatch: UpdateSource<A, S>.() -> Boolean,
        block: ActionScopeReduceBuilder<RA, RS, A, S>.() -> Unit
    ) {
        delegate.addPredicateScope(
            isMatch,
            from = ActionScopeReduceBuilder<RA, RS, A, S>(scope)
                .apply(block)
                .delegate
        )
    }

    @JvmName(name = "scopeWithTransformSource")
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: ActionScopeReduceBuilder<RA, RS, T, U>.() -> Unit
    ) {
        delegate.addTransformSourceScope(
            transformSource,
            from = { subScope ->
                ActionScopeReduceBuilder<RA, RS, T, U>(subScope)
                    .apply(block)
                    .delegate
            }
        )
    }

    fun <T : A> scope(
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

    // TODO remove Generated annotation after deploy below issue
    // https://github.com/jacoco/jacoco/issues/1873
    // In Jacoco 0.8.13, inline functions are included in test scope
    // However, the jacoco maven plugin fails to read the code, coverage is not filled.
    @ExcludeFromGeneratedTestReport
    @JvmName(name = "scopeWithActionType")
    inline fun <reified T : A> scope(noinline block: ActionScopeReduceBuilder<RA, RS, T, S>.() -> Unit) {
        scope(actionType = T::class, block)
    }
}