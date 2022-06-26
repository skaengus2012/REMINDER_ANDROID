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

package com.nlab.practice2021.core.state.util

import com.nlab.practice2021.core.state.Action
import com.nlab.practice2021.core.state.ActionProcessor
import com.nlab.practice2021.core.state.State
import com.nlab.practice2021.core.state.StateMachineConfig

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

    fun <T : A> withSideEffect(actionClazz: Class<T>, block: (UpdateSource<T, S>) -> Unit) {
        sideEffects.add { updateSource ->
            val action = updateSource.action
            if (actionClazz.isInstance(action)) {
                block(UpdateSource(actionClazz.cast(action)!!, updateSource.oldState))
            }
        }
    }

    inline fun <reified T : A> withSideEffect(noinline block: (UpdateSource<T, S>) -> Unit) {
        withSideEffect(T::class.java, block)
    }

    private class ImmutableListener<A : Action, S : State> : (UpdateSource<A, S>) -> S {
        override fun invoke(updateSource: UpdateSource<A, S>): S = updateSource.oldState
    }
}
