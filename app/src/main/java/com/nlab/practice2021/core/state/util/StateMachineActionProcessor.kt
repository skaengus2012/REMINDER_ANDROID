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
import com.nlab.practice2021.core.state.impl.DefaultActionProcessor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Doohyun
 */
internal class StateMachineActionProcessor<A : Action, S : State>(
    scope: CoroutineScope,
    state: MutableStateFlow<S>,
    stateMachineBuilder: StateMachineBuilder<A, S>,
) : ActionProcessor<A> {
    private val reduceState = StateReducer<S>()
    private val errorHandler = stateMachineBuilder.buildExceptionHandler()
    private val newStateWith = stateMachineBuilder.buildUpdateHandler()
    private val invokeSideEffect = stateMachineBuilder.buildSideEffectHandler()
    private val internalActionProcessor = DefaultActionProcessor<A>(
        scope = scope + CoroutineExceptionHandler { _, e -> errorHandler(e) },
        onActionReceived = createOnActionReceiver(state)
    )

    private fun createOnActionReceiver(state: MutableStateFlow<S>): (A) -> Unit = { action ->
        UpdateSource(action, state.value)
            .also { updateSource ->
                reduceState(state, curState = updateSource.oldState, newState = newStateWith(updateSource))
            }
            .also(invokeSideEffect)
    }

    override fun send(action: A): Job = internalActionProcessor.send(action)
}
