/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.statekit.reduce.dsl


/**
 * @author Doohyun
 */
internal class DslTransitionBuilder<R : Any, A : Any, S : R> {
    private val transitions = mutableListOf<DslTransition<R, A, S>>()

    fun add(block: DslTransition.NodeTransition<R, A, S>) {
        transitions.add(block)
    }

    fun build(): DslTransition<R, A, S>? = when (transitions.size) {
        0 -> null
        1 -> transitions.first()
        else -> DslTransition.CompositeTransition(
            head = transitions.first(),
            tail = transitions.subList(1, transitions.size)
        )
    }
}