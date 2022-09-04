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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.TestEvent
import com.nlab.reminder.core.state.TestState
import com.nlab.reminder.test.instanceOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class StateMachineEventProcessorTest {
    private lateinit var recoveryExceptionHandler: (Throwable) -> Unit

    @Before
    fun setup() {
        recoveryExceptionHandler = StateMachinePlugin.defaultErrorHandler
        StateMachinePlugin.defaultErrorHandler = {}
    }

    @After
    fun tearDown() {
        StateMachinePlugin.defaultErrorHandler = recoveryExceptionHandler
    }

    private fun createTestStateMachineEventProcessor(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit()),
        stateMachineBuilder: StateMachineBuilder<TestEvent, TestState> = StateMachineBuilder()
    ): StateMachineEventProcessor<TestEvent, TestState> =
        StateMachineEventProcessor(scope, state, stateMachineBuilder)

    @Test
    fun `handled exceptions when trying to update with testEvent1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.updateTo { (event, old) ->
                when (event) {
                    is TestEvent.Event1 -> throw throwable
                    is TestEvent.Event2 -> old
                }
            }
        }
    }

    @Test
    fun `handled exceptions when sideEffect executed by testEvent1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.sideEffectBy<TestEvent.Event1> { throw throwable }
        }
    }

    private suspend fun testExceptionHandler(
        setupThrowingException: (StateMachineBuilder<TestEvent, TestState>, Throwable) -> Unit
    ) {
        val exception = Throwable()
        val firstErrorHandler: (Throwable) -> Unit = mock()
        val secondErrorHandler: (Throwable) -> Unit = mock()
        val eventProcessor = createTestStateMachineEventProcessor(
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                onError(firstErrorHandler)
                onError(secondErrorHandler)
                setupThrowingException(this, exception)
            }
        )

        eventProcessor
            .send(TestEvent.Event1())
            .join()
        verify(firstErrorHandler, times(1))(exception)
        verify(secondErrorHandler, times(1))(exception)
    }

    @Test
    fun `state is not changed when update config is not set`() = runTest {
        val initState = TestState.State2()
        val state = MutableStateFlow<TestState>(initState)
        val eventProcessor = createTestStateMachineEventProcessor()
        eventProcessor
            .send(TestEvent.Event1())
            .join()
        eventProcessor
            .send(TestEvent.Event2())
            .join()
        assertThat(state.value, equalTo(initState))
    }

    @Test
    fun `sideEffect is executed after state update`() = runTest {
        val onReceiveEvent: (TestEvent, TestState) -> Unit = mock()
        val onReceiveState: (TestState) -> Unit = mock()
        val eventProcessor = createTestStateMachineEventProcessor(
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffect { onReceiveEvent(it.event, it.before) }
                updateTo { onReceiveState(it.before); it.before }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()

        val order = inOrder(onReceiveEvent, onReceiveState)
        order.verify(onReceiveState, times(1)).invoke(any())
        order.verify(onReceiveEvent, times(1)).invoke(any(), any())
    }

    @Test
    fun `ignore sideEffect event when config type was different with sideEffectBy`() = runTest {
        val onReceiveEvent: (TestEvent) -> Unit = mock()
        val eventProcessor = createTestStateMachineEventProcessor(
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffectBy<TestEvent.Event1> { (event) -> onReceiveEvent(event) }
            }
        )
        listOf(TestEvent.Event1(), TestEvent.Event2()).forEach { event ->
            eventProcessor
                .send(event)
                .join()
        }
        verify(onReceiveEvent, times(1)).invoke(any())
    }

    @Test
    fun `ignore sideEffect event when config type was different with sideEffectWhen`() = runTest {
        val onReceiveEvent: (TestEvent, TestState) -> Unit = mock()
        val stateFlow: MutableStateFlow<TestState> = MutableStateFlow(TestState.State2())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = stateFlow,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffectWhen<TestState.StateInit> { onReceiveEvent(it.event, it.before) }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()

        stateFlow.value = TestState.StateInit()
        listOf(TestEvent.Event1(), TestEvent.Event2()).forEach { event ->
            eventProcessor
                .send(event)
                .join()
        }

        verify(onReceiveEvent, times(2)).invoke(any(), any())
    }

    @Test
    fun `ignore sideEffect event when config type was different with sideEffectOn`() = runTest {
        val onReceiveEvent: (TestEvent, TestState) -> Unit = mock()
        val stateFlow: MutableStateFlow<TestState> = MutableStateFlow(TestState.State2())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = stateFlow,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffectOn<TestEvent.Event1, TestState.StateInit> { onReceiveEvent(it.event, it.before) }
            }
        )
        listOf(TestEvent.Event1(), TestEvent.Event2()).forEach { event ->
            eventProcessor
                .send(event)
                .join()
        }

        stateFlow.value = TestState.StateInit()
        listOf(TestEvent.Event1(), TestEvent.Event2()).forEach { event ->
            eventProcessor
                .send(event)
                .join()
        }
        verify(onReceiveEvent, times(1)).invoke(any(), any())
    }

    @Test
    fun `notify State1 when stateMachine receives event1`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                updateTo { (event) ->
                    when (event) {
                        is TestEvent.Event1 -> TestState.State1()
                        is TestEvent.Event2 -> TestState.State2()
                    }
                }
            }
        )

        eventProcessor
            .send(TestEvent.Event1())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `notify State2 when state is State1 and event2 receives`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.State1())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                updateTo { (event, oldState) ->
                    when (event) {
                        is TestEvent.Event1 -> oldState
                        is TestEvent.Event2 -> {
                            when (oldState) {
                                is TestState.StateInit -> oldState
                                is TestState.State1 -> TestState.State2()
                                is TestState.State2 -> TestState.State1()
                            }
                        }
                    }
                }
            }
        )

        eventProcessor
            .send(TestEvent.Event2())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State2::class)
        )
    }

    @Test
    fun `notify State1, State2 when state is init and event1 called event2 and invoked event1`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.State1())
        val eventProcessor = createTestStateMachineEventProcessor(
            scope = CoroutineScope(Dispatchers.Unconfined),
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                updateTo { (event) ->
                    when (event) {
                        is TestEvent.Event1 -> TestState.State1()
                        is TestEvent.Event2 -> TestState.State2()
                    }
                }

                sideEffectBy<TestEvent.Event1> { send(TestEvent.Event2()) }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State2::class)
        )
    }
}