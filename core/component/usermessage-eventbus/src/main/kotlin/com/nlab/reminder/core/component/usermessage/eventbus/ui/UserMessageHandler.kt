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

package com.nlab.reminder.core.component.usermessage.eventbus.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageHandleViewModel
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageUiState
import com.nlab.reminder.core.component.usermessage.eventbus.userMessageShown
import com.nlab.reminder.core.text.ui.compose.toText


/**
 * @author Thalys
 */
@Composable
fun UserMessageHandler(
    showApplicationToast: (String) -> Unit,
    viewModel: UserMessageHandleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UserMessageHandler(
        uiState = uiState,
        userMessageShown = { viewModel.userMessageShown(it) },
        showApplicationToast = showApplicationToast
    )
}

@Composable
private fun UserMessageHandler(
    uiState: UserMessageUiState,
    userMessageShown: (UserMessage) -> Unit,
    showApplicationToast: (String) -> Unit,
) {
    val userMessage = uiState.userMessages.firstOrNull() ?: return
    val messageText = userMessage.message.toText()
    LaunchedEffect(userMessage) {
        showApplicationToast(messageText) // TODO implements user message with priority
        userMessageShown(userMessage)
    }
}