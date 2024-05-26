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

package com.nlab.statekit.store

import com.nlab.statekit.*
import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import org.mockito.kotlin.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*


/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class DefaultStoreTest {
    @Test
    fun `Store should be dispatched with mock dispatcher`() = runTest {
        val input = TestAction.genAction()
        val mockActionDispatcher: ActionDispatcher<TestAction> = mock()
        val store = DefaultStore<TestAction, TestState>(
            state = mock(),
            coroutineScope = this,
            actionDispatcher = mockActionDispatcher,
            initJobs = emptyList()
        )

        store.dispatch(input).join()
        verify(mockActionDispatcher, once()).dispatch(input)
    }

    @Test
    fun `State is initialized correctly`() {
        val expected = MutableStateFlow(TestState.genState())
        val actual = DefaultStore<TestAction, TestState>(expected, mock(), mock(), initJobs = emptyList()).state

        assert(expected === actual)
    }
}