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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
class TransitionKtTest {
    @Test
    fun `Given changeable and non changeable Transition, When transitionTo, Then invoke changeable only`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val changeable = TestNodeTransition { _, _ -> expectedState }
        val nonChangeable = mock<TestNodeTransition>()
        val actualState = TestCompositeTransition(changeable, nonChangeable).transitionTo(inputAction, inputState)

        assertThat(actualState, equalTo(expectedState))
        verify(nonChangeable, never()).next(inputAction, inputState)
    }

    @Test
    fun `Given non changeable and changeable Transition, When transitionTo, Then invoke all`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val nonChangeable = mock<TestNodeTransition> {
            whenever(mock.next(any(), any())) doReturn inputState
        }
        val changeable = mock<TestNodeTransition> {
            whenever(mock.next(any(), any())) doReturn expectedState
        }
        val actualState = TestCompositeTransition(nonChangeable, changeable).transitionTo(inputAction, inputState)

        assertThat(actualState, equalTo(expectedState))
        inOrder(nonChangeable, changeable) {
            verify(nonChangeable, once()).next(inputAction, inputState)
            verify(changeable, once()).next(inputAction, inputState)
        }
    }

    @Test
    fun `Given two non changeable transitions, When transitionTo, Then invoke all`() {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val nonChangeable1 = mock<TestNodeTransition> {
            whenever(mock.next(any(), any())) doReturn inputState
        }
        val nonChangeable2 = mock<TestNodeTransition> {
            whenever(mock.next(any(), any())) doReturn inputState
        }
        val actualState = TestCompositeTransition(nonChangeable1, nonChangeable2).transitionTo(inputAction, inputState)

        assertThat(actualState, equalTo(inputState))
        inOrder(nonChangeable1, nonChangeable2) {
            verify(nonChangeable1, once()).next(inputAction, inputState)
            verify(nonChangeable2, once()).next(inputAction, inputState)
        }
    }
}