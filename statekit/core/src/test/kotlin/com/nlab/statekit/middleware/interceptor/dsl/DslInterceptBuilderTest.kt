/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.middleware.interceptor.dsl

import com.nlab.statekit.*
import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import org.mockito.kotlin.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.verification.VerificationMode

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class DslInterceptBuilderTest {

    private suspend fun checkWithActionDispatch(
        inputAction: TestAction = TestAction.genAction(),
        initState: TestState = TestState.genState(),
        dispatchAction: TestAction,
        verifyMode: VerificationMode,
        buildDSL: (DslInterceptBuilder<TestAction, TestState>).() -> Unit
    ) {
        val mockDispatcher: ActionDispatcher<TestAction> = mock()
        val interceptor = DslInterceptBuilder<TestAction, TestState>()
            .apply(buildDSL)
            .build()
        interceptor(mockDispatcher, UpdateSource(inputAction, initState))
        verify(mockDispatcher, verifyMode).dispatch(dispatchAction)
    }

    @Test
    fun `anyAction should be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                anyAction {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `When filteredAction predicate is true, action should be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                filteredAction(predicate = { true }) {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `When filteredAction predicate is false, action should not be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                filteredAction(predicate = { false }) {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should be dispatched, when action is action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action1,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                action<TestAction.Action1> {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should not be dispatched, when action is not action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action2,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                action<TestAction.Action1> {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should be dispatched, when action is not action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action2,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                actionNot<TestAction.Action1> {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should not be dispatched, when action is action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action1,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                actionNot<TestAction.Action1> {
                    anyState { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `anyState should be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                anyState {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `When filteredState predicate is true, action should be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                filteredState(predicate = { true }) {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `When filteredState predicate is false, action should not be dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                filteredState(predicate = { false }) {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should be dispatched, when state is state1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State1,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                state<TestState.State1> {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should not be dispatched, when state is not state1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State2,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                state<TestState.State1> {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should be dispatched, when state is not state1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State2,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = {
                stateNot<TestState.State1> {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }

    @Test
    fun `Action should not be dispatched, when state is state1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State1,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = {
                stateNot<TestState.State1> {
                    anyAction { dispatch(dispatchAction) }
                }
            }
        )
    }
}