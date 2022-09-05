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
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.coroutines.ContinuationInterceptor

/**
 * @author thalys
 */
@OptIn(
    ExperimentalCoroutinesApi::class,
    DelicateCoroutinesApi::class
)
class StateMachinePluginTest {
    private val defaultDispatcher = newSingleThreadContext("SingleDispatcher")
    private val defaultExceptionHandler: (Throwable) -> Unit = mock()

    @Before
    fun setup() {
        StateMachinePlugin.defaultDispatcher = defaultDispatcher
        StateMachinePlugin.defaultExceptionHandler = defaultExceptionHandler
    }

    @After
    fun tearDown() {
        StateMachinePlugin.defaultDispatcher = null
        StateMachinePlugin.defaultExceptionHandler = null
    }

    @Test
    fun `handled by defaultExceptionHandler when error occurred`() = runTest {
        val (exception, stateMachine) = genThrowableAndStateMachine()

        stateMachine
            .controlIn(CoroutineScope(Dispatchers.Unconfined), TestState.StateInit())
            .send(TestEvent.Event1())
            .join()
        verify(defaultExceptionHandler, once())(exception)
    }

    @Test
    fun `never invoked defaultExceptionHandler when PluginErrorHandler disabled`() = runTest {
        val (exception, stateMachine) = genThrowableAndStateMachine()

        stateMachine
            .controlIn(
                CoroutineScope(Dispatchers.Unconfined),
                TestState.StateInit(),
                StateControllerConfig(isPluginErrorHandlerEnabled = false)
            )
            .send(TestEvent.Event1())
            .join()
        verify(defaultExceptionHandler, never())(exception)
    }

    private fun genThrowableAndStateMachine(): Pair<Throwable, StateMachine<TestEvent, TestState>> {
        val exception = Throwable()
        return exception to StateMachine {
            sideEffect { throw exception }
        }
    }

    @Test
    fun `handled on defaultDispatcher`() = runTest {
        val (result, stateMachine) = genIsContextEqualsAndStateMachine()

        stateMachine.controlIn(CoroutineScope(Dispatchers.Unconfined), TestState.StateInit())
            .send(TestEvent.Event1())
            .join()
        assertThat(result.await(), equalTo(true))
    }

    @Test
    fun `not handled on testDispatcher when PluginDispatcher disabled`() = runTest {
        val (result, stateMachine) = genIsContextEqualsAndStateMachine()

        stateMachine
            .controlIn(
                CoroutineScope(Dispatchers.Unconfined),
                TestState.StateInit(),
                StateControllerConfig(isPluginDispatcherEnabled = false)
            )
            .send(TestEvent.Event1())
            .join()
        assertThat(result.await(), equalTo(false))
    }

    private fun genIsContextEqualsAndStateMachine(): Pair<Deferred<Boolean>, StateMachine<TestEvent, TestState>> {
        val result = CompletableDeferred<Boolean>()
        return result to StateMachine {
            sideEffect {
                val curContext = coroutineScope {
                    coroutineContext[ContinuationInterceptor]
                }
                val expectedContext = withContext(defaultDispatcher) {
                    coroutineContext[ContinuationInterceptor]
                }
                result.complete(curContext === expectedContext)
            }
        }
    }
}