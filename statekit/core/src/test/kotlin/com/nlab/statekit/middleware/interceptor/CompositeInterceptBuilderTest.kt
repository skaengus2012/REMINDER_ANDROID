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

package com.nlab.statekit.middleware.interceptor

import com.nlab.statekit.*
import com.nlab.statekit.middleware.interceptor.dsl.InterceptEndScope
import org.mockito.kotlin.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class CompositeInterceptBuilderTest {
    @Test
    fun `Intercepted with async, when two types strategies registered`() = runTest {
        val interceptBuilder = CompositeInterceptBuilder<TestAction, TestAction, TestState>()
        val firstStrategy: () -> Unit = mock()
        val lastStrategy: () -> Unit = mock()
        interceptBuilder.add {
            delay(1_000)
            firstStrategy()
        }
        interceptBuilder.add {
            delay(1_000)
            lastStrategy()
        }
        val interceptor = interceptBuilder.build()
        interceptor(InterceptEndScope(mock()), UpdateSource(TestAction.genAction(), TestState.genState()))
        advanceTimeBy(1_100)
        verify(firstStrategy, once())()
        verify(lastStrategy, once())()
    }
}