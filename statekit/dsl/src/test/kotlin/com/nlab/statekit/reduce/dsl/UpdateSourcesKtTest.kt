package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class UpdateSourcesKtTest {
    @Test
    fun `Given action, state, When create updateSource, Then has given fields`() {
        val action = TestAction.genAction()
        val current = TestState.genState()
        val updateSource = UpdateSource(action, current)

        assertThat(updateSource.action, equalTo(action))
        assertThat(updateSource.current, equalTo(current))
    }

    @Test
    fun `Given action type and same type action, When try copy with action type, Then return new source`() {
        val actionType = TestAction.Action1::class
        val originSource = UpdateSource<TestAction, TestState>(
            TestAction.Action1,
            TestState.genState()
        )
        val copySource = originSource.tryCopyWithActionType(actionType)

        assertThat(copySource!!.action, equalTo(originSource.action))
    }

    @Test
    fun `Given action type and another type action, When try copy with action type, Then return null`() {
        val actionType = TestAction.Action1::class
        val originSource = UpdateSource<TestAction, TestState>(
            TestAction.Action2,
            TestState.genState()
        )
        val copySource = originSource.tryCopyWithActionType(actionType)
        assert(copySource == null)
    }

    @Test
    fun `Given state type and same type state, When try copy with state type, Then return new source`() {
        val stateType = TestState.State1::class
        val originSource = UpdateSource<TestAction, TestState>(
            TestAction.genAction(),
            TestState.State1
        )
        val copySource = originSource.tryCopyWithStateType(stateType)

        assertThat(copySource!!.current, equalTo(originSource.current))
    }

    @Test
    fun `Given state type and another type state, When try copy with state type, Then return null`() {
        val stateType = TestState.State1::class
        val originSource = UpdateSource<TestAction, TestState>(
            TestAction.genAction(),
            TestState.State2
        )
        val copySource = originSource.tryCopyWithStateType(stateType)
        assert(copySource == null)
    }
}