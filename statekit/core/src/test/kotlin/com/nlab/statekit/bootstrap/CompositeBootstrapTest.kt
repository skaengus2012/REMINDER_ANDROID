package com.nlab.statekit.bootstrap

import com.nlab.statekit.TestAction
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.Job
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
class CompositeBootstrapTest {
    @Test
    fun `Given bootstraps, When fetch on merged bootstrap, Then all bootstraps fetched`() {
        val bootstraps = List(genInt(min = 5, max = 10)) {
            mock<Bootstrap<TestAction>> {
                whenever(mock.fetch(any(), any(), any())) doReturn setOf(Job())
            }
        }
        var compositeBootstrap: Bootstrap<TestAction> = bootstraps.first()
        for (i in 1 until bootstraps.size) {
            compositeBootstrap = CompositeBootstrap(
                head = bootstraps[i],
                tails = setOf(compositeBootstrap)
            )
        }
        val resultJobs = compositeBootstrap.fetch(mock(), mock(), mock())
        bootstraps.forEach { verify(it, once()).fetch(any(), any(), any()) }
        assertThat(resultJobs.size, equalTo(bootstraps.size))
    }
}