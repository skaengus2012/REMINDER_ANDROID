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

import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.coroutines.ContinuationInterceptor

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
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
        val exception = Throwable()
        val eventProcessor = StateMachineEventProcessor(
            CoroutineScope(Dispatchers.Unconfined),
            MutableStateFlow(TestState.StateInit()),
            StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffect { throw exception }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()
        verify(defaultExceptionHandler, once())(exception)
    }

    @Test
    fun `handled on defaultDispatcher`() = runTest {
        var isContextEquals = false
        val eventProcessor = StateMachineEventProcessor(
            CoroutineScope(Dispatchers.Unconfined),
            MutableStateFlow(TestState.StateInit()),
            StateMachineBuilder<TestEvent, TestState>().apply {
                sideEffect {
                    val curContext = coroutineScope {
                        coroutineContext[ContinuationInterceptor]
                    }
                    val expectedContext = withContext(defaultDispatcher) {
                        coroutineContext[ContinuationInterceptor]
                    }
                    isContextEquals = (curContext === expectedContext)
                }
            }
        )
        eventProcessor
            .send(TestEvent.Event1())
            .join()
        assertThat(isContextEquals, equalTo(true))
    }
}