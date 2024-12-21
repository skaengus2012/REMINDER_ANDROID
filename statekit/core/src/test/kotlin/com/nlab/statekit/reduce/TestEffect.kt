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

package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.CoroutineScope
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
typealias TestEffect = Effect<TestAction, TestState>
typealias TestEffectNode = Effect.Node<TestAction, TestState>
typealias TestEffectSuspendNode = Effect.SuspendNode<TestAction, TestState>
typealias TestEffectLifecycleNode = Effect.LifecycleNode<TestAction, TestState>
typealias TestEffectComposite = Effect.Composite<TestAction, TestState>

internal fun TestEffect.launchForTest(
    action: TestAction = TestAction.genAction(),
    state: TestState = TestState.genState(),
    actionDispatcher: ActionDispatcher<TestAction> = mock(),
    accPool: AccumulatorPool = AccumulatorPool(),
    coroutineScope: CoroutineScope
) {
    launch(
        action,
        state,
        actionDispatcher,
        accPool,
        coroutineScope
    )
}