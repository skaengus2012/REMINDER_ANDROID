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

import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLetterify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeTagRenameStateMachineKtTest {
    private fun genDummyActions(): Set<HomeTagRenameAction> = setOf(
        HomeTagRenameAction.OnConfirmClicked,
        HomeTagRenameAction.OnCancelClicked,
        HomeTagRenameAction.OnRenameTextInput(text = genBothify())
    )

    private fun createStateMachine(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
        initState: HomeTagRenameState = genHomeTagRenameState(),
        homeTagRenameSideEffect: SendHomeTagRenameSideEffect = mock()
    ): HomeTagRenameStateMachine = HomeTagRenameStateMachine(scope, initState, homeTagRenameSideEffect)

    @Test
    fun `holds init state when machine created by factory`() {
        val initText: String = genBothify()
        val stateMachine: HomeTagRenameStateMachine =
            HomeTagRenameStateMachineFactory(initText)
                .create(scope = CoroutineScope(Dispatchers.Unconfined), homeTagRenameSideEffect = mock())
        assertThat(
            stateMachine.state.value,
            equalTo(
                HomeTagRenameState(
                    currentText = initText,
                    isKeyboardShowWhenViewCreated = true
                )
            )
        )
    }

    @Test
    fun `state equals when simple event inputted`() = runTest {
        val initState = genHomeTagRenameState()
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(initState = initState)
        genDummyActions()
            .filterNot { it is HomeTagRenameAction.OnRenameTextInput }
            .filterNot { it is HomeTagRenameAction.OnKeyboardShownWhenViewCreated }
            .forEach { action ->
                stateMachine
                    .send(action)
                    .join()
            }

        assertThat(stateMachine.state.value, equalTo(initState))
    }

    @Test
    fun `changed text state when renameTextInput sent`() = runTest {
        val changeText = genBothify(string = "????####")
        val initState: HomeTagRenameState = genHomeTagRenameState(currentText = genLetterify("???"))
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(
            initState = initState
        )
        stateMachine
            .send(HomeTagRenameAction.OnRenameTextInput(changeText))
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(initState.copy(currentText = changeText))
        )
    }

    @Test
    fun `cleared text state when clearEvent sent`() = runTest {
        val initState: HomeTagRenameState = genHomeTagRenameState(currentText = genLetterify("?"))
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(
            initState = initState
        )
        stateMachine
            .send(HomeTagRenameAction.OnRenameTextClearClicked)
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(initState.copy(currentText = ""))
        )
    }

    @Test
    fun `changed disable keyboard shown onViewCreated when keyboardShown sent`() = runTest {
        val initState: HomeTagRenameState = genHomeTagRenameState(isKeyboardShowWhenViewCreated = true)
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(
            initState = initState
        )
        stateMachine
            .send(HomeTagRenameAction.OnKeyboardShownWhenViewCreated)
            .join()
        assertThat(
            stateMachine.state.value,
            equalTo(initState.copy(isKeyboardShowWhenViewCreated = false))
        )
    }

    @Test
    fun `notify complete event when confirm clicked`() = runTest {
        val changeText = genBothify()
        val sideEffect: SendHomeTagRenameSideEffect = mock()
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(
            initState = genHomeTagRenameState(currentText = changeText),
            homeTagRenameSideEffect = sideEffect
        )
        stateMachine
            .send(HomeTagRenameAction.OnConfirmClicked)
            .join()
        verify(sideEffect, times(1))
            .send(HomeTagRenameSideEffectMessage.Complete(changeText))
    }

    @Test
    fun `notify dismiss event when cancel clicked`() = runTest {
        val sideEffect: SendHomeTagRenameSideEffect = mock()
        val stateMachine: HomeTagRenameStateMachine = createStateMachine(homeTagRenameSideEffect = sideEffect)
        stateMachine
            .send(HomeTagRenameAction.OnCancelClicked)
            .join()
        verify(sideEffect, times(1)).send(HomeTagRenameSideEffectMessage.Dismiss)
    }
}