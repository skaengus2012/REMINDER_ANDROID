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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify


/**
 * @author Thalys
 */
class ReduceFactoriesKtTest {
    @Test
    fun `When create reduce without transition, effect, Then reduce has no transition and effect`() {
        val reduce = TestReduce()
        assert(reduce.transition == null)
        assert(reduce.effect == null)
    }

    @Test
    fun `When create empty transition, Then reduce has no transition and effect`() {
        val reduce = EmptyReduce<TestAction, TestState>()
        assert(reduce.transition == null)
        assert(reduce.effect == null)
    }

    @Test
    fun `Given transition, effect, When create reduce, Then reduce has transition and effect`() {
        val transition = TestTransitionNode { _, current -> current }
        val effect = TestEffectSuspendNode { _, _, _ ->  }
        val reduce = TestReduce(transition, effect)

        assertThat(reduce.transition, sameInstance(transition))
        assertThat(reduce.effect, sameInstance(effect))
    }

    @Test
    fun `Given multiple transitions, When transition from combineReduce, Then return expected value`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val wrongState = TestState.State3
        val notMatchedTransition = TestTransitionNode { action, current ->
            if (action != inputAction && current != inputState) wrongState
            else current
        }
        val matchedTransition = TestTransitionNode { action, current ->
            if (action == inputAction && current == inputState) expectedState
            else current
        }
        val wrongMatchedTransition = TestTransitionNode { action, current ->
            if (action == inputAction && current == inputState) wrongState
            else current
        }
        val reduce = combineReduce(
            TestReduce(),
            TestReduce(transition = notMatchedTransition),
            TestReduce(transition = matchedTransition),
            TestReduce(transition = wrongMatchedTransition),
        )
        val actualState = reduce.transition!!.transitionTo(inputAction, inputState, AccumulatorPool())
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given multiple effects, When launch effect from combineReduce, Then all effect invoked`() = runTest {
        val runner: () -> Unit = mock()
        val firstEffect = TestEffectSuspendNode { _, _, _ -> runner() }
        val secondEffect = TestEffectSuspendNode { _, _, _ -> runner() }

        val reduce = combineReduce(
            TestReduce(effect = firstEffect),
            TestReduce(effect = secondEffect),
        )
        reduce.effect!!.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            AccumulatorPool(),
            coroutineScope = this
        )
        advanceUntilIdle()
        verify(runner, times(2)).invoke()
    }
}