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

import com.nlab.reminder.test.genInt
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineEventProcessorTest {
    private fun createTestStateMachineEventProcessor(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit()),
        stateMachineBuilder: StateMachineBuilder<TestEvent, TestState> = StateMachineBuilder()
    ): EventProcessor<TestEvent> = StateMachineEventProcessor(scope, state, stateMachineBuilder)

    @Test
    fun `update to state1 when test1 event sent`() = runTest {
        val expectedNextState = TestState.State1()
        val state = MutableStateFlow<TestState>(TestState.StateInit())
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
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
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
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
        val sideEffectConfig: (StateMachineBuilder<TestEvent, TestState>).() -> Unit = {
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
        val sideEffectConfig: (StateMachineBuilder<TestEvent, TestState>).() -> Unit = {
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
        handleConfig: (StateMachineBuilder<TestEvent, TestState>).() -> Unit,
    ) = runTest {
        val eventProcessor = createTestStateMachineEventProcessor(
            state = state,
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply { handleConfig(this) }
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
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
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
    }

    @Test
    fun `catch when trying to update with testEvent1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.update { (event, before) ->
                when(event) {
                    is TestEvent.Event1 -> throw throwable
                    is TestEvent.Event2 -> before
                }
            }
        }
    }

    @Test
    fun `catch when sideEffect executed by testEvent1`() = runTest {
        testExceptionHandler { stateMachineBuilder, throwable ->
            stateMachineBuilder.handleBy<TestEvent.Event1> { throw throwable }
        }
    }

    private suspend fun testExceptionHandler(
        throwableConfig: (StateMachineBuilder<TestEvent, TestState>, Throwable) -> Unit
    ) {
        val exception = Throwable()
        val firstErrorHandler: (Throwable) -> Unit = mock()
        val secondErrorHandler: (Throwable) -> Unit = mock()
        val eventProcessor = createTestStateMachineEventProcessor(
            stateMachineBuilder = StateMachineBuilder<TestEvent, TestState>().apply {
                catch { e -> firstErrorHandler(e) }
                catch { e -> secondErrorHandler(e) }
                throwableConfig(this, exception)
            }
        )

        eventProcessor
            .send(TestEvent.Event1())
            .join()
        verify(firstErrorHandler, once())(exception)
        verify(secondErrorHandler, once())(exception)
    }
}