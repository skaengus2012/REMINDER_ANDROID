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

package com.nlab.statekit.reducer

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import java.util.*

/**
 * @author thalys
 */
internal class CompositeReduceBuilder<A : Action, B : S, S : State> {
    private val reduces: LinkedList<(UpdateSource<A, B>) -> S> = LinkedList()

    fun add(block: (UpdateSource<A, B>) -> S) {
        reduces.add(index = 0, block)
    }

    fun build(): (UpdateSource<A, B>) -> S = { updateSource ->
        reduces.asSequence()
            .map { it(updateSource) }
            .find { newState -> newState != updateSource.before }
            ?: updateSource.before
    }
}