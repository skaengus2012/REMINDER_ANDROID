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
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Doohyun
 */
class ScopeReduceBuilderTest {
    @Test
    fun `Given inputs and predicated block, When transition from predicate scope builder, Then return expected state`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            scope(isMatch = { action == inputAction && current == inputState }) {
                transition { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given inputs and transform source block, When transition from transformSource scope builder, Then return expected state`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            scope(transformSource = { UpdateSource(action = 1, current) }) {
                scope(transformSource = { UpdateSource(action = action + 1, current) }) {
                    transition {
                        if (action == 2) expectedState
                        else current
                    }
                }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given inputs and runner, When launch effect from action scope builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestScopeReduceBuilder {
            actionScope {
                scope(isMatch = { action == inputAction }) {
                    effect<TestState.State1> { runner() }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given inputs and actionType, When transition from action scope builder, Then return expected state`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            actionScope<TestAction.Action1> {
                transition<TestState.State1> { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given inputs and runner, When launch effect from state scope builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestScopeReduceBuilder {
            this.stateScope {
                scope(isMatch = { current == inputState }) {
                    effect<TestAction.Action1> { runner() }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given inputs and actionType, When transition from state scope builder, Then return expected state`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            stateScope<TestState.State1> {
                transition<TestAction.Action1> { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNextState(expectedState)
    }
}

@Suppress("TestFunctionName")
private fun TestScopeReduceBuilder(
    buildDSL: RootScopeReduceBuilder<TestAction, TestState>.() -> Unit
) = RootScopeReduceBuilder<TestAction, TestState>(scope = Any()).apply(buildDSL)