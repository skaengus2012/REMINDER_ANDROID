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
class EventHostReduceBuilder<E : Event, S : R, R : State>(
    private val defaultReduce: StateMachineScope.(UpdateSource<E, S>) -> R
) : ReduceBuilder<E, S, R>() {
    private val concatReduceBuilder: ConcatReduceBuilder<E, S, R> = ConcatReduceBuilder(defaultReduce)

    override fun build(): StateMachineScope.(UpdateSource<E, S>) -> R {
        return concatReduceBuilder.build()
    }

    @StateMachineStyleDsl
    fun anyEvent(block: (StateMachineScope).(UpdateSource<E, S>) -> R) {
        concatReduceBuilder.add(block)
    }

    @StateMachineStyleDsl
    fun filteredEvent(
        predicate: (E) -> Boolean,
        block: (StateMachineScope).(UpdateSource<E, S>) -> R
    ) {
        anyEvent { updateSource ->
            if (predicate(updateSource.event)) block(updateSource)
            else this@EventHostReduceBuilder.defaultReduce(this, updateSource)
        }
    }

    @NoInlineRequired
    @StateMachineStyleDsl
    fun <T : E> event(
        clazz: KClass<T>,
        block: (StateMachineScope).(UpdateSource<T, S>) -> R
    ) {
        filteredEvent(
            predicate = { event -> clazz.isInstance(event) },
            block = { (event, before) -> block(UpdateSource(clazz.cast(event), before)) }
        )
    }

    @StateMachineStyleDsl
    inline fun <reified T : E> event(noinline block: (StateMachineScope).(UpdateSource<T, S>) -> R) {
        event(T::class, block)
    }

    @NoInlineRequired
    @StateMachineStyleDsl
    fun <T : E> eventNot(clazz: KClass<T>, block: (StateMachineScope).(UpdateSource<E, S>) -> R) {
        filteredEvent(predicate = { event -> clazz.isInstance(event).not() }, block)
    }

    @StateMachineStyleDsl
    inline fun <reified T : E> eventNot(noinline block: (StateMachineScope).(UpdateSource<E, S>) -> R) {
        eventNot(T::class, block)
    }
}