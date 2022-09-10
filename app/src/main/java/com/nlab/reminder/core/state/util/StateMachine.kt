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
import com.nlab.reminder.core.state.StateControllerImpl
import com.nlab.reminder.core.state.StateMachineBuilder
import com.nlab.reminder.core.state.StateMachineEventProcessor
import com.nlab.reminder.core.util.test.annotation.Generated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus

/**
 * @author thalys
 */
typealias StateMachine<E, S> = StateMachineBuilder<E, S>

@Generated
@Suppress("FunctionName")
inline fun <E : Event, S : State> StateMachine(
    block: StateMachine<E, S>.() -> Unit
): StateMachine<E, S> = StateMachine<E, S>().apply(block)

fun <E : Event, S : State> StateMachine<E, S>.controlIn(
    scope: CoroutineScope,
    initState: S,
    config: StateControllerConfig = StateControllerConfig()
): StateController<E, S> {
    val state = MutableStateFlow(initState)
    return StateControllerImpl(
        PluginStateMachineEventProcessor(stateMachine = this, scope, state, config),
        state.asStateFlow()
    )
}

fun <E : Event, S : State> StateMachine<E, S>.controlIn(
    scope: CoroutineScope,
    initState: S,
    fetchEvent: E,
    config: StateControllerConfig = StateControllerConfig()
): StateController<E, S> {
    val state = MutableStateFlow(initState)
    val eventProcessor: EventProcessor<E> = PluginStateMachineEventProcessor(stateMachine = this, scope, state, config)
    return StateControllerImpl(
        eventProcessor,
        state.asStateFlow()
            .onStart(onStartToFetchConverter(eventProcessor, fetchEvent))
            .stateIn(scope, SharingStarted.Lazily, initState)
    )
}

@Suppress("FunctionName")
private fun <E : Event, S : State> PluginStateMachineEventProcessor(
    stateMachine: StateMachine<E, S>,
    scope: CoroutineScope,
    stateFlow: MutableStateFlow<S>,
    config: StateControllerConfig
): EventProcessor<E> = StateMachineEventProcessor(
    state = stateFlow,
    scope = with(scope) {
        val defaultDispatcher: CoroutineDispatcher? = StateMachinePlugin.defaultDispatcher
        if (defaultDispatcher != null && config.isPluginDispatcherEnabled) {
            this + defaultDispatcher
        } else {
            this
        }
    },
    stateMachineBuilder = stateMachine.apply {
        if (config.isPluginErrorHandlerEnabled) {
            catch { StateMachinePlugin.defaultExceptionHandler?.invoke(it) }
        }
    }
)

// Jacoco could not measure coverage in onStart suspend function..
private fun <E : Event, S : State> onStartToFetchConverter(
    eventProcessor: EventProcessor<E>,
    fetchEvent: E
): FlowCollector<S>.() -> Unit = { eventProcessor.send(fetchEvent) }