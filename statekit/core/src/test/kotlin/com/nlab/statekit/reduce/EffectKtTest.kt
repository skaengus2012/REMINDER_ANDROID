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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Thalys
 */
class EffectKtTest {
    @Test
    fun `Given not matched effects, When launch, Then effect never invoked`() = runTest {
        val runnable: () -> Unit = mock()
        val effect = TestNodeEffect(
            needInvoke = { _, _ -> false },
            invoke = { _, _, _ -> runnable.invoke() }
        )
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            object : ActionDispatcher<TestAction> {
                override suspend fun dispatch(action: TestAction) = Unit
            },
            coroutineScope = this
        )
        verify(runnable, never()).invoke()
    }

    @Test
    fun `Given effects, when launch, Then effect invoked all asynchronously`() = runTest {
        val runners: List<() -> Unit> = List(5) { mock() }
        val effect = TestCompositeEffect(
            TestCompositeEffect(
                TestNodeEffect { _, _, _ ->
                    delay(1000)
                    runners[0].invoke()
                },
                TestNodeEffect { _, _, _ ->
                    delay(1500)
                    runners[1].invoke()
                },
                TestNodeEffect { _, _, _ ->
                    delay(500)
                    runners[2].invoke()
                }
            ),
            TestCompositeEffect(
                TestNodeEffect { _, _, _ ->
                    delay(800)
                    runners[3].invoke()
                }
            ),
            TestNodeEffect { _, _, _ ->
                delay(900)
                runners[4].invoke()
            }
        )
        effect.launch(
            TestAction.genAction(),
            TestState.genState(),
            object : ActionDispatcher<TestAction> {
                override suspend fun dispatch(action: TestAction) = Unit
            },
            coroutineScope = this
        )
        advanceTimeBy(2000)
        runners.forEach { verify(it, once()).invoke() }
    }
}