package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.ActionDispatcher
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Thalys
 */
class DslEffectScopeTest {
    @Test
    fun successCreateDslEffectScope() {
        DslEffectScope(
            UpdateSource(TestAction.genAction(), TestState.genState()),
            actionDispatcher = mock<ActionDispatcher<TestAction>>()
        )
    }

    @Test
    fun testGetActionDispatcher() {
        val expectedActionDispatcher = object : ActionDispatcher<TestAction> {
            override suspend fun dispatch(action: TestAction) = Unit
        }
        val scope = DslEffectScope(
            UpdateSource(TestAction.genAction(), TestState.genState()),
            actionDispatcher = expectedActionDispatcher
        )
        assertThat(scope.actionDispatcher, sameInstance(expectedActionDispatcher))
    }
}