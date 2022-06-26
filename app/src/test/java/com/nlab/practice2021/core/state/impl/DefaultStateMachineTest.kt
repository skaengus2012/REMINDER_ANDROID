package com.nlab.practice2021.core.state.impl

import com.nlab.practice2021.core.state.ActionProcessor
import com.nlab.practice2021.core.state.TestAction
import com.nlab.practice2021.core.state.TestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultStateMachineTest {
    @Test
    fun `actionProcessor sent event when state machine sent`() = runTest {
        val actionProcessor: ActionProcessor<TestAction> = mock()
        val stateMachine = DefaultStateMachine<TestAction, TestState>(actionProcessor, mock())
        val action = TestAction.Action1()
        stateMachine.send(action)
        verify(actionProcessor, times(1)).send(action)
    }
}