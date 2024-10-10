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

package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private typealias TestActionScopeReduceBuilder = ActionScopeReduceBuilder<TestAction, TestState, TestAction, TestState>

/**
 * @author Doohyun
 */
class ActionScopeReduceBuilderTest {
    @Test
    fun `When create scope without external delegate, Then success`() {
        TestActionScopeReduceBuilder()
    }

    @Test
    fun `When build transition, Then get from delegate`() {
        val expectedTransition: TestDslTransitionBlock = { current }
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildTransition()) doReturn expectedTransition
            }
        )

        assertThat(actionScopeReduceBuilder.buildTransition(), equalTo(expectedTransition))
    }

    @Test
    fun `When build effect, Then get from delegate`() {
        val expectedEffect: TestDslEffectBlock = {}
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildEffect()) doReturn expectedEffect
            }
        )

        assertThat(actionScopeReduceBuilder.buildEffect(), equalTo(expectedEffect))
    }

    @Test
    fun `Given transition block, When transition, Then added to delegate`() {
        val transition: TestDslTransitionBlock = { current }
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(delegate)

        actionScopeReduceBuilder.transition(transition)
        verify(delegate, once()).addTransition(transition)
    }

    @Test
    fun `Given state type and transition block, When transition, Then added to delegate`() {
        val transition: DslTransitionScope<TestAction, TestState.State1>.() -> TestState = { current }
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(delegate)

        actionScopeReduceBuilder.transition<TestState.State1>(transition)
        verify(delegate, once()).addTransitionWithStateType(TestState.State1::class, transition)
    }

    @Test
    fun `Given effect block, When effect, Then added to delegate`() {
        val effect: TestDslEffectBlock = {}
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(delegate)

        actionScopeReduceBuilder.effect(effect)
        verify(delegate, once()).addEffect(effect)
    }

    @Test
    fun `Given state type and effect block, When effect, Then added to delegate`() {
        val effect: suspend DslEffectScope<TestAction, TestState.State1, TestAction>.() -> Unit = {}
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val actionScopeReduceBuilder = TestActionScopeReduceBuilder(delegate)

        actionScopeReduceBuilder.effect<TestState.State1>(effect)
        verify(delegate, once()).addEffectWithStateType(TestState.State1::class, effect)
    }

    @Test
    fun `When scope with predicate, Then launched effect optionally`() = runTest {
        suspend fun testScopeWithPredicate(
            predicateResult: Boolean
        ) {
            val onEffect: suspend () -> Unit = mock()
            val compositeEffect = TestActionScopeReduceBuilder()
                .apply {
                    scope(predicate = { predicateResult }) {
                        effect { onEffect.invoke() }
                    }
                }
                .buildEffect()
            compositeEffect.invoke(
                DslEffectScope(
                    UpdateSource(TestAction.genAction(), TestState.genState()),
                    mock()
                )
            )
        }
        testScopeWithPredicate(predicateResult = true)
        testScopeWithPredicate(predicateResult = false)
    }
}