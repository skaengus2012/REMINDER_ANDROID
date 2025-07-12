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
import com.nlab.statekit.dispatch.ActionDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StateScopeReduceBuilderTest {
    @Test
    fun `Given matched inputs and expected state, When transition with action type from builder, Then return expectedState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestStateScopeReduceBuilder {
            transition<TestAction.Action1> {
                if (current == inputState) expectedState
                else current
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given none matched inputs, When transition with action type form builder, Then return inputState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val reduceBuilder = TestStateScopeReduceBuilder {
            transition<TestAction.Action2> { TestState.State2 }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, inputState)
    }

    @Test
    fun `Given matched input and runner, When launch effect form builder, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val runner: () -> Unit = mock()
        val reduceBuilder = TestStateScopeReduceBuilder {
            effect {
                if (action == inputAction && current == inputState) {
                    runner.invoke()
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState)
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given input state and runner, When launch effect with action type form builder, Then runner invoked conditionally`() = runTest {
        suspend fun invokeWhenStateTypeIsAction1(
            inputAction: TestAction,
            runner: () -> Unit
        ) {
            val reduceBuilder = TestStateScopeReduceBuilder {
                effect<TestAction.Action1> { runner.invoke() }
            }
            val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
            effect.launchAndJoinForTest(inputAction, TestState.genState())
        }

        val runnerWithAction1: () -> Unit = mock()
        val runnerWithAction2: () -> Unit = mock()
        invokeWhenStateTypeIsAction1(inputAction = TestAction.Action1, runnerWithAction1)
        invokeWhenStateTypeIsAction1(inputAction = TestAction.Action2, runnerWithAction2)

        verify(runnerWithAction1, once()).invoke()
        verify(runnerWithAction2, never()).invoke()
    }

    @Test
    fun `Given matched input and actionDispatcher, When launch suspend effect from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestStateScopeReduceBuilder {
            suspendEffect {
                if (action == inputAction && current == inputState) dispatch(expectedAction)
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given matched inputs and actionDispatcher, When launch suspend effect with action type from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestStateScopeReduceBuilder {
            suspendEffect<TestAction.Action1> {
                if (current == inputState) dispatch(expectedAction)
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given inputs and matched predicated block, When transition with predicate scope from builder, Then return expectedState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope(isMatch = { action == inputAction && current == inputState }) {
                transition { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given inputs, actionDispatcher and transformSource block, When launch suspend effect with transformSource scope from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope(transformSource = {
                if (action == inputAction && current == inputState) UpdateSource(action = 1, current = current)
                else null
            }) {
                scope(transformSource = { UpdateSource(action = action + 2, current = current) }) {
                    suspendEffect {
                        if (action == 3) {
                            dispatch(expectedAction)
                        }
                    }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given inputs, actionType and actionDispatcher, When launch suspend effect with stateType scope from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope<TestState.State1> {
                suspendEffect {
                    if (action == inputAction) dispatch(expectedAction)
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launchAndJoinForTest(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }
}

@Suppress("TestFunctionName")
private fun TestStateScopeReduceBuilder(
    buildDSL: StateScopeReduceBuilder<TestAction, TestState, TestAction, TestState>.() -> Unit
) = StateScopeReduceBuilder<TestAction, TestState, TestAction, TestState>(scope = Any()).apply(buildDSL)