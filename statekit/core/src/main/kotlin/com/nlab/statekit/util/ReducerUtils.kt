/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.util

import com.nlab.statekit.reducer.dsl.DslReduceBuilder
import com.nlab.statekit.reducer.dsl.DslReducer
import com.nlab.statekit.Action
import com.nlab.statekit.Reducer
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.reducer.CompositeReduceBuilder
import com.nlab.statekit.reducer.DefaultReducer

/**
 * @author thalys
 */
fun <A : Action, S : State> buildReducer(block: (UpdateSource<A, S>) -> S): Reducer<A, S> {
    return DefaultReducer(block)
}

fun <A : Action, S : State> buildDslReducer(defineDSL: DslReduceBuilder<A, S>.() -> Unit): Reducer<A, S> {
    return DslReducer(defineDSL)
}

operator fun <A : Action, S : State> Reducer<A, S>.plus(reducer: Reducer<A, S>): Reducer<A, S> =
    buildReducer(
        block = CompositeReduceBuilder<A, S, S>()
            .apply { add(this@plus) }
            .apply { add(reducer) }
            .build()
    )