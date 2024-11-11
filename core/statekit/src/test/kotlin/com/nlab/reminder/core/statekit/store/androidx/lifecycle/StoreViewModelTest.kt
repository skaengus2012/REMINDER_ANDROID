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

import com.nlab.reminder.core.statekit.TestAction
import com.nlab.reminder.core.statekit.TestState
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.store.Store
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StoreViewModelTest {
    @Test
    fun `Given initState, When create ViewModel with initState, Then uiState saved initState`() {
        val initState = TestState
        val viewModel = object : StoreViewModel<TestAction, TestState>() {
            override fun onCreateStore(): Store<TestAction, TestState> = createStore(initState)
        }
        assertThat(viewModel.uiState.value, equalTo(initState))
    }

    @Test
    fun `Given action and reduce with effect, When dispatch, Then effect occurred`() = runTest {
        val action = TestAction
        val runner: () -> Unit = mock()
        val reduce = DslReduce<TestAction, TestState> {
            effect { runner.invoke() }
        }
        val viewModel = object : StoreViewModel<TestAction, TestState>() {
            override fun onCreateStore() = createStore(
                initState = TestState,
                reduce = reduce
            )
        }
        viewModel.dispatch(action).join()
        verify(runner, once()).invoke()
    }
}