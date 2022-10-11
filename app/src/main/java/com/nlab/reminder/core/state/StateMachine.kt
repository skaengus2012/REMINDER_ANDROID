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
class StateMachine<E : Event, S : State> {
    private val reduceBuilder: StateMachineReduceBuilder<E, S> = StateMachineReduceBuilder()
    private val handleBuilder: StateMachineHandleBuilder<E, S> = StateMachineHandleBuilder()
    private val onExceptionHandlers: MutableList<StateMachineScope.(Throwable) -> Unit> = mutableListOf()
    private val onEventHandlers: MutableList<suspend (EventProcessor<E>, UpdateSource<E, S>) -> Unit> = mutableListOf()

    internal fun buildReduce(): StateMachineScope.(UpdateSource<E, S>) -> S = reduceBuilder.build()
    internal fun buildHandle(): suspend (StateMachineHandleScope<E>, UpdateSource<E, S>) -> Unit = handleBuilder.build()
    internal fun buildExceptionHandler(): StateMachineScope.(Throwable) -> Unit = { throwable ->
        this@StateMachine.onExceptionHandlers.forEach { it(throwable) }
    }

    @StateMachineStyleDsl
    fun reduce(block: (StateMachineReduceBuilder<E, S>).() -> Unit) {
        reduceBuilder.apply(block)
    }

    @StateMachineStyleDsl
    fun handled(block: (StateMachineHandleBuilder<E, S>).() -> Unit) {
        handleBuilder.apply(block)
    }

    @StateMachineStyleDsl
    fun catch(block: (StateMachineScope).(Throwable) -> Unit) {
        onExceptionHandlers += { StateMachineScope.block(it) }
    }

    fun update(block: (StateMachineScope).(UpdateSource<E, S>) -> S) {
    }

    fun handle(
        filter: (UpdateSource<E, S>) -> Boolean = { true },
        block: suspend (StateMachineHandleScope<E>).(UpdateSource<E, S>) -> Unit
    ) {
        onEventHandlers += { eventProcessor, updateSource ->
            if (filter(updateSource)) {
            //    block.invoke(StateMachineHandleScope(eventProcessor), updateSource)
            }
        }
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E> handleBy(
        eventClazz: KClass<T>,
        block: suspend (StateMachineHandleScope<E>).(UpdateSource<T, S>) -> Unit
    ) {
        handle(
            filter = { updateSource -> eventClazz.isInstance(updateSource.event) },
            block = { updateSource -> block(UpdateSource(eventClazz.cast(updateSource.event), updateSource.before)) }
        )
    }

    inline fun <reified T : E> handleBy(
        noinline block: suspend (StateMachineHandleScope<E>).(UpdateSource<T, S>) -> Unit
    ) {
        handleBy(T::class, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <U : S> handleWhen(
        stateClazz: KClass<U>,
        block: suspend (StateMachineHandleScope<E>).(UpdateSource<E, U>) -> Unit
    ) {
        handle(
            filter = { updateSource -> stateClazz.isInstance(updateSource.before) },
            block = { updateSource -> block(UpdateSource(updateSource.event, stateClazz.cast(updateSource.before))) }
        )
    }

    inline fun <reified U : S> handleWhen(
        noinline block: suspend (StateMachineHandleScope<E>).(UpdateSource<E, U>) -> Unit
    ) {
        handleWhen(U::class, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E, U : S> handleOn(
        eventClazz: KClass<T>,
        stateClazz: KClass<U>,
        block: suspend (StateMachineHandleScope<E>).(UpdateSource<T, U>) -> Unit
    ) {
        handle(
            filter = { updateSource ->
                eventClazz.isInstance(updateSource.event) && stateClazz.isInstance(updateSource.before)
            },
            block = { updateSource ->
                block(
                    UpdateSource(
                        eventClazz.cast(updateSource.event),
                        stateClazz.cast(updateSource.before)
                    )
                )
            }
        )
    }

    inline fun <reified T : E, reified U : S> handleOn(
        noinline block: suspend (StateMachineHandleScope<E>).(UpdateSource<T, U>) -> Unit
    ) {
        handleOn(T::class, U::class, block)
    }
}