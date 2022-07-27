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
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.StateMachineImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun <A : Action, S : State> StateMachine(
    scope: CoroutineScope,
    initState: S,
    builderBlock: (StateMachineBuilder<A, S>).() -> Unit
): StateMachine<A, S> {
    val state = MutableStateFlow(initState)
    return StateMachineImpl(
        StateMachineActionProcessor(scope, state, StateMachineBuilder<A, S>().apply(builderBlock)),
        state.asStateFlow()
    )
}