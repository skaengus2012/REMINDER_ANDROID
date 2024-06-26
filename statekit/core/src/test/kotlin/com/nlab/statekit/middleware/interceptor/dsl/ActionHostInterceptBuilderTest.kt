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
internal class ActionHostInterceptBuilderTest {

    private suspend fun checkWithActionDispatch(
        inputAction: TestAction = TestAction.genAction(),
        dispatchAction: TestAction,
        verifyMode: VerificationMode,
        buildDSL: (ActionHostInterceptBuilder<TestAction, TestState>).() -> Unit
    ) {
        val mockDispatcher: ActionDispatcher<TestAction> = mock()
        val interceptor = ActionHostInterceptBuilder<TestAction, TestState>()
            .apply(buildDSL)
            .build()
        interceptor(mockDispatcher, UpdateSource(inputAction, TestState.genState()))
        verify(mockDispatcher, verifyMode).dispatch(dispatchAction)
    }

    @Test
    fun `When interceptor inputted the action, Action dispatched`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = { anyAction { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action dispatched, when predicate condition is true`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = { filteredAction(predicate = { true }) { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action never dispatched, when predicate condition is false`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = { filteredAction(predicate = { false }) { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action dispatched, when action is action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action1,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = { action<TestAction.Action1> { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action never dispatched, when action is not action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action2,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = { action<TestAction.Action1> { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action dispatched, when action is not action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action2,
            dispatchAction = dispatchAction,
            verifyMode = once(),
            buildDSL = { actionNot<TestAction.Action1> { dispatch(dispatchAction) } }
        )
    }

    @Test
    fun `Action never dispatched, when action is action1`() = runTest {
        val dispatchAction = TestAction.genAction()
        checkWithActionDispatch(
            inputAction = TestAction.Action1,
            dispatchAction = dispatchAction,
            verifyMode = never(),
            buildDSL = { actionNot<TestAction.Action1> { dispatch(dispatchAction) } }
        )
    }
}