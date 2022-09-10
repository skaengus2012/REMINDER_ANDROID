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

/**
 * @author thalys
 */
@StateMachineBuildMarker
class StateMachineBuilder<E : Event, S : State> {
    private val stateConverters: MutableList<(UpdateSource<E, S>) -> S> = mutableListOf()
    private val onExceptionHandlers: MutableList<(Throwable) -> Unit> = mutableListOf()
    private val onEventHandlers: MutableList<suspend (EventProcessor<E>, UpdateSource<E, S>) -> Unit> = mutableListOf()

    internal fun buildUpdateHandler(): (UpdateSource<E, S>) -> S = { inputSource ->
        stateConverters
            .asSequence()
            .map { updateTo -> updateTo(inputSource) }
            .find { newState -> newState != inputSource.before }
            ?: inputSource.before
    }

    internal fun buildExceptionHandler(): (Throwable) -> Unit = { throwable ->
        onExceptionHandlers.forEach { handler -> handler(throwable) }
    }

    internal fun buildEventHandler(): suspend (EventProcessor<E>).(UpdateSource<E, S>) -> Unit = { updateSource ->
        onEventHandlers.forEach { handler -> handler.invoke(this, updateSource) }
    }

    fun update(block: (StateMachineBuildScope).(UpdateSource<E, S>) -> S) {
        stateConverters += { updateSource -> block(StateMachineBuildScope, updateSource) }
    }

    fun catch(block: (StateMachineBuildScope).(Throwable) -> Unit) {
        onExceptionHandlers += { throwable -> block(StateMachineBuildScope, throwable) }
    }

    fun handle(
        filter: (UpdateSource<E, S>) -> Boolean = { true },
        block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<E, S>) -> Unit
    ) {
        onEventHandlers += { eventProcessor, updateSource ->
            if (filter(updateSource)) {
                block.invoke(StateMachineBuildSideEffect(eventProcessor), updateSource)
            }
        }
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E> handleBy(
        eventClazz: Class<T>,
        block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<T, S>) -> Unit
    ) {
        handle(
            filter = { updateSource -> eventClazz.isInstance(updateSource.event) },
            block = { updateSource -> block(UpdateSource(eventClazz.cast(updateSource.event)!!, updateSource.before)) }
        )
    }

    inline fun <reified T : E> handleBy(
        noinline block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<T, S>) -> Unit
    ) {
        handleBy(T::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <U : S> handleWhen(
        stateClazz: Class<U>,
        block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<E, U>) -> Unit
    ) {
        handle(
            filter = { updateSource -> stateClazz.isInstance(updateSource.before) },
            block = { updateSource -> block(UpdateSource(updateSource.event, stateClazz.cast(updateSource.before)!!)) }
        )
    }

    inline fun <reified U : S> handleWhen(
        noinline block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<E, U>) -> Unit
    ) {
        handleWhen(U::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : E, U : S> handleOn(
        eventClazz: Class<T>,
        stateClazz: Class<U>,
        block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<T, U>) -> Unit
    ) {
        handle(
            filter = { updateSource ->
                eventClazz.isInstance(updateSource.event) && stateClazz.isInstance(updateSource.before)
            },
            block = { updateSource ->
                block(
                    UpdateSource(
                        eventClazz.cast(updateSource.event)!!,
                        stateClazz.cast(updateSource.before)!!
                    )
                )
            }
        )
    }

    inline fun <reified T : E, reified U : S> handleOn(
        noinline block: suspend (StateMachineBuildSideEffect<E>).(UpdateSource<T, U>) -> Unit
    ) {
        handleOn(T::class.java, U::class.java, block)
    }
}