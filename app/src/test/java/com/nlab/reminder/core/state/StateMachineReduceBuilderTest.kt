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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineReduceBuilderTest {
    @Test
    fun `update to state2 when any event sent`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            expectedState = expectedState,
            block = {
                reduce {
                    anyEvent {
                        anyState { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when specific event sent`() = runTest {
        val fixedEvent: TestEvent = TestEvent.genEvent()
        val expectedState: TestState = TestState.State1()
        testReduceTemplate(
            input = fixedEvent,
            expectedState = expectedState,
            block = {
                reduce {
                    filteredEvent(predicate = { event -> event === fixedEvent }) {
                        anyState { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when any event excluded specific instance sent`() = runTest {
        val fixedEvent: TestEvent = TestEvent.genEvent()
        val fixedState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = fixedEvent,
            initState = fixedState,
            expectedState = fixedState,
            block = {
                reduce {
                    filteredEvent(predicate = { event -> event !== fixedEvent }) {
                        anyState { TestState.State1() }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when event1 event sent`() = runTest {
        val expectedState: TestState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.Event1(),
            expectedState = expectedState,
            block = {
                reduce {
                    event<TestEvent.Event1> {
                        anyState { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when any event excluded event1 sent`() = runTest {
        val fixedState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = TestEvent.Event2(),
            initState = fixedState,
            expectedState = fixedState,
            block = {
                reduce {
                    event<TestEvent.Event1> {
                        anyState { TestState.State1() }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when any event excluded event1 sent`() = runTest {
        val expectedState: TestState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.Event2(),
            expectedState = expectedState,
            block = {
                reduce {
                    eventNot<TestEvent.Event1> {
                        anyState { expectedState }
                    }
                }
            }
        )
    }


    @Test
    fun `not update when event1 sent`() = runTest {
        val fixedState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = TestEvent.Event1(),
            initState = fixedState,
            expectedState = fixedState,
            block = {
                reduce {
                    eventNot<TestEvent.Event1> {
                        anyState { TestState.State1() }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state2 when current was any state`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            expectedState = expectedState,
            block = {
                reduce {
                    anyState {
                        anyEvent { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when current was specific state`() = runTest {
        val fixedInitState: TestState = TestState.StateInit()
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = fixedInitState,
            expectedState = expectedState,
            block = {
                reduce {
                    filteredState(predicate = { state -> state === fixedInitState }) {
                        anyEvent { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when current was not specific state`() = runTest {
        val fixedInitState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = fixedInitState,
            expectedState = fixedInitState,
            block = {
                reduce {
                    filteredState(predicate = { state -> state !== fixedInitState }) {
                        anyEvent { TestState.State2() }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when current was init`() = runTest {
        val expectedState: TestState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = TestState.StateInit(),
            expectedState = expectedState,
            block = {
                reduce {
                    state<TestState.StateInit> {
                        anyEvent { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when current was not init`() = runTest {
        val fixedInitState: TestState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = fixedInitState,
            expectedState = fixedInitState,
            block = {
                reduce {
                    state<TestState.StateInit> {
                        anyEvent { TestState.State2() }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state2 when current was not init`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = TestState.State1(),
            expectedState = expectedState,
            block = {
                reduce {
                    stateNot<TestState.StateInit> {
                        anyEvent { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update whe current was init`() = runTest {
        val fixedInitState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = fixedInitState,
            expectedState = fixedInitState,
            block = {
                reduce {
                    stateNot<TestState.StateInit> {
                        anyEvent { TestState.State2() }
                    }
                }
            }
        )
    }
}