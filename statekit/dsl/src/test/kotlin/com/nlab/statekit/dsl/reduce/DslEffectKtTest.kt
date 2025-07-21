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
import com.nlab.statekit.test.reduce.Advance
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genLong
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class DslEffectKtTest {
    @Test
    fun `Given 3 depth composite, When launch, Then all effect launched in order`() = runTest {
        val rootScope = genBothify()
        val behaviors: List<() -> Unit> = List(3) { mockk(relaxed = true) }
        val composite = DslEffect.Composite(
            scope = rootScope,
            head = TestDslEffectNode(rootScope) { behaviors[0].invoke() },
            tails = listOf(
                DslEffect.Composite(
                    scope = rootScope,
                    head = TestDslEffectNode(rootScope) { behaviors[1].invoke() },
                    tails = listOf(TestDslEffectNode(rootScope) { behaviors[2].invoke() })
                )
            )
        )
        composite.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()

        verifyOrder {
            behaviors.forEach { it.invoke() }
        }
    }

    @Test
    fun `Given composite of one throwable, two success nodes, When launch, Then success nodes invoked all`() = runTest {
        val rootScope = genBothify()
        val successBehaviors: List<() -> Unit> = List(2) { mockk(relaxed = true) }
        val composite = DslEffect.Composite(
            scope = rootScope,
            head = TestDslEffectNode(rootScope) { throw RuntimeException() },
            tails = listOf(
                DslEffect.Composite(
                    scope = rootScope,
                    head = TestDslEffectNode(rootScope) { successBehaviors[0].invoke() },
                    tails = listOf(TestDslEffectNode(rootScope) { successBehaviors[1].invoke() })
                )
            )
        )
        composite.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()

        successBehaviors.forEach { behavior ->
            verify(exactly = 1) {
                behavior.invoke()
            }
        }
    }

    @Test
    fun `Given composite with 3 times suspend node, When launch, Then all effects launched asynchronously`() = runTest {
        val rootScope = genBothify()
        val delayTimeMillis = genLong(min = 2_000, max = 3_000)
        val behaviors: List<() -> Unit> = List(3) { mockk(relaxed = true) }
        val composite = DslEffect.Composite(
            scope = rootScope,
            head = TestDslEffectSuspendNode(rootScope) {
                delay(delayTimeMillis)
                behaviors[0].invoke()
            },
            tails = listOf(
                TestDslEffectSuspendNode(rootScope) {
                    delay(delayTimeMillis)
                    behaviors[1].invoke()
                },
                TestDslEffectSuspendNode(rootScope) {
                    delay(delayTimeMillis)
                    behaviors[2].invoke()
                }
            )
        )
        composite.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace(
                advance = Advance.TimeBy(
                    delayTimeMillis = delayTimeMillis + 100 // with some buffer time
                )
            )
        behaviors.forEach { behavior ->
            verify(exactly = 1) {
                behavior.invoke()
            }
        }
    }

    @Test
    fun `Given composite with delayed throwable first, When launch, Then success node invoked`() = runTest {
        val rootScope = genBothify()
        val delayThrowableTimeMillis = genLong(min = 2_000, max = 3_000)
        val successBehavior: () -> Unit = mockk(relaxed = true)
        val composite = DslEffect.Composite(
            scope = rootScope,
            head = TestDslEffectSuspendNode(rootScope) {
                delay(delayThrowableTimeMillis)
                throw RuntimeException()
            },
            tails = listOf(
                TestDslEffectSuspendNode(rootScope) {
                    delay(delayThrowableTimeMillis * 2)
                    successBehavior()
                }
            )
        )
        composite.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()

        verify(exactly = 1) {
            successBehavior.invoke()
        }
    }

    @Test
    fun `Given matched predicate scopes, When launch, Then child effect invoked`() = runTest {
        val rootScope = genBothify()
        val runner: () -> Unit = mockk(relaxed = true)
        val predicateScope = TestDslEffectPredicateScope(
            scope = rootScope,
            isMatch = { true },
            effect = TestDslEffectSuspendNode(rootScope) { runner.invoke() }
        )
        predicateScope.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given unmatched predicate scope, When launch, Then child effect never invoked`() = runTest {
        val rootScope = genBothify()
        val runner: () -> Unit = mockk(relaxed = true)
        val predicateScope = TestDslEffectPredicateScope(
            scope = rootScope,
            isMatch = { false },
            effect = TestDslEffectSuspendNode(rootScope) { runner.invoke() }
        )
        predicateScope.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(inverse = true) {
            runner.invoke()
        }
    }

    @Test
    fun `Given transformable scope, When launch, Then child effect invoked`() = runTest {
        val rootScope = "0"
        val subScope = "1"
        val runner: () -> Unit = mockk(relaxed = true)
        val transformScope = TestDslEffectTransformScope(
            scope = rootScope,
            subScope = subScope,
            transformSource = { source ->
                UpdateSource(action = 1, current = source.current)
            },
            effect = DslEffect.Node<Int, TestState>(
                scope = subScope,
                invoke = { runner() }
            )
        )
        transformScope.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given not matched transform scopes, When launch, Then child effect never invoked`() = runTest {
        val rootScope = "0"
        val subScope = "1"
        val runner: () -> Unit = mockk()
        val transformScope = TestDslEffectTransformScope<Int, TestState>(
            scope = rootScope,
            subScope = subScope,
            transformSource = { null },
            effect = TestDslEffectSuspendNode(subScope) { runner.invoke() }
        )
        transformScope.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(inverse = true) {
            runner.invoke()
        }
    }

    @Test
    fun `Given composite that head is not transformable scope, When launch, Then tail effect launched`() = runTest {
        val rootScope = "0"
        val subScope = "1"
        val twoDepthSubScope = "2"
        val runner: () -> Unit = mockk(relaxed = true)
        val composite = DslEffect.Composite(
            scope = rootScope,
            head = TestDslEffectTransformScope(
                scope = rootScope,
                subScope = subScope,
                transformSource = { source -> UpdateSource(action = 1, current = source.current) },
                effect = TestDslEffectTransformScope<String, TestState>(
                    scope = subScope,
                    subScope = twoDepthSubScope,
                    transformSource = { null },
                    effect = mockk()
                )
            ),
            tails = listOf(
                TestDslEffectNode(scope = rootScope) {
                    runner.invoke()
                }
            )
        )
        composite.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }
}