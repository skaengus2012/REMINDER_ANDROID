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

import com.nlab.reminder.core.state.TestAction
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
internal class StateMachineActionProcessorTest {
    private lateinit var recoveryExceptionHandler: (Throwable) -> Unit

    @Before
    fun init() {
        recoveryExceptionHandler = StateMachineConfig.defaultExceptionHandler
        StateMachineConfig.defaultExceptionHandler = {}
    }

    @After
    fun finish() {
        StateMachineConfig.defaultExceptionHandler = recoveryExceptionHandler
    }

    private fun createTestStateMachineActionProcessor(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit()),
        stateMachineBuilder: StateMachineBuilder<TestAction, TestState> = StateMachineBuilder()
    ): StateMachineActionProcessor<TestAction, TestState> =
        StateMachineActionProcessor(scope, state, stateMachineBuilder)

    @Test
    fun `handled exceptions when trying to update with testAction1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.updateTo { (action, old) ->
                when (action) {
                    is TestAction.Action1 -> throw throwable
                    is TestAction.Action2 -> old
                }
            }
        }
    }

    @Test
    fun `handled exceptions when sideEffect executed by testAction1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.sideEffectBy<TestAction.Action1> { throw throwable }
        }
    }

    private suspend fun testExceptionHandler(
        setupThrowingException: (StateMachineBuilder<TestAction, TestState>, Throwable) -> Unit
    ) {
        val exception = Throwable()
        val firstErrorHandler: (Throwable) -> Unit = mock()
        val secondErrorHandler: (Throwable) -> Unit = mock()
        val actionProcessor = createTestStateMachineActionProcessor(
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                onError(firstErrorHandler)
                onError(secondErrorHandler)
                setupThrowingException(this, exception)
            }
        )

        actionProcessor
            .send(TestAction.Action1())
            .join()
        verify(firstErrorHandler, times(1))(exception)
        verify(secondErrorHandler, times(1))(exception)
    }

    @Test
    fun `state is not changed when update config is not set`() = runTest {
        val initState = TestState.State2()
        val state = MutableStateFlow<TestState>(initState)
        val actionProcessor = createTestStateMachineActionProcessor()
        actionProcessor
            .send(TestAction.Action1())
            .join()
        actionProcessor
            .send(TestAction.Action2())
            .join()
        assertThat(state.value, equalTo(initState))
    }

    @Test
    fun `sideEffect is executed after state update`() = runTest {
        val onReceiveAction: (TestAction, TestState) -> Unit = mock()
        val onReceiveState: (TestState) -> Unit = mock()
        val actionProcessor = createTestStateMachineActionProcessor(
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                sideEffect { onReceiveAction(it.action, it.oldState) }
                updateTo { onReceiveState(it.oldState); it.oldState }
            }
        )
        actionProcessor
            .send(TestAction.Action1())
            .join()

        val order = inOrder(onReceiveAction, onReceiveState)
        order.verify(onReceiveState, times(1)).invoke(any())
        order.verify(onReceiveAction, times(1)).invoke(any(), any())
    }

    @Test
    fun `ignore sideEffect action when config type was different with sideEffectBy`() = runTest {
        val onReceiveAction: (TestAction) -> Unit = mock()
        val actionProcessor = createTestStateMachineActionProcessor(
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                sideEffectBy<TestAction.Action1> { (action) -> onReceiveAction(action) }
            }
        )
        listOf(TestAction.Action1(), TestAction.Action2()).forEach { action ->
            actionProcessor
                .send(action)
                .join()
        }
        verify(onReceiveAction, times(1)).invoke(any())
    }

    @Test
    fun `ignore sideEffect action when config type was different with sideEffectWhen`() = runTest {
        val onReceiveAction: (TestAction, TestState) -> Unit = mock()
        val stateFlow: MutableStateFlow<TestState> = MutableStateFlow(TestState.State2())
        val actionProcessor = createTestStateMachineActionProcessor(
            state = stateFlow,
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                sideEffectWhen<TestState.StateInit> { onReceiveAction(it.action, it.oldState) }
            }
        )
        actionProcessor
            .send(TestAction.Action1())
            .join()

        stateFlow.value = TestState.StateInit()
        listOf(TestAction.Action1(), TestAction.Action2()).forEach { action ->
            actionProcessor
                .send(action)
                .join()
        }

        verify(onReceiveAction, times(2)).invoke(any(), any())
    }

    @Test
    fun `ignore sideEffect action when config type was different with sideEffectOn`() = runTest {
        val onReceiveAction: (TestAction, TestState) -> Unit = mock()
        val stateFlow: MutableStateFlow<TestState> = MutableStateFlow(TestState.State2())
        val actionProcessor = createTestStateMachineActionProcessor(
            state = stateFlow,
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                sideEffectOn<TestAction.Action1, TestState.StateInit> { onReceiveAction(it.action, it.oldState) }
            }
        )
        listOf(TestAction.Action1(), TestAction.Action2()).forEach { action ->
            actionProcessor
                .send(action)
                .join()
        }

        stateFlow.value = TestState.StateInit()
        listOf(TestAction.Action1(), TestAction.Action2()).forEach { action ->
            actionProcessor
                .send(action)
                .join()
        }
        verify(onReceiveAction, times(1)).invoke(any(), any())
    }

    @Test
    fun `notify State1 when stateMachine receives action1`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit())
        val actionProcessor = createTestStateMachineActionProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                updateTo { (action) ->
                    when (action) {
                        is TestAction.Action1 -> TestState.State1()
                        is TestAction.Action2 -> TestState.State2()
                    }
                }
            }
        )

        actionProcessor
            .send(TestAction.Action1())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `notify State2 when state is State1 and Action2 receives`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.State1())
        val actionProcessor = createTestStateMachineActionProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                updateTo { (action, oldState) ->
                    when (action) {
                        is TestAction.Action1 -> oldState
                        is TestAction.Action2 -> {
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

        actionProcessor
            .send(TestAction.Action2())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State2::class)
        )
    }

    @Test
    fun `notify State1, State2 when state is init and action1 called action2 and invoked action1`() = runTest {
        val state: MutableStateFlow<TestState> = MutableStateFlow(TestState.State1())
        val actionProcessor = createTestStateMachineActionProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestAction, TestState>().apply {
                updateTo { (action) ->
                    when (action) {
                        is TestAction.Action1 -> TestState.State1()
                        is TestAction.Action2 -> TestState.State2()
                    }
                }

                sideEffectBy<TestAction.Action1> { send(TestAction.Action2()) }
            }
        )
        actionProcessor
            .send(TestAction.Action1())
            .join()
        assertThat(
            state.value,
            instanceOf(TestState.State2::class)
        )
    }
}