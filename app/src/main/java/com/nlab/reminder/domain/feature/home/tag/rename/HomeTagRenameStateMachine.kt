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

import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.util.StateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias HomeTagRenameStateMachine = StateMachine<HomeTagRenameAction, HomeTagRenameState>

fun HomeTagRenameStateMachine(
    scope: CoroutineScope,
    initState: HomeTagRenameState,
    homeTagRenameSideEffect: SendHomeTagRenameSideEffect
): HomeTagRenameStateMachine = StateMachine(scope, initState) {
    updateTo { (action, oldState) ->
        when (action) {
            is HomeTagRenameAction.OnRenameTextInput -> oldState.copy(currentText = action.text)
            is HomeTagRenameAction.OnRenameTextClearClicked -> oldState.copy(currentText = "")
            is HomeTagRenameAction.OnKeyboardShownWhenViewCreated -> {
                oldState.copy(isKeyboardShowWhenViewCreated = false)
            }

            else -> oldState
        }
    }

    sideEffectBy<HomeTagRenameAction.OnConfirmClicked> { (_, state) ->
        scope.launch { homeTagRenameSideEffect.complete(state.currentText) }
    }

    sideEffectBy<HomeTagRenameAction.OnCancelClicked> {
        scope.launch { homeTagRenameSideEffect.dismiss() }
    }
}