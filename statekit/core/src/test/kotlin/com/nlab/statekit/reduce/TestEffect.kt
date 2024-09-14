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

/**
 * @author Doohyun
 */
typealias TestNodeEffect = Effect.NodeEffect<TestAction, TestState>
typealias TestCompositeEffect = Effect.CompositeEffect<TestAction, TestState>

@Suppress("TestFunctionName")
fun TestNodeEffect(
    needInvoke: (TestAction, TestState) -> Boolean = { _, _ -> true },
    invoke: suspend (TestAction, TestState, ActionDispatcher<TestAction>) -> Unit
): TestNodeEffect = object : TestNodeEffect {
    override fun needInvoke(action: TestAction, current: TestState): Boolean = needInvoke(action, current)

    override suspend fun invoke(
        action: TestAction,
        current: TestState,
        actionDispatcher: ActionDispatcher<TestAction>
    ) {
        invoke(action, current, actionDispatcher)
    }
}