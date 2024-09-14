package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Thalys
 */
class EmptyReduceTest {
    @Test
    fun `Given state, When transition with any action, Then return same state`() {
        val reduce = EmptyReduce<TestAction, TestState>()
        val expectedState = TestState.genState()
        val actualState = reduce.transitionTo(TestAction.genAction(), expectedState)

        assertThat(actualState, sameInstance(expectedState))
    }

    @Test
    fun successLaunchEffect() = runTest {
        val reduce = EmptyReduce<TestAction, TestState>()
        reduce.launchEffect(TestAction.genAction(), TestState.genState(), mock())
    }
}