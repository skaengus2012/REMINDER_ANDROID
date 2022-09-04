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

package com.nlab.reminder.test

import androidx.lifecycle.ViewModel
import com.nlab.reminder.core.state.State
import com.nlab.reminder.core.state.StateController
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

inline fun <S : State, VM : ViewModel, reified SM : StateController<*, S>, reified F> createMockingViewModelComponent(
    state: MutableStateFlow<S>,
    createViewModel: (F) -> VM,
    wheneverMocking: (F) -> SM,
): Triple<VM, SM, F> {
    val stateMachine: SM = mock { whenever(mock.state) doReturn state }
    val stateMachineFactory: F = mock { whenever(wheneverMocking(mock)) doReturn stateMachine }
    val viewModel: VM = createViewModel(stateMachineFactory)
    return Triple(viewModel, stateMachine, stateMachineFactory)
}