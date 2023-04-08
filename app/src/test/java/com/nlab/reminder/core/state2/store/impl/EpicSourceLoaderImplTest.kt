/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.state2.store.impl

import com.nlab.reminder.core.state2.TestAction
import com.nlab.reminder.core.state2.TestState
import com.nlab.reminder.core.state2.middleware.enhancer.ActionDispatcher
import com.nlab.reminder.core.state2.middleware.epic.EpicClient
import com.nlab.reminder.core.state2.middleware.epic.EpicSource
import com.nlab.reminder.core.state2.middleware.epic.SubscriptionStrategy
import com.nlab.reminder.core.state2.middleware.epic.util.EmptyEpicClient
import com.nlab.reminder.core.state2.middleware.epic.util.EpicClientFactory
import com.nlab.testkit.genInt
import com.nlab.testkit.once
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class EpicSourceLoaderImplTest {

    private fun loadEpicSource(
        coroutineScope: CoroutineScope,
        epicSources: List<EpicSource<TestAction>> = emptyList(),
        actionDispatcher: ActionDispatcher<TestAction> = mock(),
    ) {
        EpicSourceLoaderImpl().load(
            coroutineScope,
            epicSources,
            actionDispatcher,
            stateFlow = MutableStateFlow(TestState.genState())
        )
    }

    @Test
    fun `When whileStateUsed epicSource inputted, whileStateUsedEpicClient epicClient was fetched`() = runTest {
        val epicClient: EpicClient = mock()
        EpicClientFactory.setWhileStateUsedEpicClientFactory { epicClient }

        val mockActionDispatcher: ActionDispatcher<TestAction> = mock()
        val epicStream = flowOf(TestAction.genAction())
        loadEpicSource(
            coroutineScope = this,
            epicSources = listOf(
                EpicSource(epicStream, SubscriptionStrategy.WhileStateUsed)
            ),
            actionDispatcher = mockActionDispatcher
        )

        verify(epicClient, once()).fetch(coroutineScope = this, epicStream, mockActionDispatcher)
    }

    private fun checkedEpicClientFactoryInvoked(
        coroutineScope: CoroutineScope,
        expectedCount: Int,
        epicSources: List<EpicSource<TestAction>>,
        setupEpicClientFactory: ((MutableStateFlow<*>) -> EpicClient) -> Unit
    ) {
        val factory: (MutableStateFlow<*>) -> EpicClient = mock {
            whenever(mock(any())).thenReturn(EmptyEpicClient())
        }
        setupEpicClientFactory(factory)

        EpicClientFactory.setWhileStateUsedEpicClientFactory(factory)
        loadEpicSource(coroutineScope, epicSources)

        verify(factory, times(expectedCount)).invoke(any())
    }

    @Test
    fun `WhileStateUsedEpicClient epicClient created once, when EpicSources is not empty`() = runTest {
        checkedEpicClientFactoryInvoked(
            coroutineScope = this,
            expectedCount = 1,
            epicSources = List(genInt("#0")) {
                EpicSource(flowOf(TestAction.genAction()), SubscriptionStrategy.WhileStateUsed)
            },
            setupEpicClientFactory = { factory -> EpicClientFactory.setWhileStateUsedEpicClientFactory(factory) }
        )
    }

    @Test
    fun `WhileStateUsedEpicClient epicClient never created, when EpicSources is empty`() = runTest {
        checkedEpicClientFactoryInvoked(
            coroutineScope = this,
            epicSources = emptyList(),
            expectedCount = 0,
            setupEpicClientFactory = { factory -> EpicClientFactory.setWhileStateUsedEpicClientFactory(factory) }
        )
    }
}