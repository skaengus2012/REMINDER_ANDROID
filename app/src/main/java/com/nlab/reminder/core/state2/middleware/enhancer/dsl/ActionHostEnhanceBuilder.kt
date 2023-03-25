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
class ActionHostEnhanceBuilder<A : Action, S : State> internal constructor() {
    private val compositeEnhanceBuilder = CompositeEnhanceBuilder<A, A, S>()

    internal fun build(): suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit {
        return compositeEnhanceBuilder.build()
    }

    @OperationDsl
    fun anyAction(block: suspend EnhanceEndScope<A>.(UpdateSource<A, S>) -> Unit) {
        compositeEnhanceBuilder.add { block(EnhanceEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: suspend EnhanceEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostEnhanceBuilder.anyAction]** could not be used
         */
        compositeEnhanceBuilder.add { if (predicate(it.action)) block(EnhanceEndScope(actionDispatcher = this), it) }
    }

    @OperationDsl
    fun <U : A> action(
        clazz: KClass<U>,
        block: suspend EnhanceEndScope<A>.(UpdateSource<U, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostEnhanceBuilder.filteredAction]** could not be used
         */
        compositeEnhanceBuilder.add { (action, before) ->
            if (clazz.isInstance(action)) {
                block(EnhanceEndScope(actionDispatcher = this), UpdateSource(clazz.cast(action), before))
            }
        }
    }

    @OperationDsl
    inline fun <reified U : A> action(
        noinline block: suspend EnhanceEndScope<A>.(UpdateSource<U, S>) -> Unit
    ) {
        action(U::class, block)
    }

    @OperationDsl
    fun <U : A> actionNot(
        clazz: KClass<U>,
        block: suspend EnhanceEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostEnhanceBuilder.filteredAction]** could not be used
         */
        compositeEnhanceBuilder.add { updateSource ->
            if (clazz.isInstance(updateSource.action).not()) {
                block(EnhanceEndScope(actionDispatcher = this),  updateSource)
            }
        }
    }

    @OperationDsl
    inline fun <reified U : A> actionNot(
        noinline block: suspend EnhanceEndScope<A>.(UpdateSource<A, S>) -> Unit
    ) {
        actionNot(U::class, block)
    }
}