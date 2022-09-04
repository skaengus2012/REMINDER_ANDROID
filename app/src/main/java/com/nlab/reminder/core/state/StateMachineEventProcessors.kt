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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus

/**
 * @author thalys
 */
@Suppress("FunctionName")
fun <E : Event, S : State> StateMachineEventProcessor(
    scope: CoroutineScope,
    state: MutableStateFlow<S>,
    stateMachineBuilder: StateMachineBuilder<E, S>,
): StateMachineEventProcessor<E, S> = StateMachineEventProcessor(
    scope = withPlugin(scope),
    stateReduce = StateReduce(state, stateMachineBuilder.buildUpdateHandler()),
    exceptionHandlers = withPlugin(stateMachineBuilder.buildExceptionHandler()),
    eventHandler = stateMachineBuilder.buildEventHandler(),
)

private fun withPlugin(
    scope: CoroutineScope
): CoroutineScope = when (val defaultDispatcher = StateMachinePlugin.defaultDispatcher) {
    null -> scope
    else -> { scope + defaultDispatcher }
}

private fun withPlugin(
    exceptionHandler: (Throwable) -> Unit
): List<(Throwable) -> Unit> = when (val defaultHandler = StateMachinePlugin.defaultExceptionHandler) {
    null -> listOf(exceptionHandler)
    else -> listOf(defaultHandler, exceptionHandler)
}