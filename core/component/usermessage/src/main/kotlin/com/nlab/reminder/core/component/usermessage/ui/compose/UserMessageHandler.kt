/*
 * Copyright (C) 2025 The N's lab Open Source Project
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
import com.nlab.reminder.core.component.usermessage.FeedbackPriority
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.UserMessageId
import com.nlab.reminder.core.text.ui.compose.toText
import kotlinx.coroutines.CoroutineScope

/**
 * @author Doohyun
 */
@Composable
fun UserMessageHandler(
    userMessage: UserMessage?,
    onUserMessageShown: (UserMessageId) -> Unit,
    showUserMessage: suspend CoroutineScope.(messageText: String, priority: FeedbackPriority) -> Unit,
) {
    if (userMessage == null) return
    val messageText = userMessage.message.toText()
    LaunchedEffect(userMessage.id) {
        showUserMessage(messageText, userMessage.priority)
        onUserMessageShown(userMessage.id)
    }
}

@Composable
fun UserMessageHandler(
    userMessage: UserMessage?,
    onUserMessageShown: (UserMessageId) -> Unit,
    showUserMessage: suspend CoroutineScope.(messageText: String) -> Unit,
) {
    UserMessageHandler(
        userMessage = userMessage,
        onUserMessageShown = onUserMessageShown,
        showUserMessage = { messageText, _ -> showUserMessage(messageText) }
    )
}