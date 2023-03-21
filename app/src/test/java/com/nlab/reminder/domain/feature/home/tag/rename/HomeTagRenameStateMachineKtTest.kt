/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.home.tag.rename

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.test.genStateContainerScope
import com.nlab.testkit.genBothify
import com.nlab.testkit.genLetterify
import com.nlab.testkit.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeTagRenameStateMachineKtTest {
    @Test
    fun `update currentText state when OnRenameTextInput sent`() = runTest {
        val changeText = genBothify(string = "????####")
        val initState: HomeTagRenameState = genHomeTagRenameState(currentText = genLetterify("???"))
        val stateContainer =
            genHomeTagRenameStateMachine()
                .asContainer(genStateContainerScope(), initState)
        stateContainer
            .send(HomeTagRenameEvent.OnRenameTextInput(changeText))
            .join()
        assertThat(
            stateContainer.stateFlow.value,
            equalTo(initState.copy(currentText = changeText))
        )
    }

    @Test
    fun `clear currentText state when OnRenameTextClearClicked sent`() = runTest {
        val initState: HomeTagRenameState = genHomeTagRenameState(currentText = genLetterify("?"))
        val stateContainer =
            genHomeTagRenameStateMachine()
                .asContainer(genStateContainerScope(), initState)
        stateContainer
            .send(HomeTagRenameEvent.OnRenameTextClearClicked)
            .join()
        assertThat(
            stateContainer.stateFlow.value,
            equalTo(initState.copy(currentText = ""))
        )
    }

    @Test
    fun `update disable keyboard shown onViewCreated when OnKeyboardShownWhenViewCreated sent`() = runTest {
        val initState: HomeTagRenameState = genHomeTagRenameState(isKeyboardShowWhenViewCreated = true)
        val stateContainer =
            genHomeTagRenameStateMachine()
                .asContainer(genStateContainerScope(), initState)
        stateContainer
            .send(HomeTagRenameEvent.OnKeyboardShownWhenViewCreated)
            .join()
        assertThat(
            stateContainer.stateFlow.value,
            equalTo(initState.copy(isKeyboardShowWhenViewCreated = false))
        )
    }

    @Test
    fun `notify complete when confirm clicked`() = runTest {
        val changeText = genBothify()
        val sideEffect: SideEffectHandle<HomeTagRenameSideEffect> = mock()
        val stateContainer =
            genHomeTagRenameStateMachine(sideEffect)
                .asContainer(genStateContainerScope(), genHomeTagRenameState(currentText = changeText))

        stateContainer
            .send(HomeTagRenameEvent.OnConfirmClicked)
            .join()
        verify(sideEffect, once()).post(HomeTagRenameSideEffect.Complete(changeText))
    }

    @Test
    fun `notify dismiss when cancel clicked`() = runTest {
        val sideEffect: SideEffectHandle<HomeTagRenameSideEffect> = mock()
        val stateContainer =
            genHomeTagRenameStateMachine(sideEffect)
                .asContainer(genStateContainerScope(), genHomeTagRenameState())
        stateContainer
            .send(HomeTagRenameEvent.OnCancelClicked)
            .join()
        verify(sideEffect, once()).post(HomeTagRenameSideEffect.Cancel)
    }
}