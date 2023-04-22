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

package com.nlab.statekit.middleware.enhancer.dsl

import com.nlab.statekit.*
import com.nlab.statekit.middleware.enhancer.ActionDispatcher
import com.nlab.testkit.once
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
internal class StateHostEnhanceBuilderTest {

    private suspend fun checkWithActionDispatch(
        initState: TestState = TestState.genState(),
        dispatchAction: TestAction,
        verifyMode: VerificationMode,
        buildDSL: (StateHostEnhanceBuilder<TestAction, TestAction, TestState>).() -> Unit,
    ) {
        val mockDispatcher: ActionDispatcher<TestAction> = mock()
        val enhancer = StateHostEnhanceBuilder<TestAction, TestAction, TestState>()
            .apply(buildDSL)
            .build()
        enhancer(mockDispatcher, UpdateSource(TestAction.genAction(), initState))

        verify(mockDispatcher, verifyMode).dispatch(dispatchAction)
    }

    @Test
    fun `When enhancer inputted the state, Action dispatched`() = runTest {
        val action = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = action,
            verifyMode = once(),
            buildDSL = { anyState { dispatch(action) } }
        )
    }

    @Test
    fun `Action dispatched, when predicate condition is true`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = action,
            verifyMode = once(),
            buildDSL = {
                filteredState(predicate = { true }) { dispatch(action) }
            }
        )
    }

    @Test
    fun `Action never dispatched, when predicate condition is false`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            dispatchAction = action,
            verifyMode = never(),
            buildDSL = {
                filteredState(predicate = { false }) { dispatch(action) }
            }
        )
    }

    @Test
    fun `Action dispatched, when before is state1`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State1,
            dispatchAction = action,
            verifyMode = once(),
            buildDSL = { state<TestState.State1> { dispatch(action) } }
        )
    }

    @Test
    fun `Action never dispatched, when before is not state1`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State2,
            dispatchAction = action,
            verifyMode = never(),
            buildDSL = { state<TestState.State1> { dispatch(action) } }
        )
    }

    @Test
    fun `Action dispatched, when before is not state1`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State2,
            dispatchAction = action,
            verifyMode = once(),
            buildDSL = { stateNot<TestState.State1> { dispatch(action) } }
        )
    }

    @Test
    fun `ActionDispatcher never dispatched, when before is state1`() = runTest {
        val action: TestAction = TestAction.genAction()
        checkWithActionDispatch(
            initState = TestState.State1,
            dispatchAction = action,
            verifyMode = never(),
            buildDSL = { stateNot<TestState.State1> { dispatch(action) } }
        )
    }
}