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
internal class StateHostReduceBuilderTest {

    private fun checkWithReduceResult(
        initState: TestState,
        expectedState: TestState,
        buildDSL: StateHostReduceBuilder<TestAction, TestState>.() -> Unit
    ) {
        val reduce = StateHostReduceBuilder<TestAction, TestState>(ReduceEndScope())
            .apply(buildDSL)
            .build()
        val actualState = reduce(UpdateSource(TestAction.genAction(), initState))
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `It should build with expected state when anyState is set`() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = expectedState,
            buildDSL = { anyState { expectedState } }
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
                filteredState(predicate = { true }) { expectedState }
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
                filteredState(predicate = { false }) { TestState.State2 }
            }
        )
    }

    @Test
    fun `Expected state is returned, when before is state1`() {
        val expectedState = TestState.State2
        checkWithReduceResult(
            initState = TestState.State1,
            expectedState = expectedState,
            buildDSL = {
                state<TestState.State1> { expectedState }
            }
        )
    }

    @Test
    fun `Init state is returned, when before is state2`() {
        val initState = TestState.State2
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                state<TestState.State1> { TestState.State3 }
            }
        )
    }

    @Test
    fun `Expected state is returned, when before is not state1`() {
        val expectedState = TestState.State3
        checkWithReduceResult(
            initState = TestState.State2,
            expectedState = expectedState,
            buildDSL = {
                stateNot<TestState.State1> { expectedState }
            }
        )
    }

    @Test
    fun `Init state is returned, when before is state1`() {
        val initState = TestState.State1
        checkWithReduceResult(
            initState = initState,
            expectedState = initState,
            buildDSL = {
                stateNot<TestState.State1> { TestState.State2 }
            }
        )
    }
}