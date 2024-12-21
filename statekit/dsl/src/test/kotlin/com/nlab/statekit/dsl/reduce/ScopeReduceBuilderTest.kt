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
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class ScopeReduceBuilderTest {
    @Test
    fun `Given inputs and transition block, When transition from builder, Then return expected state`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2

        val reduceBuilder = TestScopeReduceBuilder {
            transition {
                if (action == inputAction && current == inputState) expectedState
                else current
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(
            inputAction,
            inputState,
            expectedState
        )
    }

    @Test
    fun `Given inputs, When launch effect from builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mock()
        val reduceBuilder = TestScopeReduceBuilder {
            effect {
                if (action == inputAction && current == inputState) {
                    runner()
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState)
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given inputs and actionDispatcher, When launch suspend effect from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestScopeReduceBuilder {
            suspendEffect {
                if (action == inputAction && current == inputState) {
                    dispatch(expectedAction)
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given inputs and predicated block, When transition from predicate scope builder, Then return expected state`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            scope(isMatch = { action == inputAction && current == inputState }) {
                transition { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given inputs and transform source block, When transition from transformSource scope builder, Then return expected state`() {
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
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given inputs and runner, When launch effect from action scope builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mock()
        val reduceBuilder = TestScopeReduceBuilder {
            actionScope {
                scope(isMatch = { action == inputAction }) {
                    effect<TestState.State1> { runner() }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState)
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given inputs and actionType, When transition from action scope builder, Then return expected state`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            actionScope<TestAction.Action1> {
                transition<TestState.State1> { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given inputs and runner, When launch effect from state scope builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mock()
        val reduceBuilder = TestScopeReduceBuilder {
            stateScope {
                scope(isMatch = { current == inputState }) {
                    effect<TestAction.Action1> { runner() }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState)
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given inputs and actionType, When transition from state scope builder, Then return expected state`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestScopeReduceBuilder {
            stateScope<TestState.State1> {
                transition<TestAction.Action1> { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }
}

@Suppress("TestFunctionName")
private fun TestScopeReduceBuilder(
    buildDSL: ScopeReduceBuilder<TestAction, TestState, TestAction, TestState>.() -> Unit
) = ScopeReduceBuilder<TestAction, TestState, TestAction, TestState>(scope = Any()).apply(buildDSL)