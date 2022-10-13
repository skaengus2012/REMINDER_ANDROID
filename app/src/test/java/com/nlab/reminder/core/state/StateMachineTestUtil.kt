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

import com.nlab.reminder.test.genStateContainerScope
import com.nlab.reminder.test.once
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
internal fun genTestStateMachineEventProcessor(
    state: MutableStateFlow<TestState> = MutableStateFlow(TestState.StateInit()),
    stateMachine: StateMachine<TestEvent, TestState> = StateMachine(),
    scope: CoroutineScope = genStateContainerScope()
): EventProcessor<TestEvent> = StateMachineEventProcessor(scope, state, stateMachine)

internal suspend fun testReduceTemplate(
    input: TestEvent = TestEvent.genEvent(),
    initState: TestState = TestState.genState(),
    expectedState: TestState = TestState.genState(),
    block: StateMachine<TestEvent, TestState>.() -> Unit = {},
) {
    val state = MutableStateFlow(initState)
    genTestStateMachineEventProcessor(state, StateMachine<TestEvent, TestState>().apply(block))
        .send(input)
        .join()
    assertThat(state.value, equalTo(expectedState))
}

internal suspend fun testOnceHandleTemplate(
    input: TestEvent = TestEvent.genEvent(),
    initState: TestState = TestState.genState(),
    block: StateMachine<TestEvent, TestState>.(() -> Unit) -> Unit = {},
) {
    val state = MutableStateFlow(initState)
    val action: () -> Unit = mock()
    genTestStateMachineEventProcessor(state, StateMachine<TestEvent, TestState>().apply { block(action) })
        .send(input)
        .join()
    verify(action, once())()
}

internal suspend fun testNeverHandleTemplate(
    input: TestEvent = TestEvent.genEvent(),
    initState: TestState = TestState.genState(),
    block: StateMachine<TestEvent, TestState>.(() -> Unit) -> Unit = {},
) {
    val state = MutableStateFlow(initState)
    val action: () -> Unit = mock()
    genTestStateMachineEventProcessor(state, StateMachine<TestEvent, TestState>().apply { block(action) })
        .send(input)
        .join()
    verify(action, never())()
}

internal suspend fun testCatchTemplate(
    input: TestEvent = TestEvent.genEvent(),
    block: StateMachine<TestEvent, TestState>.(Throwable) -> Unit
) {
    val exception = Throwable()
    val firstErrorHandler: (Throwable) -> Unit = mock()
    val secondErrorHandler: (Throwable) -> Unit = mock()
    val eventProcessor = genTestStateMachineEventProcessor(
        stateMachine = StateMachine<TestEvent, TestState>().apply {
            catch { e -> firstErrorHandler(e) }
            catch { e -> secondErrorHandler(e) }
            block(exception)
        }
    )

    eventProcessor
        .send(input)
        .join()
    verify(firstErrorHandler, once())(exception)
    verify(secondErrorHandler, once())(exception)
}
