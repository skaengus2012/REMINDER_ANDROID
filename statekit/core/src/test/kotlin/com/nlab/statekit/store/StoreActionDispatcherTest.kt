package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.TestNodeEffect
import com.nlab.statekit.reduce.TestNodeTransition
import com.nlab.statekit.reduce.TestReduce
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StoreActionDispatcherTest {
    @Test
    fun `When action dispatched without transition, Then baseState never works`() = runTest {
        val initState = TestState.genState()
        val baseState = spy(MutableStateFlow(initState))
        val actionDispatcher = StoreActionDispatcher(
            baseState,
            TestReduce(transition = null)
        )

        actionDispatcher.dispatch(TestAction.genAction())
        verify(baseState, never()).compareAndSet(any(), any())
        assertThat(baseState.value, equalTo(initState))
    }

    @Test
    fun `Given matching Transition, When action dispatched, Then state update to expectedState`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State2
        val baseState = MutableStateFlow<TestState>(initState)
        val actionDispatcher = StoreActionDispatcher(
            baseState,
            TestReduce(transition = TestNodeTransition { _, _ -> expectedState })
        )
        actionDispatcher.dispatch(TestAction.genAction())
        assertThat(baseState.value, equalTo(expectedState))
    }

    @Test
    fun `Given matching effect and transition, When action dispatched, Then effect works with initState`() = runTest {
        val runnable: (TestState) -> Unit = mock()
        val initState = TestState.State1
        val transitionState = TestState.State2
        val actionDispatcher = StoreActionDispatcher(
            MutableStateFlow(initState),
            TestReduce(
                transition = TestNodeTransition { _, _ -> transitionState },
                effect = TestNodeEffect { _, current, _ -> runnable(current) }
            )
        )
        actionDispatcher.dispatch(TestAction.genAction())
        verify(runnable, once()).invoke(initState)
    }
}