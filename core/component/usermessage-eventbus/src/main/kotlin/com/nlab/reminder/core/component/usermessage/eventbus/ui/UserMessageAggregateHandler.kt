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
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.core.component.usermessage.FeedbackPriority
import com.nlab.reminder.core.component.usermessage.UserMessageId
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateAction
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateEnvironment
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateReduce
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateUiState
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessagePostedFlow
import com.nlab.reminder.core.component.usermessage.ui.compose.UserMessageHandler
import com.nlab.statekit.androidx.lifecycle.store.compose.retainedStore
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap
import com.nlab.statekit.foundation.store.createStore
import kotlinx.coroutines.CoroutineScope


/**
 * @author Thalys
 */
@Composable
fun UserMessageAggregateHandler(
    showUserMessage: suspend CoroutineScope.(messageText: String, priority: FeedbackPriority) -> Unit,
    environment: UserMessageAggregateEnvironment = hiltViewModel()
) {
    val store = retainedStore {
        createStore(
            initState = UserMessageAggregateUiState(messages = emptyList()),
            reduce = UserMessageAggregateReduce(),
            bootstrap = UserMessagePostedFlow(userMessageMonitor = environment.userMessageMonitor)
                .collectAsBootstrap(started = DeliveryStarted.Lazily)
        )
    }

    val uiState by store.state.collectAsStateWithLifecycle()
    UserMessageAggregateHandler(
        uiState = uiState,
        userMessageShown = { shownId ->
            store.dispatch(UserMessageAggregateAction.UserMessageShown(shownId))
        },
        showUserMessage = showUserMessage
    )
}

@Composable
private fun UserMessageAggregateHandler(
    uiState: UserMessageAggregateUiState,
    userMessageShown: (UserMessageId) -> Unit,
    showUserMessage: suspend CoroutineScope.(messageText: String, priority: FeedbackPriority) -> Unit,
) {
    UserMessageHandler(
        userMessages = uiState.messages,
        onUserMessageShown = userMessageShown,
        showUserMessage = showUserMessage
    )
}