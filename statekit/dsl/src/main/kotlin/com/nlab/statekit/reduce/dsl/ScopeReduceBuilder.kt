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
class ScopeReduceBuilder<A : Any, S : RS, RA : Any, RS : Any> internal constructor() {
    private val delegate = DslReduceBuilderDelegate<A, S, RA, RS>()

    internal fun buildTransition() = delegate.buildTransition()

    internal fun buildEffect() = delegate.buildEffect()

    @OperationDsl
    fun transition(block: DslTransitionScope<A, S>.() -> RS) {
        delegate.addTransition(block)
    }

    @OperationDsl
    fun <T : Any, U : S> transition(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: DslTransitionScope<T, U>.() -> RS
    ) {
        delegate.addTransitionWithTransformSource(transformSource, block)
    }

    @OperationDsl
    fun effect(block: suspend DslEffectScope<A, S, RA>.() -> Unit) {
        delegate.addEffect(block)
    }

    @OperationDsl
    fun <T : Any, U : RS> effect(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: suspend DslEffectScope<T, U, RA>.() -> Unit
    ) {
        delegate.addEffectWithTransformSource(transformSource, block)
    }

    @JvmName(name = "scopeWithPredicate")
    @OperationDsl
    fun scope(
        predicate: UpdateSource<A, S>.() -> Boolean,
        block: ScopeReduceBuilder<A, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ScopeReduceBuilder<A, S, RA, RS>().apply(block)
        delegate.addTransitionWithPredicate(predicate, subReduceBuilder.buildTransition())
        delegate.addEffectWithPredicate(predicate, subReduceBuilder.buildEffect())
    }

    @JvmName(name = "scopeWithTransformSource")
    @OperationDsl
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: ScopeReduceBuilder<T, U, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ScopeReduceBuilder<T, U, RA, RS>().apply(block)
        delegate.addTransitionWithTransformSource(transformSource, subReduceBuilder.buildTransition())
        delegate.addEffectWithTransformSource(transformSource, subReduceBuilder.buildEffect())
    }

    @OperationDsl
    fun actionScope(
        block: ActionScopeReduceBuilder<A, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ActionScopeReduceBuilder<A, S, RA, RS>().apply(block)
        delegate.addTransition(subReduceBuilder.buildTransition())
        delegate.addEffect(subReduceBuilder.buildEffect())
    }

    @OperationDsl
    fun <T : A> actionScope(
        actionType: KClass<T>,
        block: ActionScopeReduceBuilder<T, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ActionScopeReduceBuilder<T, S, RA, RS>().apply(block)
        delegate.addTransitionWithActionType(actionType, subReduceBuilder.buildTransition())
        delegate.addEffectWithActionType(actionType, subReduceBuilder.buildEffect())
    }

    @JvmName(name = "actionScopeWithActionType")
    @OperationDsl
    inline fun <reified T : A> actionScope(noinline block: ActionScopeReduceBuilder<T, S, RA, RS>.() -> Unit) {
        actionScope(T::class, block)
    }

    @OperationDsl
    fun stateScope(
        block: StateScopeReduceBuilder<A, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = StateScopeReduceBuilder<A, S, RA, RS>().apply(block)
        delegate.addTransition(subReduceBuilder.buildTransition())
        delegate.addEffect(subReduceBuilder.buildEffect())
    }

    @OperationDsl
    fun <T : S> stateScope(
        stateType: KClass<T>,
        block: StateScopeReduceBuilder<A, T, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = StateScopeReduceBuilder<A, T, RA, RS>().apply(block)
        delegate.addTransitionWithStateType(stateType, subReduceBuilder.buildTransition())
        delegate.addEffectWithStateType(stateType, subReduceBuilder.buildEffect())
    }

    @JvmName(name = "stateScopeWithStateType")
    @OperationDsl
    inline fun <reified T : S> stateScope(noinline block: StateScopeReduceBuilder<A, T, RA, RS>.() -> Unit) {
        stateScope(T::class, block)
    }
}