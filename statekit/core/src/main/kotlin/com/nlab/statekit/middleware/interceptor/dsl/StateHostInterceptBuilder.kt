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
class StateHostInterceptBuilder<A : Action, T : A, S : State> internal constructor() {
    private val compositeInterceptBuilder = CompositeInterceptBuilder<A, T, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit {
        return compositeInterceptBuilder.build()
    }

    @OperationDsl
    fun anyState(block: suspend InterceptEndScope<A>.(UpdateSource<T, S>) -> Unit) {
        compositeInterceptBuilder.add { block(InterceptEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: suspend InterceptEndScope<A>.(UpdateSource<T, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostInterceptBuilder.anyState]** could not be used
         */
        compositeInterceptBuilder.add { if (predicate(it.before)) block(InterceptEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun <U : S> state(
        clazz: KClass<U>,
        block: suspend InterceptEndScope<A>.(UpdateSource<T, U>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostInterceptBuilder.filteredState]** could not be used
         */
        compositeInterceptBuilder.add { (action, before) ->
            if (clazz.isInstance(before)) {
                block(InterceptEndScope(actionDispatcher = this), UpdateSource(action, clazz.cast(before)))
            }
        }
    }

    @OperationDsl
    inline fun <reified U : S> state(noinline block: suspend InterceptEndScope<A>.(UpdateSource<T, U>) -> Unit) {
        state(U::class, block)
    }

    @OperationDsl
    fun <U : S> stateNot(
        clazz: KClass<U>,
        block: suspend InterceptEndScope<A>.(UpdateSource<T, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[StateHostInterceptBuilder.filteredState]** could not be used
         */
        compositeInterceptBuilder.add { updateSource ->
            if (clazz.isInstance(updateSource.before).not()) {
                block(InterceptEndScope(actionDispatcher = this), updateSource)
            }
        }
    }

    @OperationDsl
    inline fun <reified U : S> stateNot(noinline block: suspend InterceptEndScope<A>.(UpdateSource<T, S>) -> Unit) {
        stateNot(U::class, block)
    }
}