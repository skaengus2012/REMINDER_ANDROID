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

package com.nlab.reminder.core.state2.reducer.dsl

import com.nlab.reminder.core.state2.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
class ActionHostReduceBuilderTest {

    private fun checkWithReduceResult(
        inputAction: TestAction = TestAction.genAction(),
        initState: TestState,
        expectedState: TestState,
        buildDSL: ActionHostReduceBuilder<TestAction, TestState, TestState>.() -> Unit
    ) {
        val reduce = ActionHostReduceBuilder<TestAction, TestState, TestState>(ReduceEndScope())
            .apply(buildDSL)
            .build()
        val actualState = reduce(UpdateSource(inputAction, initState))
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `It should build with expected state when anyAction is set`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = { anyAction { expectedState } }
        )
    }

    @Test
    fun `Expected state is returned, when predicate condition is true`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                filteredAction(predicate = { true }) { expectedState }
            }
        )
    }

    @Test
    fun `Init state is returned, when predicate condition is false`() {
        val initState = TestState.State1
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                filteredAction(predicate = { false }) { TestState.State2 }
            }
        )
    }

    @Test
    fun `Expected state is returned, when input is action1`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            inputAction = TestAction.Action1,
            initState = initState,
            expectedState = expectedState,
            buildDSL = { action<TestAction.Action1> { expectedState } }
        )
    }

    @Test
    fun `Init state is returned, when input is action2`() {
        val initState = TestState.State1
        checkWithReduceResult(
            inputAction = TestAction.Action2,
            initState = initState,
            expectedState = initState,
            buildDSL = { action<TestAction.Action1> { TestState.State3 } }
        )
    }

    @Test
    fun `Expected state is returned, when input is not action1`() {
        val initState = TestState.State2
        val expectedState = TestState.State1
        checkWithReduceResult(
            inputAction = TestAction.Action2,
            initState = initState,
            expectedState = expectedState,
            buildDSL = { actionNot<TestAction.Action1> { expectedState } }
        )
    }

    @Test
    fun `Init state is returned, when input is action1`() {
        val initState = TestState.State1
        checkWithReduceResult(
            inputAction = TestAction.Action1,
            initState = initState,
            expectedState = initState,
            buildDSL = {  actionNot<TestAction.Action1> { TestState.State2 } }
        )
    }
}