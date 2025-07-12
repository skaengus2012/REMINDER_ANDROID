/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.statekit.store.androidx.lifecycle

import androidx.lifecycle.ViewModel
import com.nlab.reminder.core.statekit.TestAction
import com.nlab.reminder.core.statekit.TestState
import com.nlab.reminder.core.statekit.plugins.StateKitPlugin
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.store.createStore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.backgroundUnconfinedScope
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import kotlin.coroutines.CoroutineContext

/**
 * @author Doohyun
 */
class StoreFactoriesKtTest {
    @Test
    fun `Given initState, When createStore, Then Store creation success`() {
        val initState = TestState
        val viewModel = object : ViewModel() {}
        viewModel.createStore<TestAction, TestState>(initState = initState)
    }

    @Test
    fun `Given global exception handlers, When occur exception in reduce, Then all global exception handlers occurred`() = runTest {
        suspend fun testOnlyGlobalExceptionHandle(
            vararg mockExceptionHandlers: (CoroutineContext, Throwable) -> Unit,
        ) {
            mockExceptionHandlers.forEach { StateKitPlugin.addGlobalExceptionHandler(it) }
            val viewModel = object : ViewModel() {}
            val store = viewModel.createStore<TestAction, TestState>(
                initState = TestState,
                reduce = DslReduce {
                    effect { throw Exception() }
                }
            )
            store.dispatch(TestAction).join()
            mockExceptionHandlers.forEach { verify(it, once()).invoke(any(), any()) }
            globalExceptionHandlers = emptyList()
        }

        testOnlyGlobalExceptionHandle(mock())
        testOnlyGlobalExceptionHandle(mock(), mock())
    }

    @Test
    fun `Given global exception Handlers and local handler, When occur exception in reduce, Then all error handlers occurred`() = runTest {
        val firstGlobalHandler: (CoroutineContext, Throwable) -> Unit = mock()
        val secondGlobalHandler: (CoroutineContext, Throwable) -> Unit = mock()
        val localHandler: (CoroutineContext, Throwable) -> Unit = mock()
        StateKitPlugin.addGlobalExceptionHandler(firstGlobalHandler)
        StateKitPlugin.addGlobalExceptionHandler(secondGlobalHandler)

        val baseCoroutineScope = backgroundUnconfinedScope + SupervisorJob() + CoroutineExceptionHandler(localHandler)
        val store = createStore<TestAction, TestState>(
            coroutineScope = baseCoroutineScope.toStoreMaterialScope(),
            initState = TestState,
            reduce = DslReduce {
                effect { throw Exception() }
            }
        )
        store.dispatch(TestAction).join()
        verify(firstGlobalHandler, once()).invoke(any(), any())
        verify(secondGlobalHandler, once()).invoke(any(), any())
        verify(localHandler, once()).invoke(any(), any())

        globalExceptionHandlers = emptyList()
    }
}