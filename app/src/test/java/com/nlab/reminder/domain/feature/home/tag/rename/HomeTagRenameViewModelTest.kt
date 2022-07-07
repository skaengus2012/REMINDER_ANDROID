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

import com.nlab.reminder.test.createMockingViewModelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeTagRenameViewModelTest {
    private fun createViewModel(initText: String = ""): HomeTagRenameViewModel {
        return HomeTagRenameViewModel(HomeTagRenameStateMachineFactory(initText))
    }

    @Before
    fun init() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun `notify action to stateMachine when viewModel action invoked`() {
        val (viewModel, stateMachine) = createMockingViewModelComponent(
            MutableStateFlow(
                HomeTagRenameState(
                    currentText = "",
                    isKeyboardShowWhenViewCreated = true
                )
            ),
            createViewModel = { HomeTagRenameViewModel(it) },
            wheneverMocking = { factory: HomeTagRenameStateMachineFactory ->
                factory.create(scope = any(), homeTagRenameSideEffect = any())
            }
        )
        val action = HomeTagRenameAction.OnKeyboardShownWhenViewCreated
        viewModel.onAction(action)
        verify(stateMachine, times(1)).send(action)
    }

    @Test
    fun `notify changed state when state event sent`() = runTest {
        val actualHomeRenameState = mutableListOf<HomeTagRenameState>()
        val initText = "Hello"
        val expectedInitState = HomeTagRenameState(
            currentText = initText,
            isKeyboardShowWhenViewCreated = true
        )
        val viewModel: HomeTagRenameViewModel = createViewModel(initText)
        CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect(actualHomeRenameState::add) }
        viewModel.onKeyboardShownWhenViewCreated()
        assertThat(
            actualHomeRenameState,
            equalTo(buildList {
                add(expectedInitState)
                add(expectedInitState.copy(isKeyboardShowWhenViewCreated = false))
            })
        )
    }

    @Test
    fun `notify sideEffect message when sideEffect event sent`() = runTest {
        val inputText = "test"
        val viewModel: HomeTagRenameViewModel = createViewModel()
        viewModel.onRenameTextInput(inputText)
        viewModel.onConfirmClicked()
        viewModel.onRenameTextClearClicked()
        viewModel.onConfirmClicked()
        viewModel.onCancelClicked()
        assertThat(
            viewModel.homeTagRenameSideEffect
                .event
                .take(3)
                .toList(),
            equalTo(
                listOf(
                    HomeTagRenameSideEffectMessage.Complete(inputText),
                    HomeTagRenameSideEffectMessage.Complete(rename = ""),
                    HomeTagRenameSideEffectMessage.Dismiss,
                )
            )
        )
    }
}