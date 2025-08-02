/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.statekit.dsl.reduce

/**
 * @author Thalys
 */
sealed interface NodeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS> {
    fun transition(block: DslTransitionScope<A, S>.() -> RS)
    fun effect(block: DslEffectScope<A, S>.() -> Unit)
    fun suspendEffect(block: suspend DslSuspendEffectScope<RA, A, S>.() -> Unit)
}

internal class InternalNodeReduceBuilder<RA : Any, RS : Any, A : Any, S : RS>(
    private val reduceBuilderDelegate: ReduceBuilderDelegate
) : NodeReduceBuilder<RA, RS, A, S> {
    override fun transition(block: DslTransitionScope<A, S>.() -> RS) {
        reduceBuilderDelegate.addTransitionNode(block)
    }

    override fun effect(block: DslEffectScope<A, S>.() -> Unit) {
        reduceBuilderDelegate.addEffectNode(block)
    }

    override fun suspendEffect(block: suspend DslSuspendEffectScope<RA, A, S>.() -> Unit) {
        reduceBuilderDelegate.addSuspendEffectNode(block)
    }
}