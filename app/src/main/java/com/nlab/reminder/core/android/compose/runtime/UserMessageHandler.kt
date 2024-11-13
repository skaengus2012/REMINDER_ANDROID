/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.android.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.nlab.reminder.core.uistate.UserMessage

/**
 * @author Doohyun
 */
@Composable
inline fun UserMessageHandler(
    messages: List<UserMessage>,
    crossinline onMessageReleased: (UserMessage) -> Unit,
    crossinline block: suspend UserMessageEffectScope.() -> Unit
) {
    messages.firstOrNull()?.let { message ->
        val context = LocalContext.current
        LaunchedEffect(message) {
            val displayMessage = when (message) {
                is UserMessage.ResIdValue -> context.getString(message.value)
            }
            block(UserMessageEffectScope(displayMessage))
            onMessageReleased(message)
        }
    }
}

@JvmInline
value class UserMessageEffectScope(val displayMessage: String)