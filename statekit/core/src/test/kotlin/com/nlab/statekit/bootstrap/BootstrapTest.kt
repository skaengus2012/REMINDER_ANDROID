package com.nlab.statekit.bootstrap

import com.nlab.statekit.dispatch.ActionDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import org.junit.Test
import kotlin.collections.emptySet

/**
 * @author Doohyun
 */
class BootstrapTest {
    @Test
    fun `Given parameters for fetch, When fetched, Then onFetched is also called`() {
        val coroutineScope: CoroutineScope = mockk()
        val actionDispatcher: ActionDispatcher<Any> = mockk()
        val stateSubscriptionCount: StateFlow<Int> = mockk()
        val onFetchedMock = mockk<(CoroutineScope, ActionDispatcher<Any>, StateFlow<Int>) -> Set<Job>> {
            every { this@mockk.invoke(any(), any(), any()) } returns emptySet()
        }
        val bootstrap = object : Bootstrap<Any>() {
            override fun onFetched(
                coroutineScope: CoroutineScope,
                actionDispatcher: ActionDispatcher<Any>,
                stateSubscriptionCount: StateFlow<Int>
            ): Set<Job> = onFetchedMock(coroutineScope, actionDispatcher, stateSubscriptionCount)
        }

        bootstrap.fetch(coroutineScope, actionDispatcher, stateSubscriptionCount)
        verify(exactly = 1) { onFetchedMock.invoke(coroutineScope, actionDispatcher, stateSubscriptionCount) }
    }
}