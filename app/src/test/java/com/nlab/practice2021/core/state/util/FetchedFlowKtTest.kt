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

package com.nlab.practice2021.core.state.util

import com.nlab.practice2021.core.state.TestState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
class FetchedFlowKtTest {
    @Test
    fun `fetch called never when value was invoked`() {
        val coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        val initState = TestState.StateInit()
        val fetchUseCase: () -> Unit = mock()
        val stateFlow: StateFlow<TestState> = MutableStateFlow(initState).fetchedFlow(coroutineScope) { fetchUseCase() }
        assertThat(
            stateFlow.value,
            equalTo(initState)
        )
        verify(fetchUseCase, never())()
    }

    @Test
    fun `fetch called once when value was invoked`() {
        val coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        val fetchUseCase: () -> Unit = mock()
        val stateFlow: StateFlow<TestState> =
            MutableStateFlow(TestState.StateInit()).fetchedFlow(coroutineScope) { fetchUseCase() }
        coroutineScope.launch { stateFlow.collect() }
        coroutineScope.launch { stateFlow.collect() }
        verify(fetchUseCase, times(1))()
    }
}