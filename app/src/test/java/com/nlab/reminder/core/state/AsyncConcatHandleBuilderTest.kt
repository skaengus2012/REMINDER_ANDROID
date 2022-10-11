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

package com.nlab.reminder.core.state

import com.nlab.reminder.test.once
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
class AsyncConcatHandleBuilderTest {
    @Test
    fun `handled with async when two types strategies registered`() = runTest {
        val concatHandleBuilder = AsyncConcatHandleBuilder<TestEvent, TestEvent, TestState>()
        val action1: () -> Unit = mock()
        val action2: () -> Unit = mock()
        concatHandleBuilder.add {
            delay(1_000)
            action1()
        }
        concatHandleBuilder.add {
            delay(1_000)
            action2()
        }

        val action = concatHandleBuilder.build()
        action.invoke(StateMachineHandleScope(mock(), mock()), mock())
        advanceTimeBy(1_100)
        verify(action1, once())()
        verify(action2, once())()
    }
}