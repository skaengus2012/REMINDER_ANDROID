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
class ConcatHandleBuilder<E : P, P : Event, S : State> : HandleBuilder<E, P, S>() {
    private val handles: MutableList<suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit> = mutableListOf()

    fun add(handle: suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit) {
        handles += handle
    }

    override fun build(): suspend StateMachineHandleScope<P>.(UpdateSource<E, S>) -> Unit = { updateSource ->
        handles.forEach { handle -> handle(updateSource) }
    }
}