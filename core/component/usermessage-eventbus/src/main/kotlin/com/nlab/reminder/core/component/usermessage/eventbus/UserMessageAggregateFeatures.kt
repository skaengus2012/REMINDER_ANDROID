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

package com.nlab.reminder.core.component.usermessage.eventbus

import androidx.lifecycle.ViewModel
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.UserMessageId
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateAction.*
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

internal typealias UserMessageAggregateReduce = Reduce<UserMessageAggregateAction, UserMessageAggregateUiState>

/**
 * @author Thalys
 */
sealed class UserMessageAggregateAction {
    data class UserMessagePosted(val message: UserMessage) : UserMessageAggregateAction()
    data class UserMessageShown(val shownId: UserMessageId) : UserMessageAggregateAction()
}

data class UserMessageAggregateUiState(val messages: List<UserMessage>)

internal fun UserMessageAggregateReduce(): UserMessageAggregateReduce = DslReduce {
    stateScope {
        transition<UserMessagePosted> {
            UserMessageAggregateUiState(messages = current.messages + action.message)
        }
        transition<UserMessageShown> {
            UserMessageAggregateUiState(messages = current.messages.filterNot { it.id == action.shownId })
        }
    }
}

@Suppress("FunctionName")
internal fun UserMessagePostedFlow(userMessageMonitor: UserMessageMonitor): Flow<UserMessagePosted> {
    return userMessageMonitor.message
        .receiveAsFlow()
        .map(::UserMessagePosted)
}

@HiltViewModel
class UserMessageAggregateEnvironment @Inject constructor(
    val userMessageMonitor: UserMessageMonitor
) : ViewModel()