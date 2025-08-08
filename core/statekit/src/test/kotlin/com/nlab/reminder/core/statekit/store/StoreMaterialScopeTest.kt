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

package com.nlab.reminder.core.statekit.store

import com.nlab.testkit.faker.genInt
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author Doohyun
 */
class StoreMaterialScopeTest {
    @Test
    fun `Given empty global handlers, When create store material scope, Then scope has not handler`() {
        val storeMaterialScope = CoroutineScope(EmptyCoroutineContext).toStoreMaterialScope()
        val actual = storeMaterialScope.coroutineContext[CoroutineExceptionHandler]
        assertThat(actual, nullValue())
    }

    @Test
    fun `Given single global handler, When create store material scope, Then scope has handler`() {
        val globalHandler = CoroutineExceptionHandler { _, _ -> }
        globalExceptionHandlers = listOf(globalHandler)

        val storeMaterialScope = CoroutineScope(EmptyCoroutineContext).toStoreMaterialScope()
        val actual = storeMaterialScope.coroutineContext[CoroutineExceptionHandler]
        assertThat(actual, sameInstance(globalHandler))

        globalExceptionHandlers = emptyList()
    }

    @Test
    fun `Given store with many global handlers, When throwing err on launch, Then all handlers invoked`() = runTest {
        val globalExceptionRunner: (Throwable) -> Unit = mockk(relaxed = true)
        globalExceptionHandlers = List(size = genInt(min = 2, max = 10)) {
            CoroutineExceptionHandler { _, t -> globalExceptionRunner(t) }
        }
        val expectThrowable = Throwable()
        val storeMaterialScope = CoroutineScope(EmptyCoroutineContext).toStoreMaterialScope()
        storeMaterialScope
            .launch { throw expectThrowable }
            .join()

        verify(exactly = globalExceptionHandlers.size) {
            globalExceptionRunner(expectThrowable)
        }

        globalExceptionHandlers = emptyList()
    }

    @Test
    fun `Given store with global, local handlers, When throwing err on launch, Then all handlers invoked`() = runTest {
        val globalExceptionRunner: (Throwable) -> Unit = mockk(relaxed = true)
        val localExceptionRunner: (Throwable) -> Unit = mockk(relaxed = true)
        globalExceptionHandlers = listOf(
            CoroutineExceptionHandler { _, t -> globalExceptionRunner(t) }
        )
        val targetCoroutineScope = CoroutineScope(EmptyCoroutineContext) + CoroutineExceptionHandler { _, t ->
            localExceptionRunner.invoke(t)
        }
        val storeMaterialScope = targetCoroutineScope.toStoreMaterialScope()
        val expectThrowable = Throwable()

        storeMaterialScope
            .launch { throw expectThrowable }
            .join()

        verify(exactly = 1) {
            globalExceptionRunner(expectThrowable)
        }
        verify(exactly = 1) {
            localExceptionRunner(expectThrowable)
        }
        globalExceptionHandlers = emptyList()
    }
}