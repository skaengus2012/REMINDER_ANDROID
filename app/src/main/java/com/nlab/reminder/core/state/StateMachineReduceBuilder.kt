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
class StateMachineReduceBuilder<E : Event, S : State> : ReduceBuilder<E, S, S>() {
    private val defaultReduce: StateMachineScope.(UpdateSource<E, S>) -> S = createDefaultReduce()
    private val concatReduceBuilder: ConcatReduceBuilder<E, S, S> = ConcatReduceBuilder(defaultReduce)


    override fun build(): StateMachineScope.(UpdateSource<E, S>) -> S {
        return concatReduceBuilder.build()
    }

    private fun <T : E> createStateHostReduceBuilder(): StateHostReduceBuilder<T, S> {
        return StateHostReduceBuilder(createDefaultReduce())
    }

    private fun <T : S> createEventHostReduceBuilder(): EventHostReduceBuilder<E, T, S> {
        return EventHostReduceBuilder(createDefaultReduce())
    }

    fun anyEvent(block: StateHostReduceBuilder<E, S>.() -> Unit) {
        createStateHostReduceBuilder<E>()
            .apply(block)
            .build()
            .also(concatReduceBuilder::add)
    }

    fun filteredEvent(
        predicate: (E) -> Boolean,
        block: StateHostReduceBuilder<E, S>.() -> Unit
    ) {
        val reduce = createStateHostReduceBuilder<E>()
            .apply(block)
            .build()
        concatReduceBuilder.add { updateSource ->
            if (predicate(updateSource.event)) reduce(updateSource)
            else this@StateMachineReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    fun <T : E> event(eventClazz: KClass<T>, block: StateHostReduceBuilder<T, S>.() -> Unit) {
        val reduce = createStateHostReduceBuilder<T>()
            .apply(block)
            .build()
        concatReduceBuilder.add { updateSource ->
            if (eventClazz.isInstance(updateSource.event))
                reduce(UpdateSource(eventClazz.cast(updateSource.event), updateSource.before))
            else this@StateMachineReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    inline fun <reified T : E> event(noinline block: StateHostReduceBuilder<T, S>.() -> Unit) {
        event(T::class, block)
    }

    fun <T : E> eventNot(eventClazz: KClass<T>, block: StateHostReduceBuilder<E, S>.() -> Unit) {
        filteredEvent(predicate = { event -> eventClazz.isInstance(event).not() }, block)
    }

    inline fun <reified T : E> eventNot(noinline block: StateHostReduceBuilder<E, S>.() -> Unit) {
        eventNot(T::class, block)
    }

    fun anyState(block: EventHostReduceBuilder<E, S, S>.() -> Unit) {
        createEventHostReduceBuilder<S>()
            .apply(block)
            .build()
            .also(concatReduceBuilder::add)
    }

    fun filteredState(predicate: (S) -> Boolean, block: EventHostReduceBuilder<E, S, S>.() -> Unit) {
        val reduce = createEventHostReduceBuilder<S>()
            .apply(block)
            .build()
        concatReduceBuilder.add { updateSource ->
            if (predicate(updateSource.before)) reduce(updateSource)
            else this@StateMachineReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    fun <T : S> state(stateClazz: KClass<T>, block: EventHostReduceBuilder<E, T, S>.() -> Unit) {
        val reduce = createEventHostReduceBuilder<T>()
            .apply(block)
            .build()
        concatReduceBuilder.add { updateSource ->
            if (stateClazz.isInstance(updateSource.before))
                reduce(UpdateSource(updateSource.event, stateClazz.cast(updateSource.before)))
            else this@StateMachineReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    inline fun <reified T : S> state(noinline block: EventHostReduceBuilder<E, T, S>.() -> Unit) {
        state(T::class, block)
    }

    fun <T : S> stateNot(stateClazz: KClass<T>, block: EventHostReduceBuilder<E, S, S>.() -> Unit) {
        filteredState(predicate = { state -> stateClazz.isInstance(state).not() }, block)
    }

    inline fun <reified T : S> stateNot(noinline block: EventHostReduceBuilder<E, S, S>.() -> Unit) {
        stateNot(T::class, block)
    }

    companion object {
        private fun <E : Event, S : R, R : State> createDefaultReduce():
                StateMachineScope.(UpdateSource<E, S>) -> R = { it.before }
    }
}