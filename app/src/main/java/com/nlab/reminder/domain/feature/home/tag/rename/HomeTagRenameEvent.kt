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

import com.nlab.reminder.core.state.Event
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.state.core.lifecycle.PublicEvent

sealed class HomeTagRenameEvent private constructor() : Event {
    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeTagRenameViewModel::class)
    data class OnRenameTextInput(val text: String) : HomeTagRenameEvent()

    @PublicEvent(HomeTagRenameViewModel::class)
    object OnRenameTextClearClicked : HomeTagRenameEvent()

    @PublicEvent(HomeTagRenameViewModel::class)
    object OnKeyboardShownWhenViewCreated : HomeTagRenameEvent()

    @PublicEvent(HomeTagRenameViewModel::class)
    object OnCancelClicked : HomeTagRenameEvent()

    @PublicEvent(HomeTagRenameViewModel::class)
    object OnConfirmClicked : HomeTagRenameEvent()
}