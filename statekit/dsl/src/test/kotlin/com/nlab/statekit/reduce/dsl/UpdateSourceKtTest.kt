package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class UpdateSourceKtTest {
    @Test
    fun `Given action, state, When create updateSource, Then has given fields`() {
        val action = TestAction.genAction()
        val current = TestState.genState()
        val updateSource = UpdateSource(action, current)

        assertThat(updateSource.action, equalTo(action))
        assertThat(updateSource.current, equalTo(current))
    }
}