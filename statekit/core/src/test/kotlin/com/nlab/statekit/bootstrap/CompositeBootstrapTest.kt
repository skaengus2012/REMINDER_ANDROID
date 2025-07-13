package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import com.nlab.testkit.faker.genInt
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Job
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class CompositeBootstrapTest {
    @Test
    fun `Given bootstraps, When fetch on merged bootstrap, Then all bootstraps fetched`() {
        val bootstraps = List(genInt(min = 5, max = 10)) {
            mockk<Bootstrap<TestAction>> {
                every {
                    fetch(any(), any(), any())
                } returns setOf(Job())
            }
        }
        var compositeBootstrap: Bootstrap<TestAction> = bootstraps.first()
        for (i in 1 until bootstraps.size) {
            compositeBootstrap = CompositeBootstrap(
                head = bootstraps[i],
                tails = setOf(compositeBootstrap)
            )
        }
        val resultJobs = compositeBootstrap.fetch(
            coroutineScope = mockk(),
            actionDispatcher = mockk(),
            stateSubscriptionCount = mockk()
        )
        bootstraps.forEach { bootstrap ->
            verify(exactly = 1) {
                bootstrap.fetch(any(), any(), any())
            }
        }
        assertThat(resultJobs.size, equalTo(bootstraps.size))
    }
}