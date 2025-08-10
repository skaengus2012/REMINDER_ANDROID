/*
 * Copyright (C) 2025 The N's lab Open Source Project
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
import com.nlab.statekit.store.Store
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author Thalys
 */
class RetainedStoreFactoryViewModelTest {
    @Test
    fun `Given key and factory function, When getOrPut 2 times, Then store created just once`() {
        val key = Any()
        val fakeStore: Store<TestAction, TestState> = mockk()
        val factoryFunc: () -> Store<TestAction, TestState> = mockk {
            every { this@mockk() } returns fakeStore
        }
        val viewModel = RetainedStoreFactoryViewModel()

        repeat(times = 2) {
            viewModel.getOrPut(key, { factoryFunc.invoke() })
        }

        verify(exactly = 1) { factoryFunc.invoke() }
    }
}