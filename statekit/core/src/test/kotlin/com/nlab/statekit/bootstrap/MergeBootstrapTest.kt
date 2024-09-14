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
class MergeBootstrapTest {
    @Test
    fun `Given multiple bootstrap, When get bootstraps, Then return input bootstraps`() {
        val inputBootstraps = listOf(TestBootstrap())
        val bootstrap = MergeBootstrap(inputBootstraps)
        val actualBootstraps = bootstrap.bootstraps

        assertThat(actualBootstraps, equalTo(inputBootstraps))
    }

    @Test
    fun `Given bootstraps, When fetch on merged bootstrap, Then all bootstraps fetched`() {
        val bootstraps = List(genInt(min = 5, max = 10)) {
            mock<Bootstrap<TestAction>> {
                whenever(mock.fetch(any(), any(), any())) doReturn listOf(Job())
            }
        }
        val mergeBootstrap = MergeBootstrap(bootstraps)
        val resultJobs = mergeBootstrap.fetch(mock(), mock(), mock())

        bootstraps.forEach { verify(it, once()).fetch(any(), any(), any()) }
        assertThat(resultJobs.size, equalTo(bootstraps.size))
    }
}