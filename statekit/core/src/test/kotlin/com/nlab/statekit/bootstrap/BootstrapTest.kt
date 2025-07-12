package com.nlab.statekit.bootstrap

import com.nlab.statekit.dispatch.ActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class BootstrapTest {
    @Test
    fun `Given parameters for fetch, When fetched, Then onFetched is also called`() {
        val coroutineScope: CoroutineScope = mock()
        val actionDispatcher: ActionDispatcher<Any> = mock()
        val stateSubscriptionCount: StateFlow<Int> = mock()
        val onFetchedMock = mock<(CoroutineScope, ActionDispatcher<Any>, StateFlow<Int>) -> Set<Job>>()
        val bootstrap = object : Bootstrap<Any>() {
            override fun onFetched(
                coroutineScope: CoroutineScope,
                actionDispatcher: ActionDispatcher<Any>,
                stateSubscriptionCount: StateFlow<Int>
            ): Set<Job> = onFetchedMock(coroutineScope, actionDispatcher, stateSubscriptionCount)
        }

        bootstrap.fetch(coroutineScope, actionDispatcher, stateSubscriptionCount)
        verify(onFetchedMock, once()).invoke(coroutineScope, actionDispatcher, stateSubscriptionCount)
    }
}