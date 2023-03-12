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

package com.nlab.reminder.core.state2.store

import com.nlab.reminder.core.state2.ActionDispatcher
import com.nlab.reminder.core.state2.TestAction
import com.nlab.reminder.core.state2.TestState
import com.nlab.testkit.once
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.mockito.kotlin.*


/**
 * @author thalys
 */
class DefaultStoreTest {
    @Test
    fun `ActionDispatcher dispatch called correctly`() {
        val actionDispatcher: ActionDispatcher<TestAction> = mock {
            whenever(mock.dispatch(any())) doReturn Job()
        }

        DefaultStore<TestAction, TestState>(actionDispatcher, mock()).dispatch(TestAction.genAction())
        verify(actionDispatcher, once()).dispatch(any())
    }

    @Test
    fun `State is initialized correctly`() {
        val expected = MutableStateFlow(TestState.genState())
        val actual = DefaultStore<TestAction, TestState>(mock(), expected).state

        assert(expected === actual)
    }
}