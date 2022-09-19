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
class EventHostHandleBuilder<E : Event, S : State> : HandleBuilder<E, E, S>() {
    private val concatHandleBuilder: ConcatHandleBuilder<E, E, S> = ConcatHandleBuilder()

    override fun build(): suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit {
        return concatHandleBuilder.build()
    }

    fun anyEvent(block: suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit) {
        concatHandleBuilder.add(block)
    }

    fun filteredEvent(
        predicate: (E) -> Boolean,
        block: suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit
    ) {
        concatHandleBuilder.add { updateSource ->
            if (predicate(updateSource.event)) block(updateSource)
        }
    }

    @NoInlineRequired
    fun <T : E> event(
        clazz: KClass<T>,
        block: suspend StateMachineHandleScope<E>.(UpdateSource<T, S>) -> Unit
    ) {
        filteredEvent(
            predicate = { event -> clazz.isInstance(event) },
            block = { (event, before) -> block(UpdateSource(clazz.cast(event), before)) }
        )
    }

    inline fun <reified T : E> event(
        noinline block: suspend StateMachineHandleScope<E>.(UpdateSource<T, S>) -> Unit
    ) {
        event(T::class, block)
    }

    @NoInlineRequired
    fun <T : E> eventNot(
        clazz: KClass<T>,
        block: suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit
    ) {
        filteredEvent(predicate = { event -> clazz.isInstance(event).not() }, block)
    }

    inline fun <reified T : E> eventNot(
        noinline block: suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit
    ) {
        eventNot(T::class, block)
    }
}