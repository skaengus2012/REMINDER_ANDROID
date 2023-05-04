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

package com.nlab.statekit.middleware.interceptor.dsl

import com.nlab.statekit.*
import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class DslInterceptorTest {
    @Test
    fun testDefineDSL() = runTest {
        val dispatchAction = TestAction.genAction()
        val mockActionDispatcher: ActionDispatcher<TestAction> = mock()
        val interceptor = DslInterceptor<TestAction, TestState>(
            defineDSL = {
                action<TestAction.Action1> {
                    state<TestState.State1> { dispatch(dispatchAction) }
                }
            }
        )

        interceptor(mockActionDispatcher, UpdateSource(TestAction.Action1, TestState.State1))
        verify(mockActionDispatcher).dispatch(dispatchAction)
    }
}