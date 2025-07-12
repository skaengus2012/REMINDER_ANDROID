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
import com.nlab.statekit.dispatch.ActionDispatcher
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class EffectKtTest {
    @Test
    fun `Given 4 syncable composite nodes, When launch, Then effect invoked all in order`() {
        val firstEffect: TestEffectNode = mockk(relaxed = true)
        val secondEffect: TestEffectLifecycleNode = mockk(relaxed = true)
        val thirdEffect: TestEffectLifecycleNode = mockk(relaxed = true)
        val fourthEffect: TestEffectNode = mockk(relaxed = true)
        val compositeEffect = TestEffectComposite(
            head = firstEffect,
            tails = listOf(secondEffect, thirdEffect, fourthEffect)
        )

        val inputAction = TestAction.genAction()
        val inputState = TestState.genState()
        val effectContext = EffectContext(
            coroutineScope = mockk(),
            nodeStackPool = NodeStackPool(),
            throwableCollector = mockk()
        )
        val actionDispatcher: ActionDispatcher<TestAction> = mockk(relaxed = true)

        compositeEffect.launch(
            action = inputAction,
            current = inputState,
            context = effectContext,
            actionDispatcher = actionDispatcher
        )

        verifyOrder {
            firstEffect.invoke(action = inputAction, current = inputState)
            secondEffect.invoke(
                action = inputAction,
                current = inputState,
                context = effectContext,
                actionDispatcher = actionDispatcher
            )
            thirdEffect.invoke(
                action = inputAction,
                current = inputState,
                context = effectContext,
                actionDispatcher = actionDispatcher
            )
            fourthEffect.invoke(action = inputAction, current = inputState)
        }
    }

    @Test
    fun `Given suspend composite nodes, When launch, Then effect invoked all asynchronously`() = runTest {
        val runners: List<suspend () -> Unit> = List(5) { mockk(relaxed = true) }
        val compositeEffect = TestEffectComposite(
            head = TestEffectComposite(
                head = TestEffectSuspendNode { _, _, _ ->
                    delay(1000)
                    runners[0].invoke()
                },
                tails = listOf(
                    TestEffectSuspendNode { _, _, _ ->
                        delay(1500)
                        runners[1].invoke()
                    },
                    TestEffectSuspendNode { _, _, _ ->
                        delay(500)
                        runners[2].invoke()
                    }
                ),
            ),
            tails = listOf(
                TestEffectComposite(
                    head = TestEffectSuspendNode { _, _, _ ->
                        delay(800)
                        runners[3].invoke()
                    },
                    tails = listOf(
                        TestEffectSuspendNode { _, _, _ ->
                            delay(900)
                            runners[4].invoke()
                        }
                    )
                )
            )
        )

        compositeEffect.launch(
            action = TestAction.genAction(),
            current = TestState.genState(),
            context = EffectContext(
                coroutineScope = this,
                nodeStackPool = NodeStackPool(),
                throwableCollector = mockk()
            ),
            actionDispatcher = mockk()
        )
        advanceTimeBy(2000)

        runners.forEach { runner ->
            coVerify(exactly = 1) { runner.invoke() }
        }
    }

    @Test
    fun `Given throwable effect node, When effect launched, Then exception collected`() {
        val throwable = RuntimeException()
        val errorEffect = TestEffectNode { _, _ -> throw throwable }
        val throwableCollector: ThrowableCollector = mockk(relaxed = true)

        errorEffect.launch(
            action = TestAction.genAction(),
            current = TestState.genState(),
            context = EffectContext(
                coroutineScope = mockk(),
                nodeStackPool = NodeStackPool(),
                throwableCollector = throwableCollector
            ),
            actionDispatcher = mockk()
        )

        verify(exactly = 1) { throwableCollector.collect(throwable) }
    }

    @Test
    fun `Given throwable suspend effect node, When effect launched, Then exception collected`() = runTest {
        val throwable = RuntimeException()
        val effect = TestEffectSuspendNode { _, _, _ -> throw throwable }
        val throwableCollector: ThrowableCollector = mockk(relaxed = true)

        effect.launch(
            action = TestAction.genAction(),
            current = TestState.genState(),
            context = EffectContext(
                coroutineScope = this,
                nodeStackPool = NodeStackPool(),
                throwableCollector = throwableCollector
            ),
            actionDispatcher = mockk()
        )
        advanceUntilIdle()

        verify(exactly = 1) { throwableCollector.collect(throwable) }
    }
}