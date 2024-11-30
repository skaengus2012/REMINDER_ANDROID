/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.usermessage.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.UserMessage.*

/**
 * @author Doohyun
 */
@Composable
fun UserMessageHandler(
    messages: List<UserMessage>,
    onMessageReleased: (UserMessage) -> Unit,
    block: suspend String.() -> Unit
) {
    messages.firstOrNull()?.let { message ->
        val displayMessage = message.toDisplayMessage()
        LaunchedEffect(displayMessage) {
            block(displayMessage)
            onMessageReleased(message)
        }
    }
}

@ReadOnlyComposable
@Composable
private fun UserMessage.toDisplayMessage(): String = when (this) {
    is Default -> value
    is ResId -> {
        if (args == null) stringResource(resId)
        else stringResource(resId, *args)
    }

    is PluralsResId -> {
        if (args == null) pluralStringResource(resId, count)
        else pluralStringResource(resId, count, *args)
    }
}

@JvmInline
value class UserMessageEffectScope(val displayMessage: String)