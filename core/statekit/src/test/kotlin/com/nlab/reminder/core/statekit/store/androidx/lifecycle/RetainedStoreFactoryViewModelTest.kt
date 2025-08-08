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
import com.nlab.reminder.core.statekit.store.globalExceptionHandlers
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.createStore
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class RetainedStoreFactoryViewModelTest {
    @Test
    fun `Given global handler, When launch on store material scope, Then handler invoked`() = runTest {
        val globalHandler: (Throwable) -> Unit = mockk(relaxed = true)
        globalExceptionHandlers = listOf(
            CoroutineExceptionHandler { _, t -> globalHandler.invoke(t) }
        )
        val viewModel = RetainedStoreFactoryViewModel()
        val store = viewModel.getOrPut(key = Any()) { scope ->
            createStore(
                coroutineScope = scope.storeMaterialScope,
                initState = TestState,
                reduce = Reduce(
                    effect = Effect.Node { action, state ->
                        throw IllegalStateException()
                    }
                )
            )
        }
        store.dispatch(TestAction)
            .join()

        verify(exactly = 1) {
            globalHandler.invoke(any())
        }
        globalExceptionHandlers = emptyList()
    }
}