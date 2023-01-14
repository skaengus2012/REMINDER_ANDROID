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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.EventProcessor
import com.nlab.reminder.core.state.StateMachineHandleScope
import com.nlab.reminder.core.state.TestEvent
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HandleScopeFunctionsKtTest {
    @Test
    fun generateHandleScopeFunction() = runTest {
        val eventProcessor: EventProcessor<TestEvent> = mock()
        val scope = StateMachineHandleScope(
            mock(),
            eventProcessor
        )
        val operator: (StateMachineHandleScope<TestEvent>) -> Unit = mock()
        val f = generateHandleScopeFunction {
            operator(this)
        }
        f(scope)

        verify(operator, once())(scope)
    }
}