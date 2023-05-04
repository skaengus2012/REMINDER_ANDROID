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

package com.nlab.statekit.reducer.dsl

import com.nlab.statekit.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class DslReduceBuilderTest {

    private fun checkWithReduceResult(
        inputAction: TestAction = TestAction.genAction(),
        initState: TestState,
        expectedState: TestState,
        buildDSL: DslReduceBuilder<TestAction, TestState>.() -> Unit
    ) {
        val reduce = DslReduceBuilder<TestAction, TestState>()
            .apply(buildDSL)
            .build()
        val actualState = reduce(UpdateSource(inputAction, initState))
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `anyAction returns the given state`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                anyAction {
                    anyState { expectedState }
                }
            }
        )
    }

    @Test
    fun `When filteredAction predicate is true, expectedState is returned`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                filteredAction(predicate = { true }) {
                    anyState { expectedState }
                }
            }
        )
    }

    @Test
    fun `When filteredAction predicate is false, initState is returned`() {
        val initState = TestState.State1
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                filteredAction(predicate = { false }) {
                    anyState { TestState.State2 }
                }
            }
        )
    }

    @Test
    fun `Expected state is returned, when input is action1`() {
        val input = TestAction.Action1
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            inputAction = input,
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                action<TestAction.Action1> {
                    anyState { expectedState }
                }
            }
        )
    }

    @Test
    fun `Init state is returned, when input is not action1`() {
        val input = TestAction.Action2
        val initState = TestState.State1
        checkWithReduceResult(
            inputAction = input,
            initState = initState,
            expectedState = initState,
            buildDSL = {
                action<TestAction.Action1> {
                    anyState { TestState.State2 }
                }
            }
        )
    }

    @Test
    fun `Expected state is returned, when input is not action1`() {
        val input = TestAction.Action2
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            inputAction = input,
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                actionNot<TestAction.Action1> {
                    anyState { expectedState }
                }
            }
        )
    }

    @Test
    fun `Init state is returned, when input is action1`() {
        val input = TestAction.Action1
        val initState = TestState.State1
        checkWithReduceResult(
            inputAction = input,
            initState = initState,
            expectedState = initState,
            buildDSL = {
                actionNot<TestAction.Action1> {
                    anyState { TestState.State2 }
                }
            }
        )
    }

    @Test
    fun `anyState returns the given state`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                anyState {
                    anyAction { expectedState }
                }
            }
        )
    }

    @Test
    fun `When filteredState predicate is true, expectedState is returned`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                filteredState(predicate = { true }) {
                    anyAction { expectedState }
                }
            }
        )
    }

    @Test
    fun `When filteredState predicate is false, initState is returned`() {
        val initState = TestState.State1
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                filteredState(predicate = { false }) {
                    anyAction { TestState.State2 }
                }
            }
        )
    }

    @Test
    fun `Expected state is returned, when init is state1`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                state<TestState.State1> {
                    anyAction { expectedState }
                }
            }
        )
    }

    @Test
    fun `Init state is returned, when init is not state1`() {
        val initState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                state<TestState.State1> {
                    anyAction { TestState.State3 }
                }
            }
        )
    }

    @Test
    fun `Expected state is returned, when init is not state1`() {
        val initState = TestState.State2
        val expectedState = TestState.State3
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = {
                stateNot<TestState.State1> {
                    anyAction { expectedState }
                }
            }
        )
    }

    @Test
    fun `Init state is returned, when init is state1`() {
        val initState = TestState.State1
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                stateNot<TestState.State1> {
                    anyAction { TestState.State3 }
                }
            }
        )
    }
}