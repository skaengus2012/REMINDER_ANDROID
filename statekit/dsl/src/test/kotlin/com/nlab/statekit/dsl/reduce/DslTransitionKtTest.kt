/*
 * Copyright (C) 2024 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.statekit.dsl.reduce

import com.nlab.statekit.dsl.TestAction
import com.nlab.statekit.dsl.TestState
import com.nlab.testkit.faker.genInt
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class DslTransitionKtTest {
    /**
    @Test
    fun `Given changeable node, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val node = TestDslTransitionNode(scope = Any()) { expectedState }
        node.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not changed node transition, When transitionTo, Then return input state`() {
        val inputState = TestState.State1
        TestDslTransitionNode().assert(inputState = inputState, expectedState = inputState)
    }

    @Test(expected = IllegalStateException::class)
    fun `Given zero or single transition, When create CompositeTransition, Then occurred error`() {
        val transitions = List(size = genInt(min = 0, max = 1)) { TestDslTransition() }
        DslTransition.Composite(Any(), transitions)
    }

    @Test
    fun `Given 2 or more transitions, When create CompositeTransition, Then success`() {
        val transitions = List(size = genInt(min = 2, max = 10)) { TestDslTransition() }
        DslTransition.Composite(Any(), transitions)
    }

    @Test
    fun `Given multi depth composites, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = "0"
        val goalTransition = TestDslTransitionNode(scope) { expectedState }
        var transition: DslTransition = DslTransition.Composite(
            scope = scope,
            transitions = listOf(
                TestDslTransitionNode(scope),
                goalTransition
            )
        )
        repeat(times = genInt(min = 2, max = 5)) {
            transition = DslTransition.Composite(
                scope,
                listOf(TestDslTransitionNode(scope), transition)
            )
        }
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given goal and identity transition, When transitionTo, Then identity transition never works`() {
        val inputState = TestState.State1
        val changedState = TestState.State3
        val scope = "0"
        val neverWorksTransition: (TestDslTransitionScope) -> TestState = mock()
        val transition = DslTransition.Composite(
            scope,
            listOf(
                TestDslTransitionNode(scope),
                TestDslTransitionNode(scope) { changedState },
                TestDslTransitionNode(scope, neverWorksTransition)
            )
        )
        transition.assert(inputState = inputState, expectedState = changedState)
        verify(neverWorksTransition, never()).invoke(any())
    }

    @Test
    fun `Given multi depth predicate scopes, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = "0"
        var transition: DslTransition = TestDslTransitionNode(scope) { expectedState }
        repeat(times = genInt(min = 2, max = 5)) {
            transition = DslTransition.PredicateScope<TestAction, TestState>(
                scope = scope,
                isMatch = { true },
                transition
            )
        }
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not matched predicate scope and child transition, When transitionTo, Then child node never works`() {
        val inputState = TestState.State1
        val scope = "0"
        val neverWorksTransition: (TestDslTransitionScope) -> TestState = mock()
        val transition = DslTransition.PredicateScope<TestAction, TestState>(
            scope = scope,
            isMatch = { false },
            TestDslTransitionNode(scope, neverWorksTransition)
        )
        transition.assert(inputState = inputState, expectedState = inputState)
        verify(neverWorksTransition, never()).invoke(any())
    }

    @Test
    fun `Given transform source scope, When transitionTo, Then return next node`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = "0"
        val subScope = "1"
        val transition = DslTransition.TransformSourceScope<TestAction, TestState, Int, TestState>(
            scope = scope,
            subScope = subScope,
            transformSource = { source -> UpdateSource(action = 1, source.current) },
            transition = DslTransition.Node<TestState, Int, TestState>(subScope) { expectedState }
        )
        transition.assert(inputState = inputState, expectedState = expectedState)
    }

    @Test
    fun `Given not matched transform source scopes, When transitionTo, Then child node transition never works`() {
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val scope = "0"
        val subScope = "1"
        val twoDepthSubScope = "2"
        val transition = DslTransition.Composite(
            scope = scope,
            listOf(
                TestDslTransitionNode(scope),
                DslTransition.TransformSourceScope<TestAction, TestState, Int, TestState>(
                    scope = scope,
                    subScope = subScope,
                    transformSource = { source -> UpdateSource(action = 1, source.current) },
                    transition = DslTransition.TransformSourceScope<TestAction, TestState, String, TestState>(
                        scope = subScope,
                        subScope = twoDepthSubScope,
                        transformSource = { null },
                        transition = DslTransition.Node<TestState, Int, TestState>(twoDepthSubScope) {
                            TestState.State2
                        }
                    )
                ),
                DslTransition.PredicateScope<TestAction, TestState>(
                    scope,
                    { true },
                    DslTransition.Node<TestState, Int, TestState>(scope) { expectedState }
                )
            )
        )
        transition.assert(inputState = inputState, expectedState = expectedState)
    }*/
}