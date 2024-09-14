package com.nlab.statekit.bootstrap

import com.nlab.statekit.reduce.ActionDispatcher
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
        val actionDispatcher: ActionDispatcher<in Any> = mock()
        val stateSubscriptionCount: StateFlow<Int> = mock()
        val onFetchedMock = mock<(CoroutineScope, ActionDispatcher<in Any>, StateFlow<Int>) -> List<Job>>()
        val bootstrap = object : Bootstrap<Any>() {
            override fun onFetched(
                coroutineScope: CoroutineScope,
                actionDispatcher: ActionDispatcher<in Any>,
                stateSubscriptionCount: StateFlow<Int>
            ): List<Job> = onFetchedMock(coroutineScope, actionDispatcher, stateSubscriptionCount)
        }

        bootstrap.fetch(coroutineScope, actionDispatcher, stateSubscriptionCount)
        verify(onFetchedMock, once()).invoke(coroutineScope, actionDispatcher, stateSubscriptionCount)
    }
}