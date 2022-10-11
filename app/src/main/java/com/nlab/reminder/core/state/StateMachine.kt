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
@StateMachineDsl
class StateMachine<E : Event, S : State> {
    private val reduceBuilder: StateMachineReduceBuilder<E, S> = StateMachineReduceBuilder()
    private val handleBuilder: StateMachineHandleBuilder<E, S> = StateMachineHandleBuilder()
    private val onExceptionHandlers: MutableList<StateMachineScope.(Throwable) -> Unit> = mutableListOf()

    internal fun buildReduce(): StateMachineScope.(UpdateSource<E, S>) -> S = reduceBuilder.build()
    internal fun buildHandle(): suspend (StateMachineHandleScope<E>, UpdateSource<E, S>) -> Unit = handleBuilder.build()
    internal fun buildExceptionHandler(): StateMachineScope.(Throwable) -> Unit = { throwable ->
        this@StateMachine.onExceptionHandlers.forEach { it(throwable) }
    }

    @StateMachineStyleDsl
    fun reduce(block: (StateMachineReduceBuilder<E, S>).() -> Unit) {
        reduceBuilder.apply(block)
    }

    @StateMachineStyleDsl
    fun handle(block: (StateMachineHandleBuilder<E, S>).() -> Unit) {
        handleBuilder.apply(block)
    }

    @StateMachineStyleDsl
    fun catch(block: (StateMachineScope).(Throwable) -> Unit) {
        onExceptionHandlers += { StateMachineScope.block(it) }
    }
}