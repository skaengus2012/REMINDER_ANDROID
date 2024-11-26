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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
    fun `Given node effects, when launch, Then effect invoked all asynchronously`() = runTest {
        val runners: List<() -> Unit> = List(5) { mock() }
        val effect = TestEffectComposite(
            TestEffectComposite(
                TestEffectNode { _, _, _ ->
                    delay(1000)
                    runners[0].invoke()
                },
                TestEffectNode { _, _, _ ->
                    delay(1500)
                    runners[1].invoke()
                },
                TestEffectNode { _, _, _ ->
                    delay(500)
                    runners[2].invoke()
                }
            ),
            TestEffectComposite(
                TestEffectNode { _, _, _ ->
                    delay(800)
                    runners[3].invoke()
                }
            ),
            TestEffectNode { _, _, _ ->
                delay(900)
                runners[4].invoke()
            }
        )
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            actionDispatcher = mock(),
            accumulatorPool = AccumulatorPool(),
            coroutineScope = this,
        )
        advanceTimeBy(2000)
        runners.forEach { verify(it, once()).invoke() }
    }

    @Test
    fun `Given two lifecycle node, When launch, Then effect invoked all in order`() {
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
        val firstEffect: TestEffectLifecycleNode = mock()
        val secondEffect: TestEffectLifecycleNode = mock()
        Effect.Composite(firstEffect, secondEffect).launch(
            inputAction,
            inputState,
            actionDispatcher = fakeActionDispatcher,
            accumulatorPool = accPool,
            coroutineScope = fakeCoroutineScope,
        )
        inOrder(firstEffect, secondEffect) {
            verify(firstEffect, once()).invoke(
                inputAction,
                inputState,
                fakeActionDispatcher,
                fakeCoroutineScope,
                accPool
            )
            verify(secondEffect, once()).invoke(
                inputAction,
                inputState,
                fakeActionDispatcher,
                fakeCoroutineScope,
                accPool
            )
        }
    }
}