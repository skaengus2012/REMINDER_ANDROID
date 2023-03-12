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
class DslReducerTest {
    @Test
    fun testDefineDSL() {
        val expectedState = TestState.State2
        val reducer = DslReducer<TestAction, TestState>(
            defineDSL = {
                action<TestAction.Action1> {
                    state<TestState.State1> { expectedState }
                }
            }
        )
        val actualState = reducer(UpdateSource(TestAction.Action1, TestState.State1))
        assertThat(actualState, equalTo(expectedState))
    }
}