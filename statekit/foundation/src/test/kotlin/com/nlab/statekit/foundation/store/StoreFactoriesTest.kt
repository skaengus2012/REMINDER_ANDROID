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

package com.nlab.statekit.foundation.store

import com.nlab.statekit.foundation.TestAction
import com.nlab.statekit.foundation.TestState
import com.nlab.statekit.foundation.plugins.GlobalEffect
import com.nlab.statekit.foundation.plugins.GlobalSuspendEffect
import com.nlab.statekit.foundation.plugins.StoreConfiguration
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.Reduce
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class StoreFactoriesTest {
    @Test
    fun `Given store with preferred dispatcher, When dispatch, Then execute on preferred dispatcher`() = runTest {
        val expectedDispatcher = Dispatchers.IO
        val launchOn: (CoroutineDispatcher) -> Unit = mockk(relaxed = true)
        val effect = Effect.SuspendNode<TestAction, TestState> { _, _, _ ->
            launchOn(currentCoroutineContext()[CoroutineDispatcher]!!)
        }
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = this,
            configuration = StoreConfiguration(preferredCoroutineDispatcher = expectedDispatcher)
        )
        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce = Reduce(effect = effect)
        )
        store.dispatch(TestAction).join()
        verify(exactly = 1) { launchOn.invoke(eq(expectedDispatcher)) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Given store without default exception handler, When throwing err on launch, Then throwing err`() = runTest {
        val configuration = StoreConfiguration(
            defaultCoroutineExceptionHandler = null
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = CoroutineScope(Dispatchers.Default),
            configuration = configuration
        )

        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce =  reduceWithErrorEffect()
        )
        store.dispatch(TestAction).join()
    }

    @Test
    fun `Given store with baseScope with handler, When throwing err on launch, Then handler invoked`() = runTest {
        val onError: () -> Unit = mockk(relaxed = true)
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = CoroutineScope(Dispatchers.Default) + CoroutineExceptionHandler { _, _ ->
                onError()
            }
        )
        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce = reduceWithErrorEffect()
        )
        store.dispatch(TestAction).join()
        verify(exactly = 1) { onError.invoke() }
    }

    @Test
    fun `Given store with default exception handler, When throwing err on launch, Then handler invoked`() = runTest {
        val onError: () -> Unit = mockk(relaxed = true)
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            onError()
        }
        val configuration = StoreConfiguration(
            defaultCoroutineExceptionHandler = exceptionHandler
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = CoroutineScope(Dispatchers.Default),
            configuration = configuration
        )

        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce =  reduceWithErrorEffect()
        )
        store.dispatch(TestAction).join()

        verify(exactly = 1) { onError.invoke() }
    }

    @Test
    fun `Given store with local and default handler, When throwing err on launch, Then handler invoked`() = runTest {
        val onError: () -> Unit = mockk(relaxed = true)
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            onError()
        }
        val configuration = StoreConfiguration(
            defaultCoroutineExceptionHandler = exceptionHandler
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = CoroutineScope(Dispatchers.Default) + CoroutineExceptionHandler { _, _ ->
                onError()
            },
            configuration = configuration
        )

        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce =  reduceWithErrorEffect()
        )
        store.dispatch(TestAction).join()

        verify(exactly = 2) { onError.invoke() }
    }

    @Test
    fun `Given store with default effect, When dispatch, Then default effect invoked`() = runTest {
        val globalEffect: GlobalEffect = mockk(relaxed = true)
        val configuration = StoreConfiguration(
            defaultEffects = listOf(globalEffect)
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = this,
            configuration = configuration
        )
        val store = storeMaterialScope.createStore<TestAction, TestState>(initState = TestState)

        store.dispatch(TestAction)
        advanceUntilIdle()

        verify(exactly = 1) { globalEffect.invoke(any(), any()) }
    }

    @Test
    fun `Given store with default suspend effects, When dispatch, Then default effect invoked`() = runTest {
        val globalEffect: GlobalSuspendEffect = mockk(relaxed = true)
        val configuration = StoreConfiguration(
            defaultSuspendEffects = listOf(globalEffect, globalEffect)
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = this,
            configuration = configuration
        )
        val store = storeMaterialScope.createStore<TestAction, TestState>(initState = TestState)

        store.dispatch(TestAction)
        advanceUntilIdle()

        coVerify(exactly = 2) { globalEffect.invoke(any(), any()) }
    }

    @Test
    fun `Given store with effect and global effects, When dispatch, Then all effects invoked`() = runTest {
        val handler: () -> Unit = mockk(relaxed = true)
        val globalEffect = GlobalEffect { _, _ -> handler() }
        val configuration = StoreConfiguration(
            defaultEffects = listOf(globalEffect)
        )
        val storeMaterialScope = StoreMaterialScope(
            baseCoroutineScope = this,
            configuration = configuration
        )
        val store = storeMaterialScope.createStore(
            initState = TestState,
            reduce = Reduce<TestAction, TestState>(
                effect = Effect.Node { _, _ -> handler() }
            )
        )

        store.dispatch(TestAction)
        advanceUntilIdle()

        verify(exactly = 2) { handler.invoke() }
    }
}

private fun reduceWithErrorEffect() = Reduce<TestAction, TestState>(
    effect = Effect.Node { _, _ -> error("err") }
)