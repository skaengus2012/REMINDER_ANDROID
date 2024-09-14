package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class SingleBootstrapTest {
    @Test
    fun `Given action, When fetch lazy without none subscription, Then action never dispatched`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = SingleBootstrap(action, isPendingBootUntilSubscribed = true)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()

        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(value = 0)
        )
        advanceUntilIdle()
        verify(actionDispatcher, never()).dispatch(action)
    }

    @Test
    fun `Given action, When fetch lazy with subscription, Then action never dispatched`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = SingleBootstrap(action, isPendingBootUntilSubscribed = true)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()

        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(value = 1)
        ).joinAll()
        verify(actionDispatcher, once()).dispatch(action)
    }

    @Test
    fun `Given action, When fetch lazy with subscription, Then action never dispatched2`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = SingleBootstrap(action, isPendingBootUntilSubscribed = true)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val coroutineScope = unconfinedBackgroundScope
        bootstrap.fetch(
            coroutineScope = coroutineScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(value = 0)
        )
        advanceUntilIdle()
        coroutineScope.cancel()
        verify(actionDispatcher, never()).dispatch(action)
    }

    @Test
    fun `Given action, When fetch early, Then action dispatched immediately`() = runTest {
        val action = TestAction.genAction()
        val bootstrap = SingleBootstrap(action, isPendingBootUntilSubscribed = false)
        val actionDispatcher: ActionDispatcher<TestAction> = mock()

        bootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = mock()
        )
        advanceUntilIdle()
        verify(actionDispatcher, once()).dispatch(action)
    }
}