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
import org.mockito.Mockito.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class ActionScopeReduceBuilderTest {
    @Test
    fun `Given matched inputs and expected state, When transition with state type from builder, Then return expectedState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestActionScopeReduceBuilder {
            transition<TestState.State1> {
                if (action == inputAction) expectedState
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
    fun `Given none matched inputs, When transition with state type form builder, Then return inputState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val reduceBuilder = TestActionScopeReduceBuilder {
            transition<TestState.State2> {
                if (action == inputAction) TestState.State3
                else current
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(
            inputAction,
            inputState,
            inputState
        )
    }

    @Test
    fun `Given matched input and actionDispatcher, When launch effect from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestActionScopeReduceBuilder {
            effect {
                if (action == inputAction && current == inputState) dispatch(expectedAction)
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launch(inputAction, inputState, actionDispatcher,)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given matched inputs and actionDispatcher, When launch effect with state type from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestActionScopeReduceBuilder {
            effect<TestState.State1> {
                if (action == inputAction) dispatch(expectedAction)
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launch(inputAction, inputState, actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given inputs and matched predicated block, When transition with predicate scope from builder, Then return expectedState`() {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope(isMatch = { action == inputAction && current == inputState }) {
                transition { expectedState }
            }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.assert(inputAction, inputState, expectedState)
    }

    @Test
    fun `Given inputs, actionDispatcher and transformSource block, When launch effect with transformSource scope from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope(transformSource = {
                if (action == inputAction && current == inputState) UpdateSource(action = 1, current = current)
                else null
            }) {
                scope(transformSource = { UpdateSource(action = action + 2, current = current) }) {
                    effect {
                        if (action == 3) {
                            dispatch(expectedAction)
                        }
                    }
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launch(
            inputAction,
            inputState,
            actionDispatcher
        )
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given inputs, actionType and actionDispatcher, When launch effect with actionType scope from builder, Then actionDispatcher invoked`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val reduceBuilder = TestActionScopeReduceBuilder {
            scope<TestAction.Action1> {
                effect {
                    if (current == inputState) dispatch(expectedAction)
                }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.launch(
            inputAction,
            inputState,
            actionDispatcher,
        )
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }
}

@Suppress("TestFunctionName")
private fun TestActionScopeReduceBuilder(
    buildDSL: ActionScopeReduceBuilder<TestAction, TestState, TestAction, TestState>.() -> Unit
) = ActionScopeReduceBuilder<TestAction, TestState, TestAction, TestState>(scope = Any()).apply(buildDSL)