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
import com.nlab.reminder.core.state.StateMachine

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun HomeTagRenameStateComponent(
    homeTagRenameSideEffect: SideEffectHandle<HomeTagRenameSideEffect>
) = StateMachine<HomeTagRenameEvent, HomeTagRenameState> {
    update { (event, state) ->
        when (event) {
            is HomeTagRenameEvent.OnRenameTextInput -> state.copy(currentText = event.text)
            is HomeTagRenameEvent.OnRenameTextClearClicked -> state.copy(currentText = "")
            is HomeTagRenameEvent.OnKeyboardShownWhenViewCreated -> {
                state.copy(isKeyboardShowWhenViewCreated = false)
            }
            else -> state
        }
    }

    handleBy<HomeTagRenameEvent.OnConfirmClicked> { (_, state) ->
        homeTagRenameSideEffect.handle(HomeTagRenameSideEffect.Complete(state.currentText))
    }

    handleBy<HomeTagRenameEvent.OnCancelClicked> {
        homeTagRenameSideEffect.handle(HomeTagRenameSideEffect.Cancel)
    }
}