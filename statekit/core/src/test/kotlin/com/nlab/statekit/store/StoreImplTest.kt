package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StoreImplTest {
    @Test
    fun `When store dispatched, Then actionDispatcher should be dispatched`() = runTest {
        val input = TestAction.genAction()
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val store = StoreImpl<TestAction, TestState>(
            state = mock(),
            coroutineScope = this,
            actionDispatcher = actionDispatcher,
            initJobs = emptySet()
        )
        store.dispatch(input).join()
        verify(actionDispatcher, once()).dispatch(input)
    }

    @Test
    fun `Given init state, When store created, Then store has init state`() = runTest {
        val expectedState = TestState.genState()
        val store = StoreImpl<TestAction, TestState>(
            state = MutableStateFlow(expectedState),
            coroutineScope = this,
            actionDispatcher = mock(),
            initJobs = emptySet()
        )
        assertThat(store.state.value, equalTo(expectedState))
    }
}