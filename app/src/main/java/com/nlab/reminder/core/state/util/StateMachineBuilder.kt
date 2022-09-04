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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.*

/**
 * @author Doohyun
 */
class StateMachineBuilder<E : Event, S : State>(
    defaultUpdateErrorHandler: (Throwable) -> Unit = {}
) {
    private var onUpdate: (UpdateSource<E, S>) -> S = ImmutableListener()
    private val onErrors: MutableList<(Throwable) -> Unit> = arrayListOf(defaultUpdateErrorHandler)
    private val sideEffects: MutableList<suspend (EventProcessor<E>).(UpdateSource<E, S>) -> Unit> = arrayListOf()

    internal fun buildExceptionHandler(): (Throwable) -> Unit = { error -> onErrors.forEach { it(error) } }
    internal fun buildUpdateHandler(): (UpdateSource<E, S>) -> S = { updateSource -> onUpdate(updateSource) }
    internal fun buildSideEffectHandler(): suspend (EventProcessor<E>).(UpdateSource<E, S>) -> Unit = { updateSource ->
        sideEffects.forEach { it(updateSource) }
    }

    fun updateTo(block: (UpdateSource<E, S>) -> S) {
        onUpdate = block
    }

    fun onError(block: (Throwable) -> Unit) {
        onErrors += block
    }

    fun sideEffect(block: suspend (EventProcessor<E>).(UpdateSource<E, S>) -> Unit) {
        sideEffects += block
    }

    inline fun <reified T : E> sideEffectBy(noinline block: suspend (EventProcessor<E>).(UpdateSource<T, S>) -> Unit) {
        sideEffectBy(T::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E> sideEffectBy(
        eventClazz: Class<T>,
        block: suspend (EventProcessor<E>).(UpdateSource<T, S>) -> Unit
    ) {
        sideEffect { (event, oldState) ->
            if (eventClazz.isInstance(event)) {
                block(UpdateSource(eventClazz.cast(event)!!, oldState))
            }
        }
    }

    inline fun <reified U : S> sideEffectWhen(noinline block: suspend (EventProcessor<E>).(UpdateSource<E, U>) -> Unit) {
        sideEffectWhen(U::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <U : S> sideEffectWhen(
        stateClazz: Class<U>,
        block: suspend (EventProcessor<E>).(UpdateSource<E, U>) -> Unit
    ) {
        sideEffect { (event, oldState) ->
            if (stateClazz.isInstance(oldState)) {
                block(UpdateSource(event, stateClazz.cast(oldState)!!))
            }
        }
    }

    inline fun <reified T : E, reified U : S> sideEffectOn(
        noinline block: suspend (EventProcessor<E>).(UpdateSource<T, U>) -> Unit
    ) {
        sideEffectOn(T::class.java, U::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E, U : S> sideEffectOn(
        eventClazz: Class<T>,
        stateClazz: Class<U>,
        block: suspend (EventProcessor<E>).(UpdateSource<T, U>) -> Unit
    ) {
        sideEffect { (action, oldState) ->
            if (eventClazz.isInstance(action) && stateClazz.isInstance(oldState)) {
                block(UpdateSource(eventClazz.cast(action)!!, stateClazz.cast(oldState)!!))
            }
        }
    }

    private class ImmutableListener<E : Event, S : State> : (UpdateSource<E, S>) -> S {
        override fun invoke(updateSource: UpdateSource<E, S>): S = updateSource.before
    }
}
