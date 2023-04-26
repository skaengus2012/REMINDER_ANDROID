/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.middleware.enhancer.dsl

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.dsl.BuilderDsl
import com.nlab.statekit.dsl.OperationDsl
import com.nlab.statekit.middleware.enhancer.CompositeEnhanceBuilder
import com.nlab.statekit.middleware.enhancer.ActionDispatcher
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class DslEnhanceBuilder<A : Action, S : State> internal constructor() {
    private val compositeEnhanceBuilder = CompositeEnhanceBuilder<A, A, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit {
        return compositeEnhanceBuilder.build()
    }

    private fun <T : A> createStateHostEnhanceBuilder(): StateHostEnhanceBuilder<A, T, S> {
        return StateHostEnhanceBuilder()
    }

    private fun <T : S> createActionHostEnhanceBuilder(): ActionHostEnhanceBuilder<A, T> {
        return ActionHostEnhanceBuilder()
    }

    @OperationDsl
    fun anyAction(block: StateHostEnhanceBuilder<A, A, S>.() -> Unit) {
        val enhancer = createStateHostEnhanceBuilder<A>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add(enhancer)
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: StateHostEnhanceBuilder<A, A, S>.() -> Unit
    ) {
        val enhancer = createStateHostEnhanceBuilder<A>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { updateSource ->
            if (predicate(updateSource.action)) enhancer(updateSource)
        }
    }

    @OperationDsl
    fun <T : A> action(
        actionClazz: KClass<T>,
        block: StateHostEnhanceBuilder<A, T, S>.() -> Unit
    ) {
        val enhancer = createStateHostEnhanceBuilder<T>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { (action, before) ->
            if (actionClazz.isInstance(action)) {
                enhancer(UpdateSource(actionClazz.cast(action), before))
            }
        }
    }

    @OperationDsl
    inline fun <reified T : A> action(
        noinline block: StateHostEnhanceBuilder<A, T, S>.() -> Unit
    ) {
        action(T::class, block)
    }

    @OperationDsl
    fun <T : A> actionNot(
        actionClazz: KClass<T>,
        block: StateHostEnhanceBuilder<A, A, S>.() -> Unit
    ) {
        val enhancer = createStateHostEnhanceBuilder<A>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { updateSource ->
            if (actionClazz.isInstance(updateSource.action).not()) enhancer(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : A> actionNot(
        noinline block: StateHostEnhanceBuilder<A, A, S>.() -> Unit
    ) {
        actionNot(T::class, block)
    }

    @OperationDsl
    fun anyState(block: ActionHostEnhanceBuilder<A, S>.() -> Unit) {
        val enhancer = createActionHostEnhanceBuilder<S>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add(enhancer)
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: ActionHostEnhanceBuilder<A, S>.() -> Unit
    ) {
        val enhancer = createActionHostEnhanceBuilder<S>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { updateSource ->
            if (predicate(updateSource.before)) enhancer(updateSource)
        }
    }

    @OperationDsl
    fun <T : S> state(
        stateClazz: KClass<T>,
        block: ActionHostEnhanceBuilder<A, T>.() -> Unit
    ) {
        val enhancer = createActionHostEnhanceBuilder<T>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { (action, before) ->
            if (stateClazz.isInstance(before)) {
                enhancer(UpdateSource(action, stateClazz.cast(before)))
            }
        }
    }

    @OperationDsl
    inline fun <reified T : S> state(
        noinline block: ActionHostEnhanceBuilder<A, T>.() -> Unit
    ) {
        state(T::class, block)
    }

    @OperationDsl
    fun <T : S> stateNot(
        stateClazz: KClass<T>,
        block: ActionHostEnhanceBuilder<A, S>.() -> Unit
    ) {
        val enhancer = createActionHostEnhanceBuilder<S>()
            .apply(block)
            .build()
        compositeEnhanceBuilder.add { updateSource ->
            if (stateClazz.isInstance(updateSource.before).not()) enhancer(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : S> stateNot(
        noinline block: ActionHostEnhanceBuilder<A, S>.() -> Unit
    ) {
        stateNot(T::class, block)
    }
}