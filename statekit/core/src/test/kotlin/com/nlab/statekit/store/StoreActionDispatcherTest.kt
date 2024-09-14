package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.Reduce
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
class StoreActionDispatcherTest {
    @Test
    fun `When action dispatched, Then state update to expectedState`() = runTest {
        val action = TestAction.genAction()
        val initState = TestState.State1
        val expectedState = TestState.State2
        val baseState = MutableStateFlow<TestState>(initState)
        val actionDispatcher = StoreActionDispatcher<TestAction, TestState>(
            baseState,
            mock { whenever(mock.transitionTo(action, initState)) doReturn expectedState }
        )

        actionDispatcher.dispatch(action)
        assertThat(baseState.value, equalTo(expectedState))
    }

    @Test
    fun `When action dispatched, Then effect launched with current state`() = runTest {
        val action = TestAction.genAction()
        val initState = TestState.State1
        val baseState = MutableStateFlow<TestState>(initState)
        val reduce = mock<Reduce<TestAction, TestState>> {
            whenever(mock.transitionTo(any(), any())) doReturn TestState.State2
        }
        val actionDispatcher = StoreActionDispatcher(
            baseState,
            reduce
        )

        actionDispatcher.dispatch(action)
        verify(reduce, once()).launchEffect(action, initState, actionDispatcher)
    }
}