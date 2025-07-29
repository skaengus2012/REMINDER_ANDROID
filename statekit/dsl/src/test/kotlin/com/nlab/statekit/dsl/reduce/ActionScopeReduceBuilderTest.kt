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

package com.nlab.statekit.dsl.reduce

import com.nlab.statekit.dsl.TestAction
import com.nlab.statekit.dsl.TestState
import com.nlab.statekit.test.reduce.expectedNextState
import com.nlab.statekit.test.reduce.expectedNotChanged
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Doohyun
 */
class ActionScopeReduceBuilderTest {
    @Test
    fun `Given state1, When register transition state1 to state2, Then return state2`() = runTest {
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestActionScopeReduceBuilder {
            transition<TestState.State1> { expectedState }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given state1, When register transition state2 to state3, Then state not changed`() = runTest {
        val inputState = TestState.State1
        val reduceBuilder = TestActionScopeReduceBuilder {
            transition<TestState.State2> { TestState.State3 }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given state1, When register effect on state1, Then runner invoked`() = runTest {
        val inputState = TestState.State1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestActionScopeReduceBuilder {
            effect<TestState.State1> { runner() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given state1, When register effect on state2, Then runner never invoked`() = runTest {
        val inputState = TestState.State1
        val runner: () -> Unit = mockk()
        val reduceBuilder = TestActionScopeReduceBuilder {
            effect<TestState.State2> { runner() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(inverse = true) {
            runner.invoke()
        }
    }

    @Test
    fun `Given state1, When register suspend effect on state1, Then runner invoked`() = runTest {
        val inputState = TestState.State1
        val runner: suspend () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestActionScopeReduceBuilder {
            suspendEffect<TestState.State1> { runner() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        coVerify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given matched predicate, When register effect in scope with predicate, Then runner invoked`() = runTest {
        val predicate: (TestUpdateSource) -> Boolean = { true }
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope(isMatch = predicate) {
                effect {
                    runner.invoke()
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        coVerify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given transformSource, When register effect in scope with transformSource, Then runner invoked`() = runTest {
        val transformSource: (TestUpdateSource) -> UpdateSource<Int, TestState>? = {
            UpdateSource(action = 1, current = it.current)
        }
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope(transformSource = transformSource) {
                effect {
                    runner.invoke()
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        coVerify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given action1 input, When register effect in scope with action1 filter, Then runner invoked`() = runTest {
        val input = TestAction.Action1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope<TestAction.Action1> {
                effect {
                    runner.invoke()
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(input)
            .effectScenario()
            .launchAndGetTrace()
        coVerify(exactly = 1) {
            runner.invoke()
        }
    }
}

@Suppress("TestFunctionName")
private fun TestActionScopeReduceBuilder(
    buildDSL: ActionScopeReduceBuilder<TestAction, TestState, TestAction, TestState>.() -> Unit
) = ActionScopeReduceBuilder<TestAction, TestState, TestAction, TestState>(scope = Any()).apply(buildDSL)