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
import com.nlab.statekit.reduce.ActionDispatcher
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Thalys
 */
class DslEffectKtTest {
    @Test(expected = IllegalStateException::class)
    fun `Given zero or single effect, When create CompositeEffect, Then occurred error`() {
        val scope = "0"
        DslEffect.Composite(
            scope = scope,
            effects = List(size = genInt(min = 0, max = 1)) { TestDslEffect(scope) }
        )
    }

    @Test
    fun `Given dispatch able effect, When launch, Then action dispatcher invoked`() = runTest {
        val scope = "0"
        val inputAction: TestAction = TestAction.Action1
        val expectedAction: TestAction = TestAction.Action2
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val nodeEffect = TestDslEffectNode(scope) { dslEffectScope ->
            dslEffectScope.actionDispatcher.dispatch(expectedAction)
        }
        nodeEffect.launch(action = inputAction, actionDispatcher = actionDispatcher)
        verify(actionDispatcher, once()).dispatch(expectedAction)
    }

    @Test
    fun `Given 3 times effects, When launch, Then all effects launched`() = runTest {
        val scope = "0"
        val runner: () -> Unit = mock()
        val node = TestDslEffectNode(scope) { runner.invoke() }
        val effect = DslEffect.Composite(
            scope = scope,
            effects = listOf(
                node,
                DslEffect.Composite(
                    scope = scope,
                    effects = listOf(node, node)
                )
            )
        )
        effect.launch()
        verify(runner, times(3)).invoke()
    }

    @Test
    fun `Given matched predicate scopes, When launch, Then child effect invoked`() = runTest {
        val scope = "0"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectNode(scope) { runner.invoke() }
        val effect = DslEffect.PredicateScope<TestAction, TestState>(
            scope = scope,
            isMatch = { true },
            effect = nodeEffect
        )
        effect.launch()
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given not matched predicate scopes, When launch, Then child effect never invoked`() = runTest {
        val scope = "0"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectNode(scope) { runner.invoke() }
        val effect = DslEffect.PredicateScope<TestAction, TestState>(
            scope = scope,
            isMatch = { false },
            effect = nodeEffect
        )
        effect.launch()
        verify(runner, never()).invoke()
    }

    @Test
    fun `Given transform source scopes, When launch, Then child effect invoked`() = runTest {
        val scope = "0"
        val subScope = "1"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectNode(scope) { runner.invoke() }
        val effect = DslEffect.TransformSourceScope<TestAction, TestState, Int, TestState>(
            scope = scope,
            subScope = subScope,
            transformSource = { source -> UpdateSource(action = 1, source.current) },
            effect = nodeEffect
        )
        effect.launch()
        verify(runner, once()).invoke()
    }

    @Test
    fun `Given not matched transform source scopes, When launch, Then child effect never invoked`() = runTest {
        val scope = "0"
        val subScope = "1"
        val twoDepthSubScope = "2"
        val runner: () -> Unit = mock()
        val nodeEffect = TestDslEffectNode(scope) { runner.invoke() }
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
        effect.launch()
        verify(runner, never()).invoke()
    }
}