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

package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class TransitionKtTest {
    @Test
    fun `Given two changeable composite transitions, When transitionTo, Then return first changed state`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val wrongState = TestState.State3
        val compositeTransition = TestTransitionComposite(
            head = TestTransitionNode { _, _ -> expectedState },
            tails = listOf(TestTransitionLifecycleNode { _, _, _ -> wrongState })
        )

       val actualState = compositeTransition.transitionTo(
            action = inputAction,
            current = inputState,
            context = TransitionContext(NodeStackPool())
        )
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given identity, changeable composite transitions, When transitionTo, Then return last changed state`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val compositeTransition = TestTransitionComposite(
            head = TestTransitionNode { _, _ -> inputState },
            tails = listOf(TestTransitionLifecycleNode { _, _, _ -> expectedState })
        )

        val actualState = compositeTransition.transitionTo(
            action = inputAction,
            current = inputState,
            context = TransitionContext(NodeStackPool())
        )
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given two non changeable composite transitions, When transitionTo, Then return input state`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val compositeTransition = TestTransitionComposite(
            head = TestTransitionNode { _, _ -> inputState },
            tails = listOf(TestTransitionLifecycleNode { _, _, _ -> inputState })
        )

        val actualState = compositeTransition.transitionTo(
            action = inputAction,
            current = inputState,
            context = TransitionContext(NodeStackPool())
        )
        assertThat(actualState, equalTo(inputState))
    }
}