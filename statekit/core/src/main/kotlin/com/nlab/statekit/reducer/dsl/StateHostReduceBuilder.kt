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

import com.nlab.statekit.reducer.CompositeReduceBuilder
import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.dsl.BuilderDsl
import com.nlab.statekit.dsl.OperationDsl
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class StateHostReduceBuilder<A : Action, S : State> internal constructor(
    private val reduceEndScope: ReduceEndScope
) {
    private val compositeReduceBuilder = CompositeReduceBuilder<A, S, S>()

    internal fun build(): (UpdateSource<A, S>) -> S {
        return compositeReduceBuilder.build()
    }

    @OperationDsl
    fun anyState(block: ReduceEndScope.(UpdateSource<A, S>) -> S) {
        compositeReduceBuilder.add { updateSource -> reduceEndScope.block(updateSource) }
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: ReduceEndScope.(UpdateSource<A, S>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostReduceBuilder.anyState]** could not be used
         */
        compositeReduceBuilder.add { if (predicate(it.before)) reduceEndScope.block(it) else it.before }
    }

    @OperationDsl
    fun <T : State> state(
        stateClazz: KClass<T>,
        block: ReduceEndScope.(UpdateSource<A, T>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostReduceBuilder.filteredState]** could not be used
         */
        compositeReduceBuilder.add { (action, before) ->
            if (stateClazz.isInstance(before).not()) before
            else reduceEndScope.block(UpdateSource(action, stateClazz.cast(before)))
        }
    }

    @OperationDsl
    inline fun <reified T : State> state(noinline block: ReduceEndScope.(UpdateSource<A, T>) -> S) {
        state(T::class, block)
    }

    @OperationDsl
    fun <T : State> stateNot(
        stateClazz: KClass<T>,
        block: ReduceEndScope.(UpdateSource<A, S>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostReduceBuilder.filteredState]** could not be used
         */
        compositeReduceBuilder.add { updateSource ->
            if (stateClazz.isInstance(updateSource.before)) updateSource.before
            else reduceEndScope.block(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : State> stateNot(noinline block: ReduceEndScope.(UpdateSource<A, S>) -> S) {
        stateNot(T::class, block)
    }
}