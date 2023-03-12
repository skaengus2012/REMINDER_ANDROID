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

package com.nlab.reminder.core.state2.reducer.dsl

import com.nlab.reminder.core.state2.*
import com.nlab.reminder.core.state2.dsl.*
import com.nlab.reminder.core.state2.reducer.CompositeReduceBuilder
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@BuilderDsl
class ActionHostReduceBuilder<A : Action, B : S, S : State> internal constructor(
    private val reduceEndScope: ReduceEndScope
) {
    private val compositeReduceBuilder = CompositeReduceBuilder<A, B, S>()

    internal fun build(): (UpdateSource<A, B>) -> S {
        return compositeReduceBuilder.build()
    }

    @OperationDsl
    fun anyAction(block: ReduceEndScope.(UpdateSource<A, B>) -> S) {
        compositeReduceBuilder.add { updateSource -> reduceEndScope.block(updateSource) }
    }

    @OperationDsl
    fun filteredAction(
        predicate: (A) -> Boolean,
        block: ReduceEndScope.(UpdateSource<A, B>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostReduceBuilder.anyAction]** could not be used
         */
        compositeReduceBuilder.add { if (predicate(it.action)) reduceEndScope.block(it) else it.before }
    }

    @OperationDsl
    fun <T : A> action(
        actionClazz: KClass<T>,
        block: ReduceEndScope.(UpdateSource<T, B>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostReduceBuilder.filteredAction]** could not be used
         */
        compositeReduceBuilder.add { (action, before) ->
            if (actionClazz.isInstance(action).not()) before
            else reduceEndScope.block(UpdateSource(actionClazz.cast(action), before))
        }
    }

    @OperationDsl
    inline fun <reified T : A> action(noinline block: ReduceEndScope.(UpdateSource<T, B>) -> S) {
        action(T::class, block)
    }

    @OperationDsl
    fun <T : A> actionNot(
        actionClazz: KClass<T>,
        block: ReduceEndScope.(UpdateSource<A, B>) -> S
    ) {
        /**
         * Jacoco was unable to recognize inline,
         * so **[ActionHostReduceBuilder.filteredAction]** could not be used
         */
        compositeReduceBuilder.add { updateSource ->
            if (actionClazz.isInstance(updateSource.action)) updateSource.before
            else reduceEndScope.block(updateSource)
        }
    }

    @OperationDsl
    inline fun <reified T : A> actionNot(noinline block: ReduceEndScope.(UpdateSource<A, B>) -> S) {
        actionNot(T::class, block)
    }
}