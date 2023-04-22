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

package com.nlab.statekit.util

import com.nlab.statekit.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
internal class ReducerUtilsKtTest {
    @Test
    fun testBuildReducer() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        val testReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action is TestAction.Action1) expectedState
            else before
        }
        val actualState = testReducer(UpdateSource(TestAction.Action1, initState))
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun testBuildDslReducer() {
        val initState = TestState.State1
        val expectedState = TestState.State2
        val testReducer = buildDslReducer<TestAction, TestState> {
            anyState {
                action<TestAction.Action1> { expectedState }
            }
        }
        val actualState = testReducer(UpdateSource(TestAction.Action1, initState))
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Testing composition Reducer using the first Reducer`() {
        val expectedResult = TestState.State3
        val firstReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action is TestAction.Action1) expectedResult
            else before
        }
        val lastReducer = buildReducer<TestAction, TestState> { it.before }

        val concatReducer = firstReducer + lastReducer
        val actualState = concatReducer(UpdateSource(TestAction.Action1, TestState.State1))
        assertThat(actualState, equalTo(expectedResult))
    }

    @Test
    fun `Testing composition Reducer using the last Reducer`() {
        val expectedResult = TestState.State3
        val firstReducer = buildReducer<TestAction, TestState> { it.before }
        val lastReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action is TestAction.Action1) expectedResult
            else before
        }

        val concatReducer = firstReducer + lastReducer
        val actualState = concatReducer(UpdateSource(TestAction.Action1, TestState.State1))
        assertThat(actualState, equalTo(expectedResult))
    }

    @Test
    fun `When combining two reducers with the same condition using '+' operator, the lastReducer takes precedence`() {
        val expectedResult = TestState.State3
        val firstReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action is TestAction.Action1) TestState.State2
            else before
        }
        val lastReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action is TestAction.Action1) expectedResult
            else before
        }

        val concatReducer = firstReducer + lastReducer
        val actualState = concatReducer(UpdateSource(TestAction.Action1, TestState.State1))
        assertThat(actualState, equalTo(expectedResult))
    }

    @Test
    fun `When state is updated, lastReducer should have higher priority than firstReducer`() {
        val input = TestAction.Action1
        val initState = TestState.State1
        val expectedState = TestState.State2
        val mockBlock: (UpdateSource<TestAction, TestState>) -> TestState = mock()
        val firstReducer = buildReducer(mockBlock)
        val lastReducer = buildReducer<TestAction, TestState> { (action, before) ->
            if (action == input) expectedState else before
        }

        val concatReducer = firstReducer + lastReducer
        val actualState = concatReducer(UpdateSource(input, initState))

        verify(mockBlock, never()).invoke(any())
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `reducer with no state change returns original state`() {
        val initState = TestState.State1
        val firstReducer = buildReducer<TestAction, TestState> { it.before }
        val lastReducer = buildReducer<TestAction, TestState> { it.before }

        val concatReducer = firstReducer + lastReducer
        val actualState = concatReducer(UpdateSource(TestAction.Action1, initState))

        assertThat(actualState, equalTo(initState))
    }
}