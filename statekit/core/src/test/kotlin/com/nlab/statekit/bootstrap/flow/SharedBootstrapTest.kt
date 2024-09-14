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

package com.nlab.statekit.bootstrap.flow

import com.nlab.statekit.TestAction
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedBackgroundScope
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
internal class SharedBootstrapTest {
    @Test
    fun `Given action stream, When fetch on shared bootstrap, Then action stream collected`() = runTest {
        val action = TestAction.genAction()
        val sharedBootstrap = SharedBootstrap(flowOf(action), SharingStarted.Lazily, replay = 0)
        val actionDispatcher = mock<ActionDispatcher<TestAction>>()
        sharedBootstrap.fetch(
            coroutineScope = unconfinedBackgroundScope,
            actionDispatcher = actionDispatcher,
            stateSubscriptionCount = MutableStateFlow(value = 1)
        )
        advanceUntilIdle()

        verify(actionDispatcher, once()).dispatch(action)
    }
}