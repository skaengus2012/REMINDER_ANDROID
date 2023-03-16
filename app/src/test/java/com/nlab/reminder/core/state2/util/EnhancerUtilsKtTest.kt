/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.state2.util

import com.nlab.reminder.core.state2.TestAction
import com.nlab.reminder.core.state2.TestState
import com.nlab.reminder.core.state2.UpdateSource
import com.nlab.testkit.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class EnhancerUtilsKtTest {
    @Test
    fun testBuildEnhancer() = runTest {
        val work: () -> Unit = mock()
        val enhancer = buildEnhancer<TestAction, TestState> { work() }

        enhancer.invoke(mock(), UpdateSource(TestAction.genAction(), TestState.genState()))
        verify(work, once())()
    }

    @Test
    fun testEnhancerComposition() = runTest {
        val firstWork: () -> Unit = mock()
        val lastWork: () -> Unit = mock()

        val firstEnhancer = buildEnhancer<TestAction, TestState> { firstWork() }
        val lastEnhancer = buildEnhancer<TestAction, TestState> { lastWork() }
        val compositionEnhancer = firstEnhancer + lastEnhancer

        compositionEnhancer.invoke(mock(), UpdateSource(TestAction.genAction(), TestState.genState()))
        verify(firstWork, once())()
        verify(lastWork, once())()
    }
}