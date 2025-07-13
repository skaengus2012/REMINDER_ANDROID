package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class EmptyBootstrapTest {
    @Test
    fun `When fetched, Then empty set returned`() {
        val bootstrap: Bootstrap<TestAction> = EmptyBootstrap
        val resultJobs = bootstrap.fetch(
            coroutineScope = mockk(),
            actionDispatcher = mockk(),
            stateSubscriptionCount = mockk()
        )
        assertThat(resultJobs.size, equalTo(0))
    }

}