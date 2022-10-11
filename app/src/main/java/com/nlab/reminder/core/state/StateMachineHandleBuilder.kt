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

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@StateMachineDsl
class StateMachineHandleBuilder<E : Event, S : State> : HandleBuilder<E, E, S>() {
    private val concatHandleBuilder: ConcatHandleBuilder<E, E, S> = ConcatHandleBuilder()

    override fun build(): suspend StateMachineHandleScope<E>.(UpdateSource<E, S>) -> Unit {
        return concatHandleBuilder.build()
    }

    @StateMachineStyleDsl
    fun anyEvent(block: StateHostHandleBuilder<E, E, S>.() -> Unit) {
        StateHostHandleBuilder<E, E, S>()
            .apply(block)
            .build()
            .also(concatHandleBuilder::add)
    }

    @StateMachineStyleDsl
    fun filteredEvent(predicate: (E) -> Boolean, block: StateHostHandleBuilder<E, E, S>.() -> Unit) {
        val handle = StateHostHandleBuilder<E, E, S>()
            .apply(block)
            .build()
        concatHandleBuilder.add { updateSource ->
            if (predicate(updateSource.event)) handle(updateSource)
        }
    }

    @StateMachineStyleDsl
    fun <T : E> event(eventClazz: KClass<T>, block: StateHostHandleBuilder<T, E, S>.() -> Unit) {
        val handle = StateHostHandleBuilder<T, E, S>()
            .apply(block)
            .build()
        concatHandleBuilder.add { (event, before) ->
            if (eventClazz.isInstance(event)) handle(UpdateSource(eventClazz.cast(event), before))
        }
    }

    @StateMachineStyleDsl
    inline fun <reified T : E> event(noinline block: StateHostHandleBuilder<T, E, S>.() -> Unit) {
        event(T::class, block)
    }

    @StateMachineStyleDsl
    fun <T : E> eventNot(eventClazz: KClass<T>, block: StateHostHandleBuilder<E, E, S>.() -> Unit) {
        filteredEvent(predicate = { event -> eventClazz.isInstance(event).not() }, block)
    }

    @StateMachineStyleDsl
    inline fun <reified T : E> eventNot(noinline block: StateHostHandleBuilder<E, E, S>.() -> Unit) {
        eventNot(T::class, block)
    }

    @StateMachineStyleDsl
    fun anyState(block: EventHostHandleBuilder<E, S>.() -> Unit) {
        EventHostHandleBuilder<E, S>()
            .apply(block)
            .build()
            .also(concatHandleBuilder::add)
    }

    @StateMachineStyleDsl
    fun filteredState(predicate: (S) -> Boolean, block: EventHostHandleBuilder<E, S>.() -> Unit) {
        val handle = EventHostHandleBuilder<E, S>()
            .apply(block)
            .build()
        concatHandleBuilder.add { updateSource ->
            if (predicate(updateSource.before)) handle(updateSource)
        }
    }

    @StateMachineStyleDsl
    fun <T : S> state(stateClazz: KClass<T>, block: EventHostHandleBuilder<E, T>.() -> Unit) {
        val handle = EventHostHandleBuilder<E, T>()
            .apply(block)
            .build()
        concatHandleBuilder.add { (event, before) ->
            if (stateClazz.isInstance(before)) handle(UpdateSource(event, stateClazz.cast(before)))
        }
    }

    @StateMachineStyleDsl
    inline fun <reified T : S> state(noinline block: EventHostHandleBuilder<E, T>.() -> Unit) {
        state(T::class, block)
    }

    @StateMachineStyleDsl
    fun <T : S> stateNot(stateClazz: KClass<T>, block: EventHostHandleBuilder<E, S>.() -> Unit) {
        filteredState(predicate = { state -> stateClazz.isInstance(state).not() }, block)
    }

    @StateMachineStyleDsl
    inline fun <reified T : S> stateNot(noinline block: EventHostHandleBuilder<E, S>.() -> Unit) {
        stateNot(T::class, block)
    }
}