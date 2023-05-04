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

package com.nlab.statekit.reducer.dsl

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.dsl.BuilderDsl
import com.nlab.statekit.dsl.OperationDsl
import com.nlab.statekit.reducer.CompositeReduceBuilder
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class DslReduceBuilder<A : Action, S : State> internal constructor() {
    private val reduceEndScope = ReduceEndScope()
    private val compositeReduceBuilder = CompositeReduceBuilder<A, S, S>()

    internal fun build(): (UpdateSource<A, S>) -> S {
        return compositeReduceBuilder.build()
    }

    private fun <T : A> createStateHostReduceBuilder(): StateHostReduceBuilder<T, S> {
        return StateHostReduceBuilder(reduceEndScope)
    }

    private fun <B : S> createActionHostReduceBuilder(): ActionHostReduceBuilder<A, B, S> {
        return ActionHostReduceBuilder(reduceEndScope)
    }

    @OperationDsl
    fun anyAction(block: StateHostReduceBuilder<A, S>.() -> Unit) {
        val reducer = createStateHostReduceBuilder<A>()
            .apply(block)
            .build()
        compositeReduceBuilder.add(reducer)
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: StateHostReduceBuilder<A, S>.() -> Unit
    ) {
        val reduce = createStateHostReduceBuilder<A>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { updateSource ->
            if (predicate(updateSource.action)) reduce(updateSource)
            else updateSource.before
        }
    }

    @OperationDsl
    fun <T : A> action(
        actionClazz: KClass<T>,
        block: StateHostReduceBuilder<T, S>.() -> Unit
    ) {
        val reduce = createStateHostReduceBuilder<T>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { (action, before) ->
            if (actionClazz.isInstance(action).not()) before
            else reduce(UpdateSource(actionClazz.cast(action), before))
        }
    }

    @OperationDsl
    inline fun <reified T : A> action(noinline block: StateHostReduceBuilder<T, S>.() -> Unit) {
        action(T::class, block)
    }

    @OperationDsl
    fun <T : A> actionNot(
        actionClazz: KClass<T>,
        block: StateHostReduceBuilder<A, S>.() -> Unit
    ) {
        val reduce = createStateHostReduceBuilder<A>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { updateSource ->
            if (actionClazz.isInstance(updateSource.action)) updateSource.before
            else reduce(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : A> actionNot(noinline block: StateHostReduceBuilder<A, S>.() -> Unit) {
        actionNot(T::class, block)
    }

    @OperationDsl
    fun anyState(block: ActionHostReduceBuilder<A, S, S>.() -> Unit) {
        val reduce = createActionHostReduceBuilder<S>()
            .apply(block)
            .build()
        compositeReduceBuilder.add(reduce)
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: ActionHostReduceBuilder<A, S, S>.() -> Unit
    ) {
        val reduce = createActionHostReduceBuilder<S>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { updateSource ->
            if (predicate(updateSource.before)) reduce(updateSource) else updateSource.before
        }
    }

    @OperationDsl
    fun <B : S> state(
        stateClazz: KClass<B>,
        block: ActionHostReduceBuilder<A, B, S>.() -> Unit
    ) {
        val reduce = createActionHostReduceBuilder<B>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { (action, before) ->
            if (stateClazz.isInstance(before).not()) before
            else reduce(UpdateSource(action, stateClazz.cast(before)))
        }
    }

    @OperationDsl
    inline fun <reified B : S> state(noinline block: ActionHostReduceBuilder<A, B, S>.() -> Unit) {
        state(B::class, block)
    }

    @OperationDsl
    fun <B : S> stateNot(
        stateClazz: KClass<B>,
        block: ActionHostReduceBuilder<A, S, S>.() -> Unit
    ) {
        val reduce = createActionHostReduceBuilder<S>()
            .apply(block)
            .build()
        compositeReduceBuilder.add { updateSource ->
            if (stateClazz.isInstance(updateSource.before)) updateSource.before
            else reduce(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified B : S> stateNot(noinline block: ActionHostReduceBuilder<A, S, S>.() -> Unit) {
        stateNot(B::class, block)
    }
}