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
import com.nlab.reminder.core.state.StateContainer
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.asContainer
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
        val sampleEvent = genHomeTagRenameEventSample()
        val stateContainer: StateContainer<HomeTagRenameEvent, HomeTagRenameState> = mock()
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock {
                whenever(mock.create(any(), any())) doReturn stateContainer
            }
        )

        viewModel.send(sampleEvent)
        verify(stateContainer, once()).send(sampleEvent)
    }

    @Test
    fun `notify state when stateController flow published`() {
        val sampleState = genHomeTagRenameStateSample()
        val stateContainer: StateContainer<HomeTagRenameEvent, HomeTagRenameState> = mock {
            whenever(mock.stateFlow) doReturn MutableStateFlow(sampleState)
        }
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = mock { whenever(mock.create(any(), any())) doReturn stateContainer }
        )

        assertThat(viewModel.stateFlow.value, equalTo(sampleState))
    }

    @Test
    fun `notify sideEffect when stateController invoke sideEffect`() = runTest {
        val sampleSideEffect = genHomeSideEffectSample()
        val viewModel = HomeTagRenameViewModel(
            stateControllerFactory = object : HomeTagRenameStateContainerFactory {
                override fun create(
                    scope: CoroutineScope,
                    homeTagRenameSideEffect: SideEffectHandle<HomeTagRenameSideEffect>
                ): StateContainer<HomeTagRenameEvent, HomeTagRenameState> {
                    val fakeStateMachine: StateMachine<HomeTagRenameEvent, HomeTagRenameState> = StateMachine {
                        handle {
                            anyEvent {
                                anyState { homeTagRenameSideEffect.post(sampleSideEffect) }
                            }
                        }
                    }
                    return fakeStateMachine.asContainer(scope, genHomeTagRenameState())
                }
            }
        )
        val sideEffectHandler: () -> Unit = mock()

        viewModel.homeTagRenameSideEffectFlow
            .onEach { sideEffectHandler() }
            .launchIn(genFlowObserveDispatcher())
        viewModel
            .send(genHomeTagRenameEventSample())
            .join()
        verify(sideEffectHandler, once())()
    }
}