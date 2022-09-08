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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.reminder.core.effect.util.SideEffectController
import com.nlab.reminder.core.state.util.controlIn
import com.nlab.reminder.domain.feature.home.tag.rename.view.HomeTagRenameInitText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * @author Doohyun
 */
// TODO apply new viewEffect and make test  https://github.com/skaengus2012/REMINDER_ANDROID/issues/51
@HiltViewModel
class HomeTagRenameViewModel @Inject constructor(
    initText: HomeTagRenameInitText,
    stateMachineFactory: HomeTagRenameStateMachineFactory
) : ViewModel() {
    private val _homeTagRenameSideEffect: SendHomeTagRenameSideEffect = SideEffectController()
    private val stateController =
        stateMachineFactory.create(_homeTagRenameSideEffect)
            .controlIn(
                viewModelScope,
                HomeTagRenameState(currentText = initText.value, isKeyboardShowWhenViewCreated = true)
            )

    val homeTagRenameSideEffect: HomeTagRenameSideEffect = SideEffectController()
    val state: StateFlow<HomeTagRenameState> = stateController.state

    fun invoke(event: HomeTagRenameEvent) {
        stateController.send(event)
    }
}