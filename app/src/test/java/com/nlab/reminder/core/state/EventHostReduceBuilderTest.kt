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
class EventHostReduceBuilderTest {
    @Test
    fun `update to state2 when any event sent`() = runTest {
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
    fun `update to state1 when specific event sent`() = runTest {
        val fixedEvent: TestEvent = TestEvent.genEvent()
        val expectedState: TestState = TestState.State1()
        testReduceTemplate(
            input = fixedEvent,
            expectedState = expectedState,
            block = {
                reduce {
                    anyState {
                        filteredEvent(
                            predicate = { event -> event === fixedEvent },
                            block = { expectedState }
                        )
                    }
                }
            }
        )
    }

    @Test
    fun `not update when any event excluded specific instance sent`() = runTest {
        val fixedEvent: TestEvent = TestEvent.genEvent()
        val fixedState: TestState = TestState.State1()
        testReduceTemplate(
            input = fixedEvent,
            initState = fixedState,
            expectedState = fixedState,
            block = {
                reduce {
                    anyState {
                        filteredEvent(
                            predicate = { event -> event !== fixedEvent },
                            block = { TestState.State2() }
                        )
                    }
                }
            }
        )
    }

    @Test
    fun `update to state2 when event1 sent`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.Event1(),
            expectedState = expectedState,
            block = {
                reduce {
                    anyState {
                        event<TestEvent.Event1> { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `update to state2 when any event excluded event1 sent`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            input = TestEvent.Event2(),
            expectedState = expectedState,
            block = {
                reduce {
                    anyState {
                        eventNot<TestEvent.Event1> { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when event1 sent`() = runTest {
        val fixedState: TestState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.Event1(),
            initState = fixedState,
            expectedState = fixedState,
            block = {
                reduce {
                    anyState {
                        eventNot<TestEvent.Event1> { TestState.State2() }
                    }
                }
            }
        )
    }
}