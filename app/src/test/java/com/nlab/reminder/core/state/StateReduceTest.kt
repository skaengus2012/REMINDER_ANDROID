/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
internal class StateReduceTest {
    @Test
    fun `emit update when started value was not changed`() {
        val event = TestEvent.Event1()
        val initState = TestState.State2()
        val targetState = TestState.State1()
        val state: MutableStateFlow<TestState> = MutableStateFlow(initState)
        val invoker = StateReduce<TestEvent, TestState>(
            state,
            reducer = { targetState }
        )
        assertThat(
            invoker.getSourceAndUpdate(event),
            equalTo(UpdateSource(event, initState))
        )
        assertThat(
            state.value,
            equalTo(targetState)
        )
    }

    @Test
    fun `update with current state when started value was changed`() {
        val event = TestEvent.Event1()
        val initState = TestState.State2()
        val updateState = TestState.StateInit()
        val state: MutableStateFlow<TestState> = MutableStateFlow(initState)
        val invoker = StateReduce<TestEvent, TestState>(
            state,
            reducer = { updateSource ->
                if (updateSource.before == initState) TestState.State1()
                else updateSource.before
            }
        )
        state.value = updateState

        assertThat(
            invoker.getSourceAndUpdate(event),
            equalTo(UpdateSource(event, updateState))
        )
        assertThat(
            state.value,
            equalTo(updateState)
        )
    }
}