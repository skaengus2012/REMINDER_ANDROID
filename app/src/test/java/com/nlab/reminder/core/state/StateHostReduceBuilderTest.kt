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

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateHostReduceBuilderTest {
    @Test
    fun `update to state2 when current was any state`() = runTest {
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
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
    fun `update to state1 when current was specific state`() = runTest {
        val fixedInitState: TestState = TestState.genState()
        val expectedState: TestState = TestState.State2()
        testReduceTemplate(
            initState = fixedInitState,
            expectedState = expectedState,
            block = {
                reduce {
                    anyEvent {
                        filteredState(
                            predicate = { before -> before === fixedInitState },
                            block = { expectedState }
                        )
                    }
                }
            }
        )
    }

    @Test
    fun `not update when current was not specific state`() = runTest {
        val fixedInitState: TestState = TestState.genState()
        testReduceTemplate(
            initState = fixedInitState,
            expectedState = fixedInitState,
            block = {
                reduce {
                    anyEvent {
                        filteredState(
                            predicate = { before -> before !== fixedInitState },
                            block = { TestState.State2() }
                        )
                    }
                }
            }
        )
    }

    @Test
    fun `update to state1 when current was init`() = runTest {
        val expectedState = TestState.State1()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = TestState.StateInit(),
            expectedState = expectedState,
            block = {
                reduce {
                    anyEvent {
                        state<TestState.StateInit> { expectedState }
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
                    anyEvent {
                        stateNot<TestState.StateInit> { expectedState }
                    }
                }
            }
        )
    }

    @Test
    fun `not update when current was init`() = runTest {
        val fixedInitState: TestState = TestState.StateInit()
        testReduceTemplate(
            input = TestEvent.genEvent(),
            initState = fixedInitState,
            expectedState = fixedInitState,
            block = {
                reduce {
                    anyEvent {
                        stateNot<TestState.StateInit> { TestState.State2() }
                    }
                }
            }
        )
    }

    /**
    @Test
    fun `update to state1 when test1 event sent`() = runTest {
        val expectedNextState = TestState.State1()
        val state = MutableStateFlow<TestState>(TestState.StateInit())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachine = StateMachine<TestEvent, TestState>().apply {
                update { (event, before) ->
                    when (event) {
                        is TestEvent.Event1 -> expectedNextState
                        is TestEvent.Event2 -> before
                    }
                }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()
        assertThat(state.value, equalTo(expectedNextState))
    }

    @Test
    fun `Not updated because event2 cannot changed anything`() = runTest {
        val initState = TestState.StateInit()
        val state = MutableStateFlow<TestState>(initState)
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachine = StateMachine<TestEvent, TestState>().apply {
                update { (event, before) ->
                    when (event) {
                        is TestEvent.Event1 -> TestState.State1()
                        is TestEvent.Event2 -> before
                    }
                }
            }
        )
        eventProcessor
            .send(TestEvent.Event2())
            .join()
        assertThat(state.value, equalTo(initState))
    }

    @Test
    fun `handled when any event sent`() {
        val expectedTest1EventCount = genInt("##")
        val expectedTest2EventCount = genInt("##")
        val action: () -> Unit = mock()
        sendEventWithHandleConfig(
            expectedTest1EventCount,
            expectedTest2EventCount,
            handleConfig = {
                handle { action() }
            }
        )
        verify(action, times(expectedTest1EventCount + expectedTest2EventCount))()
    }

    @Test
    fun `handled when event is TestEvent1`() {
        val expectedTest1EventCount = genInt("##")
        val action: () -> Unit = mock()
        sendEventWithHandleConfig(
            expectedTest1EventCount,
            expectedTest2EventCount = genInt("##"),
            handleConfig = {
                handle(
                    filter = { (event) -> event is TestEvent.Event1 },
                    block = { action() }
                )
            }
        )
        verify(action, times(expectedTest1EventCount))()
    }

    @Test
    fun `handled when event instance is TestEvent1`() {
        val expectedTest1EventCount = genInt("##")
        val action: () -> Unit = mock()
        sendEventWithHandleConfig(
            expectedTest1EventCount,
            expectedTest2EventCount = genInt("##"),
            handleConfig = {
                handleBy<TestEvent.Event1> { action() }
            }
        )
        verify(action, times(expectedTest1EventCount))()
    }

    @Test
    fun `handled when current was state1`() {
        val expectedTest1EventCount = genInt("##")
        val expectedTest2EventCount = genInt("##")
        val action: () -> Unit = mock()
        val sideEffectConfig: (StateMachine<TestEvent, TestState>).() -> Unit = {
            handleWhen<TestState.State1> { action() }
        }
        val initStates = listOf(
            TestState.StateInit(),
            TestState.State1(),
            TestState.State2()
        )

        initStates.forEach { initState ->
            sendEventWithHandleConfig(
                expectedTest1EventCount,
                expectedTest2EventCount,
                state = MutableStateFlow(initState),
                sideEffectConfig,
            )
        }
        verify(action, times(expectedTest1EventCount + expectedTest2EventCount))()
    }

    @Test
    fun `handled when current was state1 and event instance is TestEvent1`() {
        val expectedTest1EventCount = genInt("##")
        val action: () -> Unit = mock()
        val sideEffectConfig: (StateMachine<TestEvent, TestState>).() -> Unit = {
            handleOn<TestEvent.Event1, TestState.State1> { action() }
        }
        val initStates = listOf(
            TestState.StateInit(),
            TestState.State1(),
            TestState.State2()
        )

        initStates.forEach { initState ->
            sendEventWithHandleConfig(
                expectedTest1EventCount,
                expectedTest2EventCount = genInt("##"),
                state = MutableStateFlow(initState),
                sideEffectConfig,
            )
        }
        verify(action, times(expectedTest1EventCount))()
    }

    private fun sendEventWithHandleConfig(
        expectedTest1EventCount: Int,
        expectedTest2EventCount: Int,
        state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit()),
        handleConfig: (StateMachine<TestEvent, TestState>).() -> Unit,
    ) = runTest {
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachine = StateMachine<TestEvent, TestState>().apply { handleConfig(this) }
        )
        val jobs: List<Job> =
            List(expectedTest1EventCount) { eventProcessor.send(TestEvent.Event1()) } +
                    List(expectedTest2EventCount) { eventProcessor.send(TestEvent.Event2()) }
        jobs.joinAll()
    }

    @Test
    fun `handled with before state when after update`() = runTest {
        val updateSource: UpdateSource<TestEvent, TestState> = UpdateSource(
            TestEvent.Event1(),
            TestState.State2(),
        )
        val updateFunction: (UpdateSource<TestEvent, TestState>) -> TestState = mock {
            whenever(mock.invoke(updateSource)) doReturn TestState.State1()
        }
        val sideEffectFunction: (UpdateSource<TestEvent, TestState>) -> Unit = mock()
        val eventProcessor = createTestStateMachineEventProcessor(
            state = MutableStateFlow(updateSource.before),
            stateMachine = StateMachine<TestEvent, TestState>().apply {
                update { updateFunction(it) }
                handle { sideEffectFunction(it) }
            }
        )
        eventProcessor
            .send(updateSource.event)
            .join()

        val executionOrder = inOrder(updateFunction, sideEffectFunction)
        executionOrder.verify(updateFunction, once())(updateSource)
        executionOrder.verify(sideEffectFunction, once())(updateSource)
    }*/
}