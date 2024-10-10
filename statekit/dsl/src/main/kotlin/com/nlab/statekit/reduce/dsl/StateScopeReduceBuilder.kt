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

import com.nlab.statekit.annotation.*
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
@BuilderDsl
class StateScopeReduceBuilder<A : Any, S : RS, RA : Any, RS : Any> internal constructor(
    private val delegate: DslReduceBuilderDelegate<A, S, RA, RS> = DslReduceBuilderDelegate()
) {
    internal fun buildTransition() = delegate.buildTransition()

    internal fun buildEffect() = delegate.buildEffect()

    @OperationDsl
    fun transition(block: DslTransitionScope<A, S>.() -> RS) {
        delegate.addTransition(block)
    }

    @OperationDsl
    fun <T : A> transition(
        actionType: KClass<T>,
        block: DslTransitionScope<T, S>.() -> RS
    ) {
        delegate.addTransitionWithActionType(actionType, block)
    }

    @JvmName(name = "transitionWithActionType")
    @OperationDsl
    inline fun <reified T : A> transition(noinline block: DslTransitionScope<T, S>.() -> RS) {
        transition(T::class, block)
    }

    @OperationDsl
    fun effect(block: suspend DslEffectScope<A, S, RA>.() -> Unit) {
        delegate.addEffect(block)
    }

    @OperationDsl
    fun <T : A> effect(
        actionType: KClass<T>,
        block: suspend DslEffectScope<T, S, RA>.() -> Unit
    ) {
        delegate.addEffectWithActionType(actionType, block)
    }

    @JvmName(name = "effectWithActionType")
    @OperationDsl
    inline fun <reified T : A> effect(noinline block: suspend DslEffectScope<T, S, RA>.() -> Unit) {
        effect(T::class, block)
    }

    @JvmName(name = "scopeWithPredicate")
    @OperationDsl
    fun scope(
        predicate: UpdateSource<A, S>.() -> Boolean,
        block: StateScopeReduceBuilder<A, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = StateScopeReduceBuilder<A, S, RA, RS>().apply(block)
        delegate.addTransitionWithPredicate(predicate, subReduceBuilder.buildTransition())
        delegate.addEffectWithPredicate(predicate, subReduceBuilder.buildEffect())
    }

    @JvmName(name = "scopeWithTransformSource")
    @OperationDsl
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: StateScopeReduceBuilder<T, U, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = StateScopeReduceBuilder<T, U, RA, RS>().apply(block)
        delegate.addTransitionWithTransformSource(transformSource, subReduceBuilder.buildTransition())
        delegate.addEffectWithTransformSource(transformSource, subReduceBuilder.buildEffect())
    }

    @OperationDsl
    fun <T : S> scope(
        stateType: KClass<T>,
        block: StateScopeReduceBuilder<A, T, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = StateScopeReduceBuilder<A, T, RA, RS>().apply(block)
        delegate.addTransitionWithStateType(stateType, subReduceBuilder.buildTransition())
        delegate.addEffectWithStateType(stateType, subReduceBuilder.buildEffect())
    }

    @OperationDsl
    inline fun <reified T : S> scope(noinline block: StateScopeReduceBuilder<A, T, RA, RS>.() -> Unit) {
        scope(T::class, block)
    }
}