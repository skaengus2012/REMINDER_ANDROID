package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.dispatch.ActionDispatcher
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class StoreImplTest {
    @Test
    fun `When store dispatched, Then actionDispatcher should be dispatched`() = runTest {
        val input = TestAction.genAction()
        val actionDispatcher: ActionDispatcher<TestAction> = mockk(relaxed = true)
        val store = StoreImpl<TestAction, TestState>(
            state = mockk(),
            coroutineScope = this,
            actionDispatcher = actionDispatcher,
            initJobs = emptySet()
        )

        store.dispatch(input)
        advanceUntilIdle()

        coVerify(exactly = 1) { actionDispatcher.dispatch(input) }
    }

    @Test
    fun `Given init state, When store created, Then store has init state`() = runTest {
        val expectedState = TestState.genState()
        val store = StoreImpl<TestAction, TestState>(
            state = MutableStateFlow(expectedState),
            coroutineScope = this,
            actionDispatcher = mockk(),
            initJobs = emptySet()
        )
        assertThat(store.state.value, equalTo(expectedState))
    }
}