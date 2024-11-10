package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
class EmptyBootstrapTest {
    @Test
    fun `When fetched, Then empty set returned`() {
        val bootstrap: Bootstrap<TestAction> = EmptyBootstrap
        val resultJobs = bootstrap.fetch(
            coroutineScope = mock(),
            actionDispatcher = mock(),
            stateSubscriptionCount = mock()
        )
        assertThat(resultJobs.size, equalTo(0))
    }

}