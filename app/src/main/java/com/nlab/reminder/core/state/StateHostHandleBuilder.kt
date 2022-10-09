/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.state

import com.nlab.reminder.core.util.test.annotation.NoInlineRequired
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@StateMachineDsl
class StateHostHandleBuilder<E : P, P : Event, S : State> : HandleBuilder<E, P, S>() {
    private val concatHandleBuilder = AsyncConcatHandleBuilder<E, P, S>()

    override fun build(): suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit {
        return concatHandleBuilder.build()
    }

    fun anyState(block: suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit) {
        concatHandleBuilder.add(block)
    }

    fun filteredState(
        predicate: (S) -> Boolean,
        block: suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit
    ) {
        concatHandleBuilder.add { updateSource -> if (predicate(updateSource.before)) block(updateSource) }
    }

    @NoInlineRequired
    fun <T : S> state(
        clazz: KClass<T>,
        block: suspend StateMachineHandleScope<P>.(UpdateSource<E, T>) -> Unit
    ) {
        filteredState(
            predicate = { before -> clazz.isInstance(before) },
            block = { (event, before) -> block(UpdateSource(event, clazz.cast(before))) }
        )
    }

    inline fun <reified T : S> state(
        noinline block: suspend StateMachineHandleScope<P>.(UpdateSource<E, T>) -> Unit
    ) {
        state(T::class, block)
    }

    @NoInlineRequired
    fun <T : S> stateNot(
        clazz: KClass<T>,
        block: suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit
    ) {
        filteredState(predicate = { before -> clazz.isInstance(before).not() }, block)
    }

    inline fun <reified T : S> stateNot(
        noinline block: suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit
    ) {
        stateNot(T::class, block)
    }
}