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

import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeTagRenameViewModelTest {
    /**
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
        val stateContainer: StateContainer<HomeTagRenameEvent, HomeTagRenameState> = mock()
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock {
                whenever(mock.create(any(), any())) doReturn stateContainer
            }
        )

        viewModel.invoke(sampleEvent)
        verify(stateContainer, once()).send(sampleEvent)
    }

    @Test
    fun `notify state when stateController flow published`() {
        val stateContainer: StateContainer<HomeTagRenameEvent, HomeTagRenameState> = mock {
            whenever(mock.stateFlow) doReturn MutableStateFlow(sampleState)
        }
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock { whenever(mock.create(any(), any())) doReturn stateContainer }
        )

        assertThat(viewModel.state.value, equalTo(sampleState))
    }

    @Test
    fun `notify sideEffect when stateController invoke sideEffect`() = runTest {
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = object : HomeTagRenameStateContainerFactory {
                override fun create(
                    scope: CoroutineScope,
                    homeTagRenameSideEffect: SideEffectSender<HomeTagRenameSideEffect>
                ): StateContainer<HomeTagRenameEvent, HomeTagRenameState> {
                    val fakeStateMachine: StateMachine<HomeTagRenameEvent, HomeTagRenameState> = StateMachine {
                        handle { homeTagRenameSideEffect.post(sampleEffect) }
                    }
                    return fakeStateMachine.asContainer(scope, genHomeTagRenameState())
                }
            }
        )
        val sideEffectHandler: () -> Unit = mock()

        viewModel.homeTagRenameSideEffect.flow
            .onEach { sideEffectHandler() }
            .launchIn(genFlowObserveDispatcher())
        viewModel.invoke(sampleEvent).join()
        verify(sideEffectHandler, once())()
    }*/
}