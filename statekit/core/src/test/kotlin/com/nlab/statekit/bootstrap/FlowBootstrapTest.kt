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

package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import com.nlab.statekit.dispatch.ActionDispatcher
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.backgroundUnconfinedScope
import org.junit.Test

/**
 * @author Doohyun
 */
class FlowBootstrapTest {
    @Test
    fun `Given action and early started, When fetched with zero subscription count, Then action stream collected`() = runTest {
        val action = TestAction.genAction()
        val started = DeliveryStarted.Eagerly
        val actionDispatcher = mockk<ActionDispatcher<TestAction>>(relaxed = true)

        FlowBootstrap(flowOf(action), started).fetch(
            coroutineScope = backgroundUnconfinedScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(value = 0)
        )
        advanceUntilIdle()

        coVerify(exactly = 1) { actionDispatcher.dispatch(action) }
    }

    @Test
    fun `Given action and lazy started, When fetched, Then action stream collected after subscription count increased`() = runTest {
        val action = TestAction.genAction()
        val started = DeliveryStarted.Lazily
        val actionDispatcher = mockk<ActionDispatcher<TestAction>>(relaxed = true)
        val stateSubscriptionCount = MutableStateFlow(value = 0)

        FlowBootstrap(flowOf(action), started).fetch(
            coroutineScope = backgroundUnconfinedScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = stateSubscriptionCount
        )
        advanceUntilIdle()
        coVerify(inverse = true) { actionDispatcher.dispatch(action) }

        stateSubscriptionCount.update { it + 1 }
        advanceUntilIdle()
        coVerify(exactly = 1) { actionDispatcher.dispatch(action) }
    }

    @Test
    fun `Given action and While subscribed started, When fetched, Then action stream collected after subscription count increased`() = runTest {
        val action = TestAction.genAction()
        val started = DeliveryStarted.WhileSubscribed()
        val actionDispatcher = mockk<ActionDispatcher<TestAction>>(relaxed = true)
        val stateSubscriptionCount = MutableStateFlow(value = 0)

        FlowBootstrap(flowOf(action), started).fetch(
            coroutineScope = backgroundUnconfinedScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = stateSubscriptionCount
        )
        advanceUntilIdle()
        coVerify(inverse = true) { actionDispatcher.dispatch(action) }

        stateSubscriptionCount.update { it + 1 }
        advanceUntilIdle()
        coVerify(exactly = 1) { actionDispatcher.dispatch(action) }
    }

    @Test
    fun `Given action flow and While subscribed started with 2 stopTimeoutSeconds, When fetched, Then actionStream is canceled 2_5 seconds after the number of subscribers reaches 0`() = runTest {
        val actionPublisher = Channel<TestAction>(Channel.UNLIMITED)
        val cancelledExceptionHandler: () -> Unit = mockk(relaxed = true)
        val actionFlow = flow {
            try {
                for (action in actionPublisher) emit(action)
            } catch (_: CancellationException) {
                cancelledExceptionHandler.invoke()
            }
        }
        val started = DeliveryStarted.WhileSubscribed(stopTimeoutMillis = 2_000)
        val actionDispatcher = mockk<ActionDispatcher<TestAction>>(relaxed = true)
        val stateSubscriptionCount = MutableStateFlow(value = 1)

        val jobs = FlowBootstrap(actionFlow, started).fetch(
            coroutineScope = this,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = stateSubscriptionCount
        )
        advanceUntilIdle()
        stateSubscriptionCount.update { 0 }

        advanceTimeBy(2_500)
        verify(exactly = 1) { cancelledExceptionHandler.invoke() }

        jobs.forEach { it.cancelAndJoin() }
    }
}