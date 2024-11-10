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
import com.nlab.statekit.reduce.ActionDispatcher
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class BootstrapFactoriesKtTest {
    @Test
    fun `Given action, When fetch from with single action, Then action invoked after subscription count is greater then zero`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = Bootstrap(action)
        val subscriptionCount = MutableStateFlow(0)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = subscriptionCount
        )
        advanceUntilIdle()
        verify(actionDispatcher, never()).dispatch(action)

        subscriptionCount.update { it + 1 }
        advanceUntilIdle()
        verify(actionDispatcher, once()).dispatch(action)
    }

    @Test
    fun `When called EmptyBootstrap, Then return singleton empty bootstrap`() {
        assertThat(EmptyBootstrap(), sameInstance(EmptyBootstrap))
    }

    @Test
    fun `Given action and none pendingBootUntilSubscribed, When fetch from with single action, Then action invoked`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = Bootstrap(action, isPendingBootUntilSubscribed = false)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(0)
        )
        advanceUntilIdle()
        verify(actionDispatcher, once()).dispatch(action)
    }

    @Test
    fun `Given action stream, When fetch from with action stream, Then action invoked`() = runTest {
        val expectedActions = List(genInt(min = 2, max = 5)) { TestAction.genAction() }
        val actionStream = Channel<TestAction>(Channel.UNLIMITED)
            .apply { expectedActions.forEach { trySend(it) } }
            .receiveAsFlow()
        val bootstrap = actionStream.streamingBootstrap(started = DeliveryStarted.Eagerly)
        val actualActions = mutableListOf<TestAction>()

        val actionDispatcher: ActionDispatcher<TestAction> = object : ActionDispatcher<TestAction> {
            override suspend fun dispatch(action: TestAction) {
                actualActions += action
            }
        }
        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(0)
        )
        advanceUntilIdle()

        assertThat(actualActions, equalTo(expectedActions))
    }

    @Test
    fun `Given 3 Bootstraps, When called combineBootstrap, Then return merge bootstrap`() {
        val boot1 = TestBootstrap()
        val boot2 = TestBootstrap()
        val boot3 = TestBootstrap()
        val bootstrap = combineBootstrap(boot1, boot2, boot3)
        assertThat(bootstrap, instanceOf(CompositeBootstrap::class))
    }

    @Test
    fun `Given bootstrap list, When called combineBootstrap, Then return valid bootstrap`() {
        // empty case
        assertThat(combineBootstrap<TestAction>(emptyList()), instanceOf(EmptyBootstrap::class))

        // single case
        val singleBootstrap = TestBootstrap()
        assertThat(combineBootstrap(listOf(singleBootstrap)), sameInstance(singleBootstrap))

        // multiple cases
        val multipleBootstraps = List(genInt(min = 2, max = 5)) { TestBootstrap() }
        assertThat(combineBootstrap(multipleBootstraps), instanceOf(CompositeBootstrap::class))
    }

    @Test
    fun `Given 2 bootstraps, When plus, Then return merge bootstrap`() {
        val first = TestBootstrap()
        val second = TestBootstrap()

        val merge = first + second
        assertThat(merge, instanceOf(CompositeBootstrap::class))
    }
}