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

import com.nlab.reminder.core.state.Event
import com.nlab.reminder.core.state.EventProcessor
import com.nlab.reminder.core.state.State
import com.nlab.reminder.core.state.EventProcessorImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Doohyun
 */
internal class StateMachineEventProcessor<E : Event, S : State>(
    scope: CoroutineScope,
    state: MutableStateFlow<S>,
    stateMachineBuilder: StateMachineBuilder<E, S>,
) : EventProcessor<E> {
    private val stateReduceInvoker = StateReduceInvoker(state, stateMachineBuilder.buildUpdateHandler())
    private val errorHandler = stateMachineBuilder.buildExceptionHandler()
    private val invokeSideEffect = stateMachineBuilder.buildSideEffectHandler()
    private val internalActionProcessor = EventProcessorImpl(
        scope = scope + CoroutineExceptionHandler { _, e -> errorHandler(e) },
        onEventReceived = this::onEventReceived
    )

    private fun onEventReceived(event: E) {
        invokeSideEffect(stateReduceInvoker.getSourceAndUpdate(event))
    }

    override fun send(event: E): Job = internalActionProcessor.send(event)
}