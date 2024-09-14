package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author Thalys
 */
class DslTransitionBuilderTest {
    @Test
    fun `When build without transition strategy, Then return identity transition`() {
        /**
        val expectedState = TestState.genState()
        val transition = TestDslTransitionBuilder().build()

        val actualState = transition(
            DslTransitionScope(UpdateSource(TestAction.genAction(), expectedState))
        )
        assertThat(actualState, equalTo(expectedState))*/
    }

    @Test
    fun `Given multiple transitions, When build, Then return state matches condition`() {
        /**
        val inputState = TestState.State3
        val expectedState = TestState.State1
        val neverWorksTransition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock()
        val transition = TestDslTransitionBuilder()
            .apply {
                add { scope ->
                    // not matches case
                    if (scope.action == TestAction.Action2) TestState.State2
                    else scope.current
                }
                add { scope ->
                    // matches case
                    if (scope.action == TestAction.Action1) TestState.State1
                    else scope.current
                }
                add(neverWorksTransition) // fast cancelled
            }
            .build()
        val scope = DslTransitionScope<TestAction, TestState>(UpdateSource(TestAction.Action1, inputState))
        val actualState = transition(scope)
        assertThat(actualState, equalTo(expectedState))
        verify(neverWorksTransition, never()).invoke(scope)*/
    }
}