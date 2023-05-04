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

package com.nlab.statekit.middleware.interceptor.dsl

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.dsl.BuilderDsl
import com.nlab.statekit.dsl.OperationDsl
import com.nlab.statekit.middleware.interceptor.CompositeInterceptBuilder
import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class DslInterceptBuilder<A : Action, S : State> internal constructor() {
    private val compositeInterceptBuilder = CompositeInterceptBuilder<A, A, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit {
        return compositeInterceptBuilder.build()
    }

    private fun <T : A> createStateHostInterceptBuilder(): StateHostInterceptBuilder<A, T, S> {
        return StateHostInterceptBuilder()
    }

    private fun <T : S> createActionHostInterceptBuilder(): ActionHostInterceptBuilder<A, T> {
        return ActionHostInterceptBuilder()
    }

    @OperationDsl
    fun anyAction(block: StateHostInterceptBuilder<A, A, S>.() -> Unit) {
        val interceptor = createStateHostInterceptBuilder<A>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add(interceptor)
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: StateHostInterceptBuilder<A, A, S>.() -> Unit
    ) {
        val interceptor = createStateHostInterceptBuilder<A>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { updateSource ->
            if (predicate(updateSource.action)) interceptor(updateSource)
        }
    }

    @OperationDsl
    fun <T : A> action(
        actionClazz: KClass<T>,
        block: StateHostInterceptBuilder<A, T, S>.() -> Unit
    ) {
        val interceptor = createStateHostInterceptBuilder<T>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { (action, before) ->
            if (actionClazz.isInstance(action)) {
                interceptor(UpdateSource(actionClazz.cast(action), before))
            }
        }
    }

    @OperationDsl
    inline fun <reified T : A> action(
        noinline block: StateHostInterceptBuilder<A, T, S>.() -> Unit
    ) {
        action(T::class, block)
    }

    @OperationDsl
    fun <T : A> actionNot(
        actionClazz: KClass<T>,
        block: StateHostInterceptBuilder<A, A, S>.() -> Unit
    ) {
        val interceptor = createStateHostInterceptBuilder<A>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { updateSource ->
            if (actionClazz.isInstance(updateSource.action).not()) interceptor(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : A> actionNot(
        noinline block: StateHostInterceptBuilder<A, A, S>.() -> Unit
    ) {
        actionNot(T::class, block)
    }

    @OperationDsl
    fun anyState(block: ActionHostInterceptBuilder<A, S>.() -> Unit) {
        val interceptor = createActionHostInterceptBuilder<S>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add(interceptor)
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: ActionHostInterceptBuilder<A, S>.() -> Unit
    ) {
        val interceptor = createActionHostInterceptBuilder<S>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { updateSource ->
            if (predicate(updateSource.before)) interceptor(updateSource)
        }
    }

    @OperationDsl
    fun <T : S> state(
        stateClazz: KClass<T>,
        block: ActionHostInterceptBuilder<A, T>.() -> Unit
    ) {
        val interceptor = createActionHostInterceptBuilder<T>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { (action, before) ->
            if (stateClazz.isInstance(before)) {
                interceptor(UpdateSource(action, stateClazz.cast(before)))
            }
        }
    }

    @OperationDsl
    inline fun <reified T : S> state(
        noinline block: ActionHostInterceptBuilder<A, T>.() -> Unit
    ) {
        state(T::class, block)
    }

    @OperationDsl
    fun <T : S> stateNot(
        stateClazz: KClass<T>,
        block: ActionHostInterceptBuilder<A, S>.() -> Unit
    ) {
        val interceptor = createActionHostInterceptBuilder<S>()
            .apply(block)
            .build()
        compositeInterceptBuilder.add { updateSource ->
            if (stateClazz.isInstance(updateSource.before).not()) interceptor(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : S> stateNot(
        noinline block: ActionHostInterceptBuilder<A, S>.() -> Unit
    ) {
        stateNot(T::class, block)
    }
}