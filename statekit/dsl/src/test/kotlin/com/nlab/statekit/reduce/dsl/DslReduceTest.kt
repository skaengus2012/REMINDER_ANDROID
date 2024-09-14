package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.test.runTest
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
 * @author Thalys
 */
class DslReduceTest {
    @Test
    fun `Given transition strategy, When try transition, Then return next state by strategy`() {
        val expectedNextState = TestState.genState()
        val transitionStrategy: (DslTransitionScope<TestAction, TestState>) -> TestState = mock {
            whenever(mock.invoke(any())) doReturn expectedNextState
        }
        val reduce = DslReduce(transitionStrategy, mock())
        val actualNextState = reduce.transitionTo(TestAction.genAction(), TestState.genState())

        assertThat(actualNextState, equalTo(expectedNextState))
        verify(transitionStrategy, once()).invoke(any())
    }

    @Test
    fun `Given executor, When try launch effect, Then executor invoked`() = runTest {
        val executor: () -> Unit = mock()
        val reduce = DslReduce<TestAction, TestState>(mock(), { executor.invoke() })
        reduce.launchEffect(
            TestAction.genAction(),
            TestState.genState(),
            object : ActionDispatcher<TestAction> {
                override suspend fun dispatch(action: TestAction) = Unit
            }
        )
        verify(executor, once()).invoke()
    }
}
