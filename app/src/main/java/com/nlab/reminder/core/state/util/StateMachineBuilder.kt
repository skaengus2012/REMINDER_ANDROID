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

import com.nlab.reminder.core.state.Action
import com.nlab.reminder.core.state.State
import com.nlab.reminder.core.state.StateMachineConfig

/**
 * @author Doohyun
 */
class StateMachineBuilder<A : Action, S : State>(
    defaultUpdateErrorHandler: (Throwable) -> Unit = StateMachineConfig.defaultExceptionHandler
) {
    private var onUpdate: (UpdateSource<A, S>) -> S = ImmutableListener()
    private val onErrors: MutableList<(Throwable) -> Unit> = arrayListOf(defaultUpdateErrorHandler)
    private val sideEffects: MutableList<(UpdateSource<A, S>) -> Unit> = arrayListOf()

    internal fun buildExceptionHandler(): (Throwable) -> Unit = { error -> onErrors.forEach { it(error) } }
    internal fun buildUpdateHandler(): (UpdateSource<A, S>) -> S = { updateSource -> onUpdate(updateSource) }
    internal fun buildSideEffectHandler(): (UpdateSource<A, S>) -> Unit = { updateSource ->
        sideEffects.forEach { it(updateSource) }
    }

    fun updateTo(block: (UpdateSource<A, S>) -> S) {
        onUpdate = block
    }

    fun onError(block: (Throwable) -> Unit) {
        onErrors += block
    }

    fun sideEffect(block: (UpdateSource<A, S>) -> Unit) {
        sideEffects += block
    }

    inline fun <reified T : A> sideEffectBy(noinline block: (UpdateSource<T, S>) -> Unit) {
        sideEffectBy(T::class.java, block)
    }

    // Jacoco could not measure coverage for functions that were directly processed as inline.
    // So I created a wrapping function
    fun <T : A> sideEffectBy(actionClazz: Class<T>, block: (UpdateSource<T, S>) -> Unit) {
        sideEffect { (action, oldState) ->
            if (actionClazz.isInstance(action)) {
                block(UpdateSource(actionClazz.cast(action)!!, oldState))
            }
        }
    }

    inline fun <reified T : A, reified U : S> sideEffectWhen(noinline block: (UpdateSource<T, U>) -> Unit) {
        sideEffectWhen(T::class.java, U::class.java, block)
    }

    fun <T : A, U : S> sideEffectWhen(
        actionClazz: Class<T>,
        stateClazz: Class<U>,
        block: (UpdateSource<T, U>) -> Unit
    ) {
        sideEffect { (action, oldState) ->
            if (actionClazz.isInstance(action) && stateClazz.isInstance(oldState)) {
                block(UpdateSource(actionClazz.cast(action)!!, stateClazz.cast(oldState)!!))
            }
        }
    }

    private class ImmutableListener<A : Action, S : State> : (UpdateSource<A, S>) -> S {
        override fun invoke(updateSource: UpdateSource<A, S>): S = updateSource.oldState
    }
}
