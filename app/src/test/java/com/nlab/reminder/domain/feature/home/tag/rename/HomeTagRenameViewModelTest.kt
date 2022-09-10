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

import com.nlab.reminder.core.effect.SideEffectSender
import com.nlab.reminder.core.state.StateController
import com.nlab.reminder.core.state.util.StateMachine
import com.nlab.reminder.core.state.util.controlIn
import com.nlab.reminder.test.genFlowObserveDispatcher
import com.nlab.reminder.test.once
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeTagRenameViewModelTest {
    private val sampleEvent: HomeTagRenameEvent = HomeTagRenameEvent.OnRenameTextClearClicked
    private val sampleState: HomeTagRenameState = genHomeTagRenameState()
    private val sampleEffect: HomeTagRenameSideEffect = HomeTagRenameSideEffect.Cancel

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stateController send event when viewModel sent`() {
        val stateController: StateController<HomeTagRenameEvent, HomeTagRenameState> = mock()
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock {
                whenever(mock.create(any(), any())) doReturn stateController
            }
        )

        viewModel.invoke(sampleEvent)
        verify(stateController, once()).send(sampleEvent)
    }

    @Test
    fun `notify state when stateController flow published`() {
        val stateController: StateController<HomeTagRenameEvent, HomeTagRenameState> = mock {
            whenever(mock.state) doReturn MutableStateFlow(sampleState)
        }
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock { whenever(mock.create(any(), any())) doReturn stateController }
        )

        assertThat(viewModel.state.value, equalTo(sampleState))
    }

    @Test
    fun `notify sideEffect when stateController invoke sideEffect`() = runTest {
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = object : HomeTagRenameStateControllerFactory {
                override fun create(
                    scope: CoroutineScope,
                    homeTagRenameSideEffect: SideEffectSender<HomeTagRenameSideEffect>
                ): StateController<HomeTagRenameEvent, HomeTagRenameState> {
                    val fakeStateMachine: StateMachine<HomeTagRenameEvent, HomeTagRenameState> = StateMachine {
                        handle { homeTagRenameSideEffect.post(sampleEffect) }
                    }
                    return fakeStateMachine.controlIn(scope, genHomeTagRenameState())
                }
            }
        )
        val sideEffectHandler: () -> Unit = mock()

        viewModel.homeTagRenameSideEffect.flow
            .onEach { sideEffectHandler() }
            .launchIn(genFlowObserveDispatcher())
        viewModel.invoke(sampleEvent).join()
        verify(sideEffectHandler, once())()
    }
}