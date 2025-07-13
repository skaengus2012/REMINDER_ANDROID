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
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class DslEffectKtTest {
    @Test
    fun `Given 3 depth composite node, When launch, Then all effect launched in order`() = runTest {
        val rootScope = genBothify()
        val behaviors: List<() -> Unit> = List(3) { mockk(relaxed = true) }
        val node = DslEffect.Composite(
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
        node.launchAndAwaitUntilIdle()

        verifyOrder {
            behaviors.forEach { it.invoke() }
        }
    }

    @Test
    fun `Given composed of one throwable, two success nodes, When launch, Then success nodes invoked all`() = runTest {
        val rootScope = genBothify()
        val successBehaviors: List<() -> Unit> = List(2) { mockk(relaxed = true) }
        val node = DslEffect.Composite(
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

    }

/**
    @Test
    fun `Given 3 depth effects, When launch Then all effects launched in order`() = runTest {
        val scope = "0"
        val firstBehavior: () -> Unit = mock()
        val secondBehavior: () -> Unit = mock()
        val thirdBehavior: () -> Unit = mock()
        val nodEffect = DslEffect.Composite(
            scope = scope,
            effects = listOf(
                TestDslEffectNode(scope) { firstBehavior.invoke() },
                DslEffect.Composite(
                    scope = scope,
                    effects = listOf(
                        TestDslEffectNode(scope) { secondBehavior.invoke() },
                        DslEffect.Composite(
                            scope = scope,
                            effects = listOf(
                                TestDslEffectNode(scope) { thirdBehavior.invoke() },
                                TestDslEffectNode(scope) { thirdBehavior.invoke() },
                            )
                        )
                    )
                )
            )
        )
        nodEffect.launchAndJoinForTest()
        inOrder(firstBehavior, secondBehavior, thirdBehavior) {
            verify(firstBehavior, once()).invoke()
            verify(secondBehavior, once()).invoke()
            verify(thirdBehavior, times(2)).invoke()
        }
    }

    @Test
    fun `Given 3 times suspend effects, When launch, Then all effects launched asynchronously`() = runTest {
        val scope = "0"
        val firstBehavior: () -> Unit = mock()
        val secondBehavior: () -> Unit = mock()
        val thirdBehavior: () -> Unit = mock()
        val nodEffect = DslEffect.Composite(
            scope = scope,
            effects = listOf(
                TestDslEffectSuspendNode(scope) {
                    delay(5_000)
                    firstBehavior.invoke()
                },
                TestDslEffectSuspendNode(scope) {
                    delay(3_000)
                    secondBehavior.invoke()
                },
                TestDslEffectSuspendNode(scope) {
                    delay(1_500)
                    thirdBehavior.invoke()
                }
            )
        )
        nodEffect.launchAndJoinForTest()
        advanceTimeBy(5_500)
        verify(firstBehavior, once()).invoke()
        verify(secondBehavior, once()).invoke()
        verify(thirdBehavior, once()).invoke()
    }

    @Test
    fun `Given matched predicate scopes, When launch, Then child effect invoked`() = runTest {
        val scope = "0"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectSuspendNode(scope) { runner.invoke() }
        val effect = DslEffect.PredicateScope<TestAction, TestState>(
            scope = scope,
            isMatch = { true },
            effect = nodeEffect
        )
        effect.launchAndJoinForTest()
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given not matched predicate scopes, When launch, Then child effect never invoked`() = runTest {
        val scope = "0"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectSuspendNode(scope) { runner.invoke() }
        val effect = DslEffect.PredicateScope<TestAction, TestState>(
            scope = scope,
            isMatch = { false },
            effect = nodeEffect
        )
        effect.launchAndJoinForTest()
        verify(runner, never()).invoke()
    }

    @Test
    fun `Given transform source scopes, When launch, Then child effect invoked`() = runTest {
        val scope = "0"
        val subScope = "1"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectSuspendNode(scope) { runner.invoke() }
        val effect = DslEffect.TransformSourceScope<TestAction, TestState, Int, TestState>(
            scope = scope,
            subScope = subScope,
            transformSource = { source -> UpdateSource(action = 1, source.current) },
            effect = nodeEffect
        )
        effect.launchAndJoinForTest()
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given not matched transform source scopes, When launch, Then child effect never invoked`() = runTest {
        val scope = "0"
        val subScope = "1"
        val twoDepthSubScope = "2"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectSuspendNode(scope) { runner.invoke() }
        val effect = DslEffect.TransformSourceScope<TestAction, TestState, Int, TestState>(
            scope = scope,
            subScope = subScope,
            transformSource = { null },
            effect = DslEffect.TransformSourceScope<TestAction, TestState, String, TestState>(
                scope = subScope,
                subScope = twoDepthSubScope,
                transformSource = { null },
                effect = nodeEffect
            )
        )
        effect.launchAndJoinForTest()
        verify(runner, never()).invoke()
    }

    @Test(expected = RuntimeException::class)
    fun `Given throwable effect nodes, When effect launched, Then exception be thrown`() {
        val effect = TestDslEffectNode(scope = "1") { throw RuntimeException() }
        effect.launchForTest(coroutineScope = CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `Given throwable effect nodes and exceptionHandler, When effect launched, Then exception be thrown to exceptionHandler`() {
        val effect = TestDslEffectNode(scope = "1") { throw RuntimeException() }
        val exceptionBlock: () -> Unit = mock()
        effect.launchForTest(
            coroutineScope = CoroutineScope(Dispatchers.Unconfined) + CoroutineExceptionHandler { _, _ ->
                exceptionBlock()
            }
        )
        verify(exceptionBlock, once()).invoke()
    }

    @Test(expected = RuntimeException::class)
    fun `Given throwable suspend effect nodes, When effect launched, Then exception be thrown`() = runTest {
        val effect = TestDslEffectSuspendNode(scope = "1") { throw RuntimeException() }
        effect.launchForTest(coroutineScope = CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `Given throwable suspend effect nodes and exceptionHandler, When effect launched, Then exception be thrown to exceptionHandler`() = runTest {
        val effect = TestDslEffectSuspendNode(scope = "1") { throw RuntimeException() }
        val exceptionBlock: () -> Unit = mock()
        val superJob = SupervisorJob()
        effect.launchForTest(
            coroutineScope = CoroutineScope(Dispatchers.Unconfined) + superJob + CoroutineExceptionHandler { _, _ ->
                exceptionBlock()
                superJob.cancel()
            }
        )
        superJob.join()
        verify(exceptionBlock, once()).invoke()
    }
    */
}