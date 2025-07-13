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
import com.nlab.statekit.test.reduce.expectedNextState
import com.nlab.statekit.test.reduce.expectedNotChanged
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Doohyun
 */
class DslTransitionKtTest {
    @Test
    fun `Given changeable node, When transition, Then return next node`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State3
        val node = TestDslTransitionNode(scope = genBothify()) { expectedState }

        node.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given non-changeable node, When transition, Then return initState`() = runTest {
        val initState = TestState.State1
        val node = TestDslTransitionNode(scope = genBothify()) { transitionScope -> transitionScope.current }

        node.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given multi depth composite, When transition, Then return correct state`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State3
        val rootScope = genBothify()
        var composite = DslTransition.Composite(
            scope = rootScope,
            head = TestDslTransitionNode(rootScope) { it.current },
            tails = listOf(TestDslTransitionNode(rootScope) { expectedState })
        )
        repeat(times = genInt(min = 2, max = 5)) {
            composite = DslTransition.Composite(
                scope = rootScope,
                head = TestDslTransitionNode(rootScope) { it.current },
                tails = listOf(composite)
            )
        }

        composite.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given identity, changed, mock composite, When transition, Then mock never works`() = runTest {
        val initState = TestState.State1
        val changedState = TestState.State3
        val rootScope = genBothify()
        val neverWorksTransitionNode: TestDslTransitionNode = mockk()
        val composite = DslTransition.Composite(
            rootScope,
            head = TestDslTransitionNode(rootScope) { it.current },
            tails = listOf(
                TestDslTransitionNode(rootScope) { changedState },
                neverWorksTransitionNode
            )
        )

        composite.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .getTransitionResult()

        verify(inverse = true) { neverWorksTransitionNode.next.invoke(any()) }
    }

    @Test
    fun `Given fully matched nested predicate scopes, When transition, Then return correct state`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State3
        val rootScope = genBothify()
        var dslTransition: DslTransition = TestDslTransitionNode(rootScope) { expectedState }
        repeat(times = genInt(min = 2, max = 5)) {
            dslTransition = TestDslTransitionPredicateScope(
                scope = rootScope,
                isMatch = { true },
                dslTransition
            )
        }

        dslTransition.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given unmatched predicate scope, When transition, Then return initState`() = runTest {
        val initState = TestState.State1
        val rootScope = genBothify()
        val predicatedScope = TestDslTransitionPredicateScope(
            scope = rootScope,
            isMatch = { false },
            transition = mockk()
        )

        predicatedScope.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given transformable scope, When transition, Then return correct state`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State3
        val rootScope = "0"
        val subScope = "1"
        val transformScope = TestDslTransitionTransformScope(
            scope = rootScope,
            subScope = subScope,
            transformSource = { source -> UpdateSource(action = 1, source.current) },
            transition = DslTransition.Node<TestState, Int, TestState>(subScope) { expectedState }
        )

        transformScope.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given not matched transform scope, When transition, Then return initState`() = runTest {
        val initState = TestState.State1
        val rootScope = "0"
        val subScope = "1"
        val transformScope = TestDslTransitionTransformScope<Int, TestState>(
            scope = rootScope,
            subScope = subScope,
            transformSource = { source -> null },
            transition = DslTransition.Node<TestState, Int, TestState>(scope = subScope, next = mockk())
        )

        transformScope.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given composite that head is not transformable scope, When transition, Then return correct state`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State3
        val rootScope = "0"
        val subScope = "1"
        val twoDepthSubScope = "2"
        val composite = DslTransition.Composite(
            scope = rootScope,
            head = TestDslTransitionTransformScope(
                scope = rootScope,
                subScope = subScope,
                transformSource = { source -> UpdateSource(action = 1, current = source.current) },
                transition = TestDslTransitionTransformScope<String, TestState>(
                    scope = subScope,
                    subScope = twoDepthSubScope,
                    transformSource = { null },
                    transition = mockk()
                )
            ),
            tails = listOf(
                TestDslTransitionNode(rootScope) { expectedState }
            )
        )

        composite.toReduceTestBuilder()
            .givenCurrent(initState)
            .actionToDispatch(TestAction.genAction())
            .transitionScenario()
            .expectedNextState(expectedState)
    }
}