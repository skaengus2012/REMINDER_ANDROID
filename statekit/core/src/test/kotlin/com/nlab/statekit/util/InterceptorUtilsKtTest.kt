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

import com.nlab.statekit.*
import com.nlab.statekit.UpdateSource
import org.mockito.kotlin.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class InterceptorUtilsKtTest {
    @Test
    fun testBuildInterceptor() = runTest {
        val work: () -> Unit = mock()
        val interceptor = buildInterceptor<TestAction, TestState> { work() }

        interceptor.invoke(mock(), UpdateSource(TestAction.genAction(), TestState.genState()))
        verify(work, once())()
    }

    @Test
    fun testBuildDslInterceptor() = runTest {
        val work: () -> Unit = mock()
        val interceptor = buildDslInterceptor<TestAction, TestState> {
            anyState {
                anyAction { work() }
            }
        }

        interceptor.invoke(mock(), UpdateSource(TestAction.genAction(), TestState.genState()))
        verify(work, once())()
    }

    @Test
    fun testInterceptorComposition() = runTest {
        val firstWork: () -> Unit = mock()
        val lastWork: () -> Unit = mock()

        val firstInterceptor = buildInterceptor<TestAction, TestState> { firstWork() }
        val lastInterceptor = buildInterceptor<TestAction, TestState> { lastWork() }
        val compositeInterceptor = firstInterceptor + lastInterceptor

        compositeInterceptor.invoke(mock(), UpdateSource(TestAction.genAction(), TestState.genState()))
        verify(firstWork, once())()
        verify(lastWork, once())()
    }
}