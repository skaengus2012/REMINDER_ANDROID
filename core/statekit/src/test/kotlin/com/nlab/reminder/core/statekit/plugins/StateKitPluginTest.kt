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

package com.nlab.reminder.core.statekit.plugins

import com.nlab.reminder.core.statekit.store.androidx.lifecycle.globalExceptionHandlers
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import kotlin.coroutines.CoroutineContext

/**
 * @author Doohyun
 */
class StateKitPluginTest {
    @Test
    fun `Given exception handle block, When invoke globalExceptionHandlers after addGlobalExceptionHandler, Then exception handler invoked`() = runTest {
        val exceptionHandler: (CoroutineContext, Throwable) -> Unit = mockk(relaxed = true)
        StateKitPlugin.addGlobalExceptionHandler(exceptionHandler)

        globalExceptionHandlers.first().handleException(coroutineContext, RuntimeException())
        verify(exactly = 1) {
            exceptionHandler.invoke(any(), any())

        }
    }

    @After
    fun tearDown() {
        globalExceptionHandlers = emptyList()
    }
}