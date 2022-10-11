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

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.plus

/**
 * @author thalys
 */
internal class StateMachineEventProcessor<E : Event, S : State>(
    scope: CoroutineScope,
    private val state: MutableStateFlow<S>,
    stateMachine: StateMachine<E, S>
) : EventProcessor<E> {
    private val scope = StateMachineHandleScope(state.subscriptionCount, eventProcessor = this)
    private val reduce = stateMachine.buildReduce()
    private val handle = stateMachine.buildHandle()
    private val exceptionHandler = stateMachine.buildExceptionHandler()
    private val internalActionProcessor = EventProcessorImpl<E>(
        scope = scope + CoroutineExceptionHandler { _, e -> StateMachineScope.exceptionHandler(e) },
        onEventReceived = { event ->
            handleSource(getAndUpdate(event))
        }
    )

    private fun getAndUpdate(event: E): UpdateSource<E, S> {
        return UpdateSource(
            event,
            before = state.getAndUpdate { old -> StateMachineScope.reduce(UpdateSource(event, old)) }
        )
    }

    private suspend fun handleSource(updateSource: UpdateSource<E, S>) {
        handle(scope, updateSource)
    }

    override fun send(event: E): Job = internalActionProcessor.send(event)
}