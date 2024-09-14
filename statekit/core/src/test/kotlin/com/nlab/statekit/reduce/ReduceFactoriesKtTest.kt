package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Thalys
 */
class ReduceFactoriesKtTest {
    @Test
    fun `When Reduce invoked, Then return DefaultReduce`() {
        val reduce = Reduce<TestAction, TestState>(transitionTo = mock(), launchEffect = mock())
        assertThat(reduce, instanceOf(DefaultReduce::class))
    }

    @Test
    fun testEmptyReduce() {
        assertThat(emptyReduce<TestAction, TestState>(), instanceOf(EmptyReduce::class))
    }
}