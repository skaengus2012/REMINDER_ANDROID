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

import com.nlab.reminder.core.util.test.annotation.NoInlineRequired
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author thalys
 */
@StateMachineDsl
class StateHostReduceBuilder<E : Event, S : State>(
    private val defaultReduce: StateMachineScope.(UpdateSource<E, S>) -> S
) : ReduceBuilder<E, S, S>() {
    private val concatComponentBuilder: ConcatReduceBuilder<E, S, S> = ConcatReduceBuilder(defaultReduce)

    override fun build(): StateMachineScope.(UpdateSource<E, S>) -> S {
        return concatComponentBuilder.build()
    }

    @StateMachineStyleDsl
    fun anyState(block: (StateMachineScope).(UpdateSource<E, S>) -> S) {
        concatComponentBuilder.add(block)
    }

    @StateMachineStyleDsl
    fun filteredState(
        predicate: (S) -> Boolean,
        block: (StateMachineScope).(UpdateSource<E, S>) -> S
    ) {
        anyState { updateSource ->
            if (predicate(updateSource.before)) block(updateSource)
            else this@StateHostReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    @NoInlineRequired
    @StateMachineStyleDsl
    fun <T : S> state(
        clazz: KClass<T>,
        block: (StateMachineScope).(UpdateSource<E, T>) -> S
    ) {
        filteredState(
            predicate = clazz::isInstance,
            block = { (event, before) -> block(UpdateSource(event, clazz.cast(before))) }
        )
    }

    @StateMachineStyleDsl
    inline fun <reified T : S> state(
        noinline block: (StateMachineScope).(UpdateSource<E, T>) -> S
    ) {
        state(T::class, block)
    }

    @NoInlineRequired
    @StateMachineStyleDsl
    fun <T : S> stateNot(
        clazz: KClass<T>,
        block: (StateMachineScope).(UpdateSource<E, S>) -> S
    ) {
        filteredState(predicate = { before -> clazz.isInstance(before).not() }, block)
    }

    @StateMachineStyleDsl
    inline fun <reified T : S> stateNot(noinline block: (StateMachineScope).(UpdateSource<E, S>) -> S) {
        stateNot(T::class, block)
    }
}