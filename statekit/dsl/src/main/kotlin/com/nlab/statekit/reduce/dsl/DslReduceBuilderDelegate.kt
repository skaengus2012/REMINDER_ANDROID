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

    fun addTransition(block: (DslTransitionScope<A, S>) -> RS) {
        transitionBuilder.add(block)
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun addTransitionWithPredicate(
        predicate: (UpdateSource<A, S>) -> Boolean,
        block: (DslTransitionScope<A, S>) -> RS
    ) {
        addTransition { scope -> if (predicate(scope)) block(scope) else scope.current }
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : Any, U : RS> addTransitionWithTransformSource(
        transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        block: (DslTransitionScope<T, U>) -> RS
    ) {
        addTransition { scope ->
            val newSource = transformSource(scope)
            if (newSource == null) scope.current else block(DslTransitionScope(newSource))
        }
    }

    // TODO define inline function after fix below
    // https://github.com/jacoco/jacoco/pull/1670
    fun <T : A> addTransitionWithActionType(
        actionType: KClass<T>,
        block: (DslTransitionScope<T, S>) -> RS
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
        block: (DslTransitionScope<A, T>) -> RS
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