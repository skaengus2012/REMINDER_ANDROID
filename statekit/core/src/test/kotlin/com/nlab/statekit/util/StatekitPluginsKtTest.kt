/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.util

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.mockito.kotlin.once
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@ExperimentalCoroutinesApi
internal class StatekitPluginsKtTest {
    private lateinit var globalExceptionHandler: (Throwable) -> Unit

    @Before
    fun setup() = runTest {
        globalExceptionHandler = mock()
        StatekitPlugin.configureGlobalExceptionHandler(CoroutineExceptionHandler { _, throwable ->
            globalExceptionHandler(throwable)
        })
    }

    @After
    fun tearDown() {
        StatekitPlugin.configureGlobalExceptionHandler(null)
    }

    @Test
    fun `GlobalExceptionHandler invoked, when interceptor occurred exception`() = runTest {
        val expectedThrowable = Throwable()
        val store = createStore<TestAction, TestState>(
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            initState = TestState.genState(),
            interceptor = buildInterceptor { throw expectedThrowable }
        )
        store.dispatch(TestAction.genAction()).join()
        verify(globalExceptionHandler, once()).invoke(expectedThrowable)
    }

    @Test
    fun `GlobalExceptionHandler invoked with LocalExceptionHandler, when interceptor occurred exception`() = runTest {
        val expectedThrowable = Throwable()
        val localExceptionHandler: (Throwable) -> Unit = mock()
        val store = createStore<TestAction, TestState>(
            coroutineScope = CoroutineScope(Dispatchers.Unconfined) + CoroutineExceptionHandler { _, throwable ->
                localExceptionHandler(throwable)
            },
            initState = TestState.genState(),
            interceptor = buildInterceptor { throw expectedThrowable }
        )
        store.dispatch(TestAction.genAction()).join()
        verify(globalExceptionHandler, once()).invoke(expectedThrowable)
        verify(localExceptionHandler, once()).invoke(expectedThrowable)
    }
}