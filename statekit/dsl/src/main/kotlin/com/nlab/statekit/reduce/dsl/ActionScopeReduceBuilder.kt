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

/**
 * @author Doohyun
 */
@BuilderDsl
class ActionScopeReduceBuilder<A : Any, S : RS, RA : Any, RS : Any> internal constructor(
    private val scope: Any
) {
    private val transitionBuilder = DslTransitionBuilder(scope)

    internal fun buildTransition(): DslTransition? = transitionBuilder.build()

    @OperationDsl
    fun transition(block: DslTransitionScope<A, S>.() -> RS) {
    //    transitions.add(DslTransition.NodeTransition(scope = this, next = block))
    }
/**
    @OperationDsl
    fun <T : S> transition(
        stateType: KClass<T>,
        block: DslTransitionScope<A, T>.() -> RS
    ) {
        delegate.addTransitionWithStateType(stateType, block)
    }

    @JvmName(name = "transitionWithStateType")
    @OperationDsl
    inline fun <reified T : S> transition(noinline block: DslTransitionScope<A, T>.() -> RS) {
        transition(T::class, block)
    }

    @OperationDsl
    fun effect(block: suspend DslEffectScope<A, S, RA>.() -> Unit) {
        delegate.addEffect(block)
    }

    @OperationDsl
    fun <T : S> effect(
        stateType: KClass<T>,
        block: suspend DslEffectScope<A, T, RA>.() -> Unit
    ) {
        delegate.addEffectWithStateType(stateType, block)
    }

    @JvmName(name = "effectWithStateType")
    @OperationDsl
    inline fun <reified T : S> effect(noinline block: suspend DslEffectScope<A, T, RA>.() -> Unit) {
        effect(T::class, block)
    }

    @JvmName(name = "scopeWithPredicate")
    @OperationDsl
    fun scope(
        predicate: UpdateSource<A, S>.() -> Boolean,
        block: ActionScopeReduceBuilder<A, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ActionScopeReduceBuilder<A, S, RA, RS>().apply(block)
        delegate.addTransitionWithPredicate(predicate, subReduceBuilder.buildTransition())
        delegate.addEffectWithPredicate(predicate, subReduceBuilder.buildEffect())
    }

    @JvmName(name = "scopeWithTransformSource")
    @OperationDsl
    fun <T : Any, U : RS> scope(
        transformSource: UpdateSource<A, S>.() -> UpdateSource<T, U>?,
        block: ActionScopeReduceBuilder<T, U, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ActionScopeReduceBuilder<T, U, RA, RS>().apply(block)
        delegate.addTransitionWithTransformSource(transformSource, subReduceBuilder.buildTransition())
        delegate.addEffectWithTransformSource(transformSource, subReduceBuilder.buildEffect())
    }

    @OperationDsl
    fun <T : A> scope(
        actionType: KClass<T>,
        block: ActionScopeReduceBuilder<T, S, RA, RS>.() -> Unit
    ) {
        val subReduceBuilder = ActionScopeReduceBuilder<T, S, RA, RS>().apply(block)
        delegate.addTransitionWithActionType(actionType, subReduceBuilder.buildTransition())
        delegate.addEffectWithActionType(actionType, subReduceBuilder.buildEffect())
    }

    @OperationDsl
    inline fun <reified T : A> scope(noinline block: ActionScopeReduceBuilder<T, S, RA, RS>.() -> Unit) {
        scope(T::class, block)
    }*/
}