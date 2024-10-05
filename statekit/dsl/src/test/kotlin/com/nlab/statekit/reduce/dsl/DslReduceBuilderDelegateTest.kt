package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private typealias TestDslReduceBuilderDelegate = DslReduceBuilderDelegate<TestAction, TestState, TestAction, TestState>

/**
 * @author Doohyun
 */
class DslReduceBuilderDelegateTest {
    @Test
    fun `Given transition, When build transition, Then transition invoked`() {
        val inputState = TestState.State1
        val transitionReturnState = TestState.State3
        val expectedNextState = TestState.State3
        val transition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock {
            whenever(mock.invoke(any())) doReturn transitionReturnState
        }
        val compositeTransition = TestDslReduceBuilderDelegate()
            .apply { addTransition(transition) }
            .buildTransition()
        val actualState = compositeTransition.invoke(
            DslTransitionScope(UpdateSource(TestAction.genAction(), inputState))
        )
        assertThat(actualState, equalTo(expectedNextState))
    }

    @Test
    fun `Given transition and predicate result is true, When build transition, Then transition invoked`() {
        val inputState = TestState.State1
        val transitionReturnState = TestState.State3
        val expectedNextState = TestState.State3
        val transition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock {
            whenever(mock.invoke(any())) doReturn transitionReturnState
        }
        val compositeTransition = TestDslReduceBuilderDelegate()
            .apply { addTransitionWithPredicate(predicate = { true }, transition) }
            .buildTransition()
        val actualState = compositeTransition.invoke(
            DslTransitionScope(UpdateSource(TestAction.genAction(), inputState))
        )
        assertThat(actualState, equalTo(expectedNextState))
    }

    @Test
    fun `Given transition and predicate result is false, When build transition, Then transition never invoked`() {
        val inputState = TestState.State1
        val transitionReturnState = TestState.State3
        val expectedNextState = TestState.State1
        val transition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock {
            whenever(mock.invoke(any())) doReturn transitionReturnState
        }
        val compositeTransition = TestDslReduceBuilderDelegate()
            .apply { addTransitionWithPredicate(predicate = { false }, transition) }
            .buildTransition()
        val actualState = compositeTransition.invoke(
            DslTransitionScope(UpdateSource(TestAction.genAction(), inputState))
        )
        assertThat(actualState, equalTo(expectedNextState))
    }
}