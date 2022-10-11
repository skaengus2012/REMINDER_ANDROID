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
fun HomeTagRenameStateMachine(
    sideEffectHandle: SideEffectHandle<HomeTagRenameSideEffect>
) = StateMachine<HomeTagRenameEvent, HomeTagRenameState> {
    reduce {
        anyState {
            event<HomeTagRenameEvent.OnRenameTextInput> { (event, before) -> before.copy(currentText = event.text) }
            event<HomeTagRenameEvent.OnRenameTextClearClicked> { (_, before) -> before.copy(currentText = "") }
            event<HomeTagRenameEvent.OnKeyboardShownWhenViewCreated> { (_, before) ->
                before.copy(isKeyboardShowWhenViewCreated = false)
            }
        }
    }

    handle {
        anyState {
            event<HomeTagRenameEvent.OnConfirmClicked> { (_, before) ->
                sideEffectHandle.post(HomeTagRenameSideEffect.Complete(before.currentText))
            }
            event<HomeTagRenameEvent.OnCancelClicked> { sideEffectHandle.post(HomeTagRenameSideEffect.Cancel) }
        }
    }
}