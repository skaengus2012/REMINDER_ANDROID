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
import com.nlab.reminder.core.state.UserMessage

/**
 * @author Doohyun
 */
@Composable
fun UserMessageEffect(
    messages: List<UserMessage>,
    onMessageReleased: (UserMessage) -> Unit,
    block: suspend UserMessageEffectScope.() -> Unit
) {
    val context = LocalContext.current
    messages.firstOrNull()?.let { userMessage ->
        LaunchedEffect(userMessage) {
            val scope = UserMessageEffectScope(
                displayMessage = when (userMessage) {
                    is UserMessage.ResIdValue -> context.getString(userMessage.value)
                }
            )
            block(scope)
            onMessageReleased(userMessage)
        }
    }
}

@JvmInline
value class UserMessageEffectScope(val displayMessage: String)