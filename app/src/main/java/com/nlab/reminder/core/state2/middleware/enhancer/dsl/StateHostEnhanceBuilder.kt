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

package com.nlab.reminder.core.state2.middleware.enhancer.dsl

import com.nlab.reminder.core.state2.Action
import com.nlab.reminder.core.state2.State
import com.nlab.reminder.core.state2.UpdateSource
import com.nlab.reminder.core.state2.dsl.BuilderDsl
import com.nlab.reminder.core.state2.dsl.OperationDsl
import com.nlab.reminder.core.state2.middleware.enhancer.CompositeEnhanceBuilder
import com.nlab.reminder.core.state2.middleware.enhancer.ActionDispatcher
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class StateHostEnhanceBuilder<A : Action, T : A, S : State> internal constructor() {
    private val compositeEnhanceBuilder = CompositeEnhanceBuilder<A, T, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit {
        return compositeEnhanceBuilder.build()
    }

    @OperationDsl
    fun anyState(block: suspend EnhanceEndScope<A>.(UpdateSource<T, S>) -> Unit) {
        compositeEnhanceBuilder.add { block(EnhanceEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: suspend EnhanceEndScope<A>.(UpdateSource<T, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostEnhanceBuilder.anyState]** could not be used
         */
        compositeEnhanceBuilder.add { if (predicate(it.before)) block(EnhanceEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun <U : S> state(
        clazz: KClass<U>,
        block: suspend EnhanceEndScope<A>.(UpdateSource<T, U>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostEnhanceBuilder.filteredState]** could not be used
         */
        compositeEnhanceBuilder.add { (action, before) ->
            if (clazz.isInstance(before)) {
                block(EnhanceEndScope(actionDispatcher = this), UpdateSource(action, clazz.cast(before)))
            }
        }
    }

    @OperationDsl
    inline fun <reified U : S> state(noinline block: suspend EnhanceEndScope<A>.(UpdateSource<T, U>) -> Unit) {
        state(U::class, block)
    }

    @OperationDsl
    fun <U : S> stateNot(
        clazz: KClass<U>,
        block: suspend EnhanceEndScope<A>.(UpdateSource<T, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostEnhanceBuilder.filteredState]** could not be used
         */
        compositeEnhanceBuilder.add { updateSource ->
            if (clazz.isInstance(updateSource.before).not()) {
                block(EnhanceEndScope(actionDispatcher = this), updateSource)
            }
        }
    }

    @OperationDsl
    inline fun <reified U : S> stateNot(noinline block: suspend EnhanceEndScope<A>.(UpdateSource<T, S>) -> Unit) {
        stateNot(U::class, block)
    }
}