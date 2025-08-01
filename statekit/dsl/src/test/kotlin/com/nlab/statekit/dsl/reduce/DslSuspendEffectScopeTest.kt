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
import com.nlab.statekit.dispatch.ActionDispatcher
import io.mockk.mockk
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class DslSuspendEffectScopeTest {
    @Test
    fun successCreateDslEffectScope() {
        DslSuspendEffectScope(
            UpdateSource(TestAction.genAction(), TestState.genState()),
            actionDispatcher = mockk<ActionDispatcher<TestAction>>()
        )
    }

    @Test
    fun testGetActionDispatcher() {
        val expectedActionDispatcher = object : ActionDispatcher<TestAction> {
            override suspend fun dispatch(action: TestAction) = Unit
        }
        val scope = DslSuspendEffectScope(
            UpdateSource(TestAction.genAction(), TestState.genState()),
            actionDispatcher = expectedActionDispatcher
        )
        assertThat(scope.actionDispatcher, sameInstance(expectedActionDispatcher))
    }
}