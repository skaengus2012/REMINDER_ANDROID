package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.verification.VerificationMode

/**
 * @author Doohyun
 */
class DslReduceBuilderDelegateTest {
    @Test
    fun `When build transition with addTransition, Then transition invoked`() {
        val inputState = TestState.State1
        val expectedNextState = TestState.State3
        testTransition(
            inputState = inputState,
            expectedNextState = expectedNextState,
            setupReduce = {
                addTransition { expectedNextState }
            }
        )
    }

    @Test
    fun `When build transition with addTransitionWithPredicate, Then transition invoked optionally`() {
        fun testAddTransitionWithPredicate(
            predicateResult: Boolean
        ) {
            val inputState = TestState.State1
            val transitionReturnState = TestState.State3
            testTransition(
                inputState = inputState,
                expectedNextState = if (predicateResult) transitionReturnState else inputState,
                setupReduce = {
                    addTransitionWithPredicate(
                        predicate = { predicateResult },
                        block = { transitionReturnState }
                    )
                }
            )
        }

        testAddTransitionWithPredicate(predicateResult = true)
        testAddTransitionWithPredicate(predicateResult = false)
    }

    @Test
    fun `When build transition with addTransitionWithTransformSource, Then transition invoked optionally`() {
        fun testAddTransitionWithTransformSource(
            canSourceConvert: Boolean
        ) {
            val inputState = TestState.State1
            val transitionReturnState = TestState.State3
            testTransition(
                inputState = inputState,
                expectedNextState = if (canSourceConvert) transitionReturnState else inputState,
                setupReduce = {
                    addTransitionWithTransformSource(
                        transformSource = { if (canSourceConvert) UpdateSource(genInt(), genInt()) else null },
                        block = { transitionReturnState }
                    )
                }
            )
        }

        testAddTransitionWithTransformSource(canSourceConvert = true)
        testAddTransitionWithTransformSource(canSourceConvert = false)
    }

    @Test
    fun `When build transition with addTransitionWithActionType, Then transition invoked optionally`() {
        fun testAddTransitionWithActionType(
            isInputActionMatched: Boolean
        ) {
            val inputAction = TestAction.Action1
            val inputState = TestState.State1
            val transitionReturnState = TestState.State3
            testTransition(
                inputAction = inputAction,
                inputState = inputState,
                expectedNextState = if (isInputActionMatched) transitionReturnState else inputState,
                setupReduce = {
                    addTransitionWithActionType(
                        actionType = if (isInputActionMatched) TestAction.Action1::class else TestAction.Action2::class,
                        block = { transitionReturnState }
                    )
                }
            )
        }
        testAddTransitionWithActionType(isInputActionMatched = true)
        testAddTransitionWithActionType(isInputActionMatched = false)
    }

    @Test
    fun `When build transition with addTransitionWithStateType, Then transition invoked optionally`() {
        fun testAddTransitionWithStateType(
            isInputStateMatched: Boolean
        ) {
            val inputState = TestState.State1
            val transitionReturnState = TestState.State3
            testTransition(
                inputState = inputState,
                expectedNextState = if (isInputStateMatched) transitionReturnState else inputState,
                setupReduce = {
                    addTransitionWithStateType(
                        stateType = if (isInputStateMatched) TestState.State1::class else TestState.State2::class,
                        block = { transitionReturnState }
                    )
                }
            )
        }
        testAddTransitionWithStateType(isInputStateMatched = true)
        testAddTransitionWithStateType(isInputStateMatched = false)
    }

    @Test
    fun `When build effect with addEffect, Then effect invoked`() = runTest {
        testEffect(
            setupReduce = { mockEffect ->
                addEffect { mockEffect.invoke() }
            },
            verificationMode = once()
        )
    }

    @Test
    fun `When build effect with addEffectWithPredicate, Then effect invoked optionally`() = runTest {
        suspend fun testAddEffectWithPredicate(
            predicateResult: Boolean
        ) {
            testEffect(
                setupReduce = { mockEffect ->
                    addEffectWithPredicate(
                        predicate = { predicateResult },
                        block = { mockEffect.invoke() }
                    )
                },
                verificationMode = if (predicateResult) once() else never()
            )
        }

        testAddEffectWithPredicate(predicateResult = true)
        testAddEffectWithPredicate(predicateResult = false)
    }

    @Test
    fun `When build effect with addEffectWithTransformSource, Then effect invoked optionally`() = runTest {
        suspend fun testAddEffectWithTransformSource(
            canSourceConvert: Boolean
        ) {
            testEffect(
                setupReduce = { mockEffect ->
                    addEffectWithTransformSource(
                        transformSource = { if (canSourceConvert) UpdateSource(genInt(), genInt()) else null },
                        block = { mockEffect.invoke() }
                    )
                },
                verificationMode = if (canSourceConvert) once() else never()
            )
        }

        testAddEffectWithTransformSource(canSourceConvert = true)
        testAddEffectWithTransformSource(canSourceConvert = false)
    }

    @Test
    fun `When build effect with addEffectWithActionType, Then effect invoked optionally`() = runTest {
        suspend fun testAddEffectWithActionType(
            isInputActionMatched: Boolean
        ) {
            val inputAction = TestAction.Action1
            testEffect(
                inputAction = inputAction,
                setupReduce = { mockEffect ->
                    addEffectWithActionType(
                        if (isInputActionMatched) TestAction.Action1::class else TestAction.Action2::class,
                        block = { mockEffect.invoke() }
                    )
                },
                verificationMode = if (isInputActionMatched) once() else never()
            )
        }
        testAddEffectWithActionType(isInputActionMatched = true)
        testAddEffectWithActionType(isInputActionMatched = false)
    }

    @Test
    fun `When build effect with addEffectWithStateType, Then effect invoked optionally`() = runTest {
        suspend fun testAddEffectWithStateType(
            isInputStateMatched: Boolean
        ) {
            val inputState = TestState.State1
            testEffect(
                inputState = inputState,
                setupReduce = { mockEffect ->
                    addEffectWithStateType(
                        if (isInputStateMatched) TestState.State1::class else TestState.State2::class,
                        block = { mockEffect.invoke() }
                    )
                },
                verificationMode = if (isInputStateMatched) once() else never()
            )
        }
        testAddEffectWithStateType(isInputStateMatched = true)
        testAddEffectWithStateType(isInputStateMatched = false)
    }
}

private fun testTransition(
    inputAction: TestAction = TestAction.genAction(),
    inputState: TestState,
    expectedNextState: TestState,
    setupReduce: TestDslReduceBuilderDelegate.() -> Unit
) {
    val compositeTransition = TestDslReduceBuilderDelegate()
        .apply { setupReduce() }
        .buildTransition()
    val actualState = compositeTransition.invoke(
        DslTransitionScope(UpdateSource(inputAction, inputState))
    )
    assertThat(actualState, equalTo(expectedNextState))
}

private suspend fun testEffect(
    inputAction: TestAction = TestAction.genAction(),
    inputState: TestState = TestState.genState(),
    setupReduce: TestDslReduceBuilderDelegate.(mockEffect: () -> Unit) -> Unit,
    verificationMode: VerificationMode
) {
    val runnable: () -> Unit = mock()
    val compositeEffect = TestDslReduceBuilderDelegate()
        .apply { setupReduce(runnable) }
        .buildEffect()
    compositeEffect.invoke(
        DslEffectScope(UpdateSource(inputAction, inputState), mock()),
    )
    verify(runnable, verificationMode).invoke()
}