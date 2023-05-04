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
class ActionHostInterceptBuilder<A : Action, S : State> internal constructor() {
    private val compositeInterceptBuilder = CompositeInterceptBuilder<A, A, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit {
        return compositeInterceptBuilder.build()
    }

    @OperationDsl
    fun anyAction(block: suspend InterceptEndScope<A>.(UpdateSource<A, S>) -> Unit) {
        compositeInterceptBuilder.add { block(InterceptEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: suspend InterceptEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostInterceptBuilder.anyAction]** could not be used
         */
        compositeInterceptBuilder.add {
            if (predicate(it.action)) block(InterceptEndScope(actionDispatcher = this), it)
        }
    }

    @OperationDsl
    fun <U : A> action(
        clazz: KClass<U>,
        block: suspend InterceptEndScope<A>.(UpdateSource<U, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostInterceptBuilder.filteredAction]** could not be used
         */
        compositeInterceptBuilder.add { (action, before) ->
            if (clazz.isInstance(action)) {
                block(InterceptEndScope(actionDispatcher = this), UpdateSource(clazz.cast(action), before))
            }
        }
    }

    @OperationDsl
    inline fun <reified U : A> action(
        noinline block: suspend InterceptEndScope<A>.(UpdateSource<U, S>) -> Unit
    ) {
        action(U::class, block)
    }

    @OperationDsl
    fun <U : A> actionNot(
        clazz: KClass<U>,
        block: suspend InterceptEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostInterceptBuilder.filteredAction]** could not be used
         */
        compositeInterceptBuilder.add { updateSource ->
            if (clazz.isInstance(updateSource.action).not()) {
                block(InterceptEndScope(actionDispatcher = this),  updateSource)
            }
        }
    }

    @OperationDsl
    inline fun <reified U : A> actionNot(
        noinline block: suspend InterceptEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        actionNot(U::class, block)
    }
}