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

import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
internal class DslReduceBuilderDelegate<A : Any, S : RS, RA : Any, RS : Any> {
    private val transitionBuilder = DslTransitionBuilder<RS, A, S>()
    private val effectBuilder = DslEffectBuilder<A, S, RA>()

    fun buildTransition() = transitionBuilder.build()
    fun buildEffect() = effectBuilder.build()

    fun addTransition(transition: DslTransition<RS, A, S>?) {
        transition ?: return
        transitionBuilder.add(transition)
    }

    fun addNodeTransition(block: DslTransition.NodeTransition<RS, A, S>) {
        transitionBuilder.add(block)
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun addTransitionWithPredicate(
        predicate: (UpdateSource<A, S>) -> Boolean,
        transition: DslTransition<RS, A, S>?
    ) {
        transition ?: return
        transitionBuilder.add(
            DslTransition.PredicateScopeTransition(
                predicate,
                transition = transition
            )
        )
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : Any, U : RS> addTransitionWithTransformSource(
        transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        transition: DslTransition<RS, T, U>?
    ) {
        transition ?: return
        transitionBuilder.add(
            DslTransition.TransformSourceScopeTransition(transformSource, transition)
        )
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : A> addTransitionWithActionType(
        actionType: KClass<T>,
        block: DslTransition.NodeTransition<RS, T, S>
    ) {
        addTransitionWithTransformSource(
            transformSource = { it.tryCopyWithActionType(actionType) },
            block
        )
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : S> addTransitionWithStateType(
        stateType: KClass<T>,
        block: DslTransition.NodeTransition<RS, A, T>
    ) {
        addTransitionWithTransformSource(
            transformSource = { it.tryCopyWithStateType(stateType) },
            block
        )
    }

    fun addEffect(block: suspend (DslEffectScope<A, S, RA>) -> Unit) {
        effectBuilder.add(block)
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun addEffectWithPredicate(
        predicate: (UpdateSource<A, S>) -> Boolean,
        block: suspend (DslEffectScope<A, S, RA>) -> Unit
    ) {
        addEffect { scope -> if (predicate(scope)) block(scope) }
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : Any, U : RS> addEffectWithTransformSource(
        transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        block: suspend (DslEffectScope<T, U, RA>) -> Unit
    ) {
        addEffect { scope ->
            transformSource(scope)?.let { newSource -> block(DslEffectScope(newSource, scope.actionDispatcher)) }
        }
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : A> addEffectWithActionType(
        actionType: KClass<T>,
        block: suspend (DslEffectScope<T, S, RA>) -> Unit
    ) {
        addEffectWithTransformSource(
            transformSource = { it.tryCopyWithActionType(actionType) },
            block
        )
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : S> addEffectWithStateType(
        stateType: KClass<T>,
        block: suspend (DslEffectScope<A, T, RA>) -> Unit
    ) {
        addEffectWithTransformSource(
            transformSource = { it.tryCopyWithStateType(stateType) },
            block
        )
    }
}