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

private typealias TestStateScopeReduceBuilder = StateScopeReduceBuilder<TestAction, TestState, TestAction, TestState>

/**
 * @author Doohyun
 */
class StateScopeReduceBuilderTest {
    @Test
    fun `When create scope without external delegate, Then success`() {
        TestStateScopeReduceBuilder()
    }

    @Test
    fun `When build transition, Then get from delegate`() {
        val expectedTransition: TestDslTransitionBlock = { current }
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildTransition()) doReturn expectedTransition
            }
        )

        assertThat(stateScopeReduceBuilder.buildTransition(), equalTo(expectedTransition))
    }

    @Test
    fun `When build effect, Then get from delegate`() {
        val expectedEffect: TestDslEffectBlock = {}
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(
            delegate = mock {
                whenever(mock.buildEffect()) doReturn expectedEffect
            }
        )

        assertThat(stateScopeReduceBuilder.buildEffect(), equalTo(expectedEffect))
    }

    @Test
    fun `Given transition block, When transition, Then added to delegate`() {
        val transition: TestDslTransitionBlock = { current }
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(delegate)

        stateScopeReduceBuilder.transition(transition)
        verify(delegate, once()).addTransition(transition)
    }


    @Test
    fun `Given action type and transition block, When transition, Then added to delegate`() {
        val transition: DslTransitionScope<TestAction.Action1, TestState>.() -> TestState = { current }
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(delegate)

        stateScopeReduceBuilder.transition<TestAction.Action1>(transition)
        verify(delegate, once()).addTransitionWithActionType(TestAction.Action1::class, transition)
    }

    @Test
    fun `Given effect block, When effect, Then added to delegate`() {
        val effect: TestDslEffectBlock = {}
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(delegate)

        stateScopeReduceBuilder.effect(effect)
        verify(delegate, once()).addEffect(effect)
    }

    @Test
    fun `Given action type and effect block, When effect, Then added to delegate`() {
        val effect: suspend DslEffectScope<TestAction.Action1, TestState, TestAction>.() -> Unit = {}
        val delegate = mock<TestDslReduceBuilderDelegate>()
        val stateScopeReduceBuilder = TestStateScopeReduceBuilder(delegate)

        stateScopeReduceBuilder.effect<TestAction.Action1>(effect)
        verify(delegate, once()).addEffectWithActionType(TestAction.Action1::class, effect)
    }

    @Test
    fun `When scope with predicate, Then launched effect optionally`() = runTest {
        suspend fun testScopeWithPredicate(
            predicateResult: Boolean
        ) {
            testScopeInStateScopeReduceBuilder(
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
            testScopeInStateScopeReduceBuilder(
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
    fun `When scope with state type, Then launched effect optionally`() = runTest {
        suspend fun testScopeWithActionType(
            isInputActionMatched: Boolean
        ) {
            val inputState = TestState.State1
            testScopeInStateScopeReduceBuilder(
                inputState = inputState,
                setupReduce = { mockEffect ->
                    if (isInputActionMatched) {
                        scope<TestState.State1> {
                            effect { mockEffect.invoke() }
                        }
                    } else {
                        scope<TestState.State2> {
                            effect { mockEffect.invoke() }
                        }
                    }
                },
                verificationMode = if (isInputActionMatched) once() else never()
            )
        }
        testScopeWithActionType(isInputActionMatched = true)
        testScopeWithActionType(isInputActionMatched = false)
    }
}

private suspend fun testScopeInStateScopeReduceBuilder(
    inputState: TestState = TestState.genState(),
    setupReduce: TestStateScopeReduceBuilder.(mockEffect: () -> Unit) -> Unit,
    verificationMode: VerificationMode
) {
    val runnable: () -> Unit = mock()
    val compositeEffect = TestStateScopeReduceBuilder()
        .apply { setupReduce(runnable) }
        .buildEffect()
    compositeEffect.invoke(
        DslEffectScope(UpdateSource(TestAction.genAction(), inputState), mock()),
    )
    verify(runnable, verificationMode).invoke()
}