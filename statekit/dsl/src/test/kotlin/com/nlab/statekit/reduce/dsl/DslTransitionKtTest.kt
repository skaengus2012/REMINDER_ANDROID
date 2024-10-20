package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.dsl.DslTransition.*
import com.nlab.statekit.reduce.transitionTo
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class DslTransitionKtTest {
    @Test
    fun `Given changeable node transition, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val nodeTransition = NodeTransition<TestState, TestAction, TestState>(Any()) { expectedState }
        nodeTransition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not changed node transition, When transitionTo, Then return input state`() {
        val inputState = TestState.State1
        IdentityNodeTransition().assert(inputState = inputState, expectedState = inputState)
    }

    @Test(expected = IllegalStateException::class)
    fun `Given zero or single transition, When create CompositeTransition, Then occurred error`() {
        val transitions = List(size = genInt(min = 0, max = 1)) {
            IdentityNodeTransition()
        }
        CompositeTransition(Any(), transitions)
    }

    @Test
    fun `Given 2 or more transitions, When create CompositeTransition, Then success`() {
        val transitions = List(size = genInt(min = 2, max = 10)) {
            IdentityNodeTransition()
        }
        CompositeTransition(Any(), transitions)
    }

    @Test
    fun `Given multi depth composite transitions, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = 0
        val goalTransition = NodeTransition<TestState, TestAction, TestState>(scope) { expectedState }
        var transition: DslTransition = CompositeTransition(
            scope = scope,
            transitions = listOf(
                IdentityNodeTransition(scope),
                goalTransition
            )
        )
        repeat(times = genInt(min = 2, max = 5)) {
            transition = CompositeTransition(
                scope,
                listOf(IdentityNodeTransition(scope), transition)
            )
        }
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given goal and identity transition, When transitionTo, Then identity transition never works`() {
        val inputState = TestState.State1
        val changedState = TestState.State3
        val scope = 0
        val neverWorksTransition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock()
        val transition = CompositeTransition(
            scope,
            listOf(
                IdentityNodeTransition(scope),
                NodeTransition(scope) { changedState },
                NodeTransition(scope, neverWorksTransition)
            )
        )
        transition.assert(inputState = inputState, expectedState = changedState)
        verify(neverWorksTransition, never()).invoke(any())
    }

    @Test
    fun `Given multi depth predicate transitions, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = 0
        var transition: DslTransition = NodeTransition<TestState, TestAction, TestState>(scope) { expectedState }
        repeat(times = genInt(min = 2, max = 5)) {
            transition = PredicateScopeTransition<TestAction, TestState>(
                scope = scope,
                isMatch = { true },
                transition
            )
        }
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not matched predicate and child transition, When transitionTo, Then child node transition never works`() {
        val inputState = TestState.State1
        val scope = 0
        val neverWorksTransition: (DslTransitionScope<TestAction, TestState>) -> TestState = mock()
        val transition = PredicateScopeTransition<TestAction, TestState>(
            scope = scope,
            isMatch = { false },
            NodeTransition(scope, neverWorksTransition)
        )
        transition.assert(inputState = inputState, expectedState = inputState)
        verify(neverWorksTransition, never()).invoke(any())
    }

    @Test
    fun `Given transform source transition, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = 0
        val subScope = 1
        val transition = TransformSourceScopeTransition<TestAction, TestState, Int, TestState>(
            scope = scope,
            subScope = subScope,
            transformSource = { source -> UpdateSource(action = 1, source.current) },
            transition = NodeTransition<TestState, Int, TestState>(subScope) { expectedState }
        )
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not matched transform source transition, When transitionTo, Then child node transition never works`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = 0
        val subScope = 1
        val twoDepthSubScope = 2
        val transition = CompositeTransition(
            scope = scope,
            listOf(
                IdentityNodeTransition(scope),
                TransformSourceScopeTransition<TestAction, TestState, Int, TestState>(
                    scope = scope,
                    subScope = subScope,
                    transformSource = { source -> UpdateSource(action = 1, source.current) },
                    transition = TransformSourceScopeTransition<TestAction, TestState, String, TestState>(
                        scope = subScope,
                        subScope = twoDepthSubScope,
                        transformSource = { null },
                        transition = NodeTransition<TestState, Int, TestState>(twoDepthSubScope) { TestState.State2 }
                    )
                ),
                PredicateScopeTransition<TestAction, TestState>(
                    scope,
                    { true },
                    NodeTransition<TestState, Int, TestState>(scope) { expectedState }
                )
            )
        )
        transition.assert(inputState = inputState, expectedState = expectedState)
    }
}

@Suppress("TestFunctionName")
private fun TestTransition(dslTransition: DslTransition) = Transition<TestAction, TestState>(dslTransition)

@Suppress("TestFunctionName")
private fun IdentityNodeTransition(
    scope: Any = Any()
): NodeTransition<TestState, TestAction, TestState> = NodeTransition(scope) { it.current }

private fun DslTransition.assert(
    inputAction: TestAction = TestAction.genAction(),
    inputState: TestState = TestState.genState(),
    expectedState: TestState
) {
    val transition = TestTransition(dslTransition = this)
    assertThat(
        transition.transitionTo(inputAction, inputState),
        equalTo(expectedState)
    )
}