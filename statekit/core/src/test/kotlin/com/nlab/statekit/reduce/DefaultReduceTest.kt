package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
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
class DefaultReduceTest {
    @Test
    fun `Given transit function, When transition invoked, Then transit function also invoked`() {
        val expectedState = TestState.genState()
        val transition: (TestAction, TestState) -> TestState = mock {
            whenever(mock.invoke(any(), any())) doReturn expectedState
        }
        val reduce = DefaultReduce(transition, mock())
        val actualState = reduce.transitionTo(TestAction.genAction(), TestState.genState())

        assertThat(actualState, equalTo(expectedState))
        verify(transition, once()).invoke(any(), any())
    }

    @Test
    fun `Given launchEffect function, When launchEffect invoked, Then launchEffect function also invoked`() = runTest {
        val launchEffect: suspend (TestAction, TestState, ActionDispatcher<TestAction>) -> Unit = mock()
        val reduce = DefaultReduce(mock(), launchEffect)
        val inputAction = TestAction.genAction()
        val inputState = TestState.genState()
        val inputActionDispatcher = object : ActionDispatcher<TestAction> {
            override suspend fun dispatch(action: TestAction) = Unit
        }
        reduce.launchEffect(inputAction, inputState, inputActionDispatcher)
        verify(launchEffect, once()).invoke(inputAction, inputState, inputActionDispatcher)
    }
}