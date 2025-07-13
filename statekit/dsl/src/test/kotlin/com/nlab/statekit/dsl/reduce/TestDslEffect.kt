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
import com.nlab.statekit.reduce.NodeStackPool
import com.nlab.statekit.dispatch.ActionDispatcher
import com.nlab.statekit.reduce.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
typealias TestDslEffectScope = DslEffectScope<TestAction, TestAction>
typealias TestDslSuspendEffectScope = DslSuspendEffectScope<TestAction, TestAction, TestState>
internal typealias TestDslEffectNode = DslEffect.Node<TestAction, TestState>
internal typealias TestDslEffectSuspendNode = DslEffect.SuspendNode<TestAction, TestAction, TestState>

@Suppress("TestFunctionName")
internal fun TestDslEffect(scope: Any = Any()): DslEffect = TestDslEffectNode(scope)

@Suppress("TestFunctionName")
internal fun TestDslEffectNode(scope: Any = Any()): TestDslEffectNode = TestDslEffectNode(scope) {}

@Suppress("TestFunctionName")
internal fun TestDslEffectSuspendNode(
    scope: Any = Any()
): TestDslEffectSuspendNode = TestDslEffectSuspendNode(scope) {}

/**
internal suspend fun DslEffect.launchAndJoinForTest(
    action: TestAction = TestAction.genAction(),
    state: TestState = TestState.genState(),
    actionDispatcher: ActionDispatcher<TestAction> = mock(),
    accPool: NodeStackPool = NodeStackPool(),
) {
    coroutineScope {
        effectOf<TestAction, TestState>(dslEffect = this@launchAndJoinForTest).launch(
            action,
            state,
            actionDispatcher,
            accPool,
            coroutineScope = this
        )
    }
}

internal fun DslEffect.launchForTest(
    action: TestAction = TestAction.genAction(),
    state: TestState = TestState.genState(),
    actionDispatcher: ActionDispatcher<TestAction> = mock(),
    accPool: NodeStackPool = NodeStackPool(),
    coroutineScope: CoroutineScope
) {
    effectOf<TestAction, TestState>(dslEffect = this@launchForTest).launch(
        action,
        state,
        actionDispatcher,
        accPool,
        coroutineScope
    )
}*/