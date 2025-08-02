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

import com.nlab.statekit.dsl.TestAction
import com.nlab.statekit.dsl.TestState
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author Thalys
 */
class InternalNodeReduceBuilderTest {
    @Test
    fun `Given delegate, When invoke transition, Then transition block added to delegate`() {
        val delegate: ReduceBuilderDelegate = mockk(relaxed = true)
        val nodeReduceBuilder = InternalNodeReduceBuilder<TestAction, TestState, TestAction, TestState>(
            reduceBuilderDelegate = delegate
        )

        val block: (TestDslTransitionScope) -> TestState = mockk()
        nodeReduceBuilder.transition(block)

        verify(exactly = 1) {
            delegate.addTransitionNode(block)
        }
    }

    @Test
    fun `Given delegate, When invoke effect, Then effect block added to delegate`() {
        val delegate: ReduceBuilderDelegate = mockk(relaxed = true)
        val nodeReduceBuilder = InternalNodeReduceBuilder<TestAction, TestState, TestAction, TestState>(
            reduceBuilderDelegate = delegate
        )

        val block: (TestDslEffectScope) -> Unit = mockk()
        nodeReduceBuilder.effect(block)

        verify(exactly = 1) {
            delegate.addEffectNode(block)
        }
    }

    @Test
    fun `Given delegate, When invoke suspend effect, Then suspend effect block added to delegate`() {
        val delegate: ReduceBuilderDelegate = mockk(relaxed = true)
        val nodeReduceBuilder = InternalNodeReduceBuilder<TestAction, TestState, TestAction, TestState>(
            reduceBuilderDelegate = delegate
        )

        val block: suspend (TestDslSuspendEffectScope) -> Unit = mockk()
        nodeReduceBuilder.suspendEffect(block)

        verify(exactly = 1) {
            delegate.addSuspendEffectNode(block)
        }
    }
}