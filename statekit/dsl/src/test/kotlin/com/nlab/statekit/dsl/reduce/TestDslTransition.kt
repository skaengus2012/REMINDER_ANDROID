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
import com.nlab.statekit.reduce.AccumulatorPool
import com.nlab.statekit.reduce.transitionTo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
typealias TestDslTransitionScope = DslTransitionScope<TestAction, TestState>
internal typealias TestDslTransitionNode = DslTransition.Node<TestState, TestAction, TestState>

@Suppress("TestFunctionName")
internal fun TestDslTransition(
    scope: Any = Any()
): DslTransition = TestDslTransitionNode(scope)

@Suppress("TestFunctionName")
internal fun TestDslTransitionNode(
    scope: Any = Any()
): TestDslTransitionNode = TestDslTransitionNode(scope) { it.current }

internal fun DslTransition.assert(
    inputAction: TestAction = TestAction.genAction(),
    inputState: TestState = TestState.genState(),
    expectedState: TestState
) {
    val transition = transitionOf<TestAction, TestState>(dslTransition = this)
    assertThat(
        transition.transitionTo(inputAction, inputState, AccumulatorPool()),
        equalTo(expectedState)
    )
}