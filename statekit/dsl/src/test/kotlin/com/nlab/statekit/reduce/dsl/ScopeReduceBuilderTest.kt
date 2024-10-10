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
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.verification.VerificationMode

private typealias TestScopeReduceBuilder = ScopeReduceBuilder<TestAction, TestState, TestAction, TestState>

/**
 * @author Doohyun
 */
class ScopeReduceBuilderTest {
    @Test
    fun `When create scope without external delegate, Then success`() {
        TestScopeReduceBuilder()
    }

    @Test
    fun `When build transition, Then get from delegate`() {
        val expectedTransition: TestDslTransitionBlock = { current }
        val scopeReduceBuilder = TestScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildTransition()) doReturn expectedTransition
            }
        )

        assertThat(scopeReduceBuilder.buildTransition(), equalTo(expectedTransition))
    }

    @Test
    fun `When build effect, Then get from delegate`() {
        val expectedEffect: TestDslEffectBlock = {}
        val scopeReduceBuilder = TestScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildEffect()) doReturn expectedEffect
            }
        )

        assertThat(scopeReduceBuilder.buildEffect(), equalTo(expectedEffect))
    }

    @Test
    fun `Given transition block, When transition, Then added to delegate`() {
        val transition: TestDslTransitionBlock = { current }
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val scopeReduceBuilder = TestScopeReduceBuilder(delegate)

        scopeReduceBuilder.transition(transition)
        verify(delegate, once()).addTransition(transition)
    }

    @Test
    fun `Given effect block, When effect, Then added to delegate`() {
        val effect: TestDslEffectBlock = {}
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val scopeReduceBuilder = TestScopeReduceBuilder(delegate)

        scopeReduceBuilder.effect(effect)
        verify(delegate, once()).addEffect(effect)
    }

    @Test
    fun `When scope with predicate, Then launched effect optionally`() = runTest {
        suspend fun testScopeWithPredicate(
            predicateResult: Boolean
        ) {
            testScopeScopeReduceBuilder(
                setupReduce = { mockEffect ->
                    scope(predicate = { predicateResult }) {
                        effect { mockEffect.invoke() }
                    }
                },
                verificationMode = if (predicateResult) once() else never()
            )
        }

        testScopeWithPredicate(predicateResult = true)
        testScopeWithPredicate(predicateResult = false)
    }

    @Test
    fun `When scope with transform source, Then launched effect optionally`() = runTest {
        suspend fun testScopeWithTransformSource(
            canSourceConvert: Boolean
        ) {
            testScopeScopeReduceBuilder(
                setupReduce = { mockEffect ->
                    scope(transformSource = { if (canSourceConvert) this else null }) {
                        effect { mockEffect.invoke() }
                    }
                },
                verificationMode = if (canSourceConvert) once() else never()
            )
        }

        testScopeWithTransformSource(canSourceConvert = true)
        testScopeWithTransformSource(canSourceConvert = false)
    }

    @Test
    fun `When actionScope, Then launched effect optionally`() = runTest {
        testScopeScopeReduceBuilder(
            setupReduce = { mockEffect ->
                actionScope {
                    effect { mockEffect.invoke() }
                }
            },
            verificationMode = once()
        )
    }

    @Test
    fun `When actionScope with action type, Then launched effect optionally`() = runTest {
        suspend fun testActionScopeWithActionType(
            isInputActionMatched: Boolean
        ) {

        }
    }
}

private suspend fun testScopeScopeReduceBuilder(
    inputAction: TestAction = TestAction.genAction(),
    inputState: TestState = TestState.genState(),
    setupReduce: TestScopeReduceBuilder.(mockEffect: () -> Unit) -> Unit,
    verificationMode: VerificationMode
) {
    val runnable: () -> Unit = mock()
    val compositeEffect = TestScopeReduceBuilder()
        .apply { setupReduce(runnable) }
        .buildEffect()
    compositeEffect.invoke(
        DslEffectScope(UpdateSource(inputAction, inputState), mock()),
    )
    verify(runnable, verificationMode).invoke()
}