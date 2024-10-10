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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.verification.VerificationMode

/**
 * @author Doohyun
 */
typealias TestDslTransitionBlock = (DslTransitionScope<TestAction, TestState>).() -> TestState
typealias TestDslEffectBlock = suspend (DslEffectScope<TestAction, TestState, TestAction>) -> Unit
internal typealias TestDslReduceBuilderDelegate = DslReduceBuilderDelegate<TestAction, TestState, TestAction, TestState>

internal fun testTransition(
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

internal suspend fun testEffect(
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