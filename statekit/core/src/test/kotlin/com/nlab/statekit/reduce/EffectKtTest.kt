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

package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import kotlin.coroutines.CoroutineContext

/**
 * @author Thalys
 */
class EffectKtTest {
    @Test
    fun `Given 4 syncable nodes, When launch, Then effect invoked all in order`() {
        val firstEffect: TestEffectNode = mock()
        val secondEffect: TestEffectLifecycleNode = mock()
        val thirdEffect: TestEffectLifecycleNode = mock()
        val fourthEffect: TestEffectNode = mock()
        val inputAction = TestAction.genAction()
        val inputState = TestState.genState()
        val fakeActionDispatcher: ActionDispatcher<TestAction> = object : ActionDispatcher<TestAction> {
            override suspend fun dispatch(action: TestAction) = Unit
        }
        val fakeCoroutineScope: CoroutineScope = object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = error("Fake coroutine scope does not have a coroutine context.")
        }
        val accPool = AccumulatorPool()
        TestEffectComposite(firstEffect, secondEffect, thirdEffect, fourthEffect).launch(
            inputAction,
            inputState,
            actionDispatcher = fakeActionDispatcher,
            accPool = accPool,
            coroutineScope = fakeCoroutineScope,
        )

        inOrder(firstEffect, secondEffect, thirdEffect, fourthEffect) {
            verify(firstEffect, once()).invoke(
                inputAction,
                inputState
            )
            verify(secondEffect, once()).invoke(
                inputAction,
                inputState,
                fakeActionDispatcher,
                accPool,
                fakeCoroutineScope
            )
            verify(thirdEffect, once()).invoke(
                inputAction,
                inputState,
                fakeActionDispatcher,
                accPool,
                fakeCoroutineScope
            )
            verify(fourthEffect, once()).invoke(
                inputAction,
                inputState
            )
        }
    }

    @Test
    fun `Given suspend node effects, when launch, Then effect invoked all asynchronously`() = runTest {
        val runners: List<() -> Unit> = List(5) { mock() }
        val effect = TestEffectComposite(
            TestEffectComposite(
                TestEffectSuspendNode { _, _, _ ->
                    delay(1000)
                    runners[0].invoke()
                },
                TestEffectSuspendNode { _, _, _ ->
                    delay(1500)
                    runners[1].invoke()
                },
                TestEffectSuspendNode { _, _, _ ->
                    delay(500)
                    runners[2].invoke()
                }
            ),
            TestEffectComposite(
                TestEffectSuspendNode { _, _, _ ->
                    delay(800)
                    runners[3].invoke()
                }
            ),
            TestEffectSuspendNode { _, _, _ ->
                delay(900)
                runners[4].invoke()
            }
        )
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accPool = AccumulatorPool(),
            coroutineScope = this,
        )
        advanceTimeBy(2000)
        runners.forEach { verify(it, once()).invoke() }
    }

    @Test(expected = RuntimeException::class)
    fun `Given throwable effect nodes, When effect launched, Then exception be thrown`() {
        val effect = TestEffectNode { _, _ -> throw RuntimeException() }
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accPool = AccumulatorPool(),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
        )
    }

    @Test
    fun `Given throwable effect nodes and exceptionHandler, When effect launched, Then exception be thrown to exceptionHandler`() {
        val exceptionBlock: () -> Unit = mock()
        val effect = TestEffectNode { _, _ -> throw RuntimeException() }
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accPool = AccumulatorPool(),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined) + CoroutineExceptionHandler { _, _ ->
                exceptionBlock()
            }
        )
        verify(exceptionBlock, once()).invoke()
    }

    @Test(expected = RuntimeException::class)
    fun `Given throwable suspend effect nodes, When effect launched, Then exception be thrown`() = runTest {
        val effect = TestEffectSuspendNode { _, _, _ -> throw RuntimeException() }
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accPool = AccumulatorPool(),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
        )
    }

    @Test
    fun `Given throwable suspend effect nodes and exceptionHandler, When effect launched, Then exception be thrown to exceptionHandler`() = runTest {
        val effect = TestEffectSuspendNode { _, _, _ -> throw RuntimeException() }
        val exceptionBlock: () -> Unit = mock()
        val superJob = SupervisorJob()
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accPool = AccumulatorPool(),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined) + superJob + CoroutineExceptionHandler { _, _ ->
                exceptionBlock()
                superJob.cancel()
            }
        )
        superJob.join()
        verify(exceptionBlock, once()).invoke()
    }
}