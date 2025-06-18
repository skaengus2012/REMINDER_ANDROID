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

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.UserMessageId
import com.nlab.reminder.core.component.usermessage.eventbus.UserMessageAggregateAction.*
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.StoreViewModel
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.createStore
import com.nlab.statekit.annotation.UiAction
import com.nlab.statekit.annotation.UiActionMapping
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

internal typealias UserMessageAggregateReduce = Reduce<UserMessageAggregateAction, UserMessageAggregateUiState>

/**
 * @author Thalys
 */
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

sealed class UserMessageAggregateAction {
    data class UserMessagePosted(val message: UserMessage) : UserMessageAggregateAction()

    @UiAction(isPublic = true)
    data class UserMessageShown(val shownId: UserMessageId) : UserMessageAggregateAction()
}

data class UserMessageAggregateUiState(val messages: List<UserMessage>)

@ExcludeFromGeneratedTestReport
@UiActionMapping(UserMessageAggregateAction::class)
@HiltViewModel
class UserMessageAggregateViewModel @Inject constructor(
    private val userMessageMonitor: UserMessageMonitor
) : StoreViewModel<UserMessageAggregateAction, UserMessageAggregateUiState>() {
    override fun onCreateStore() = createStore(
        initState = UserMessageAggregateUiState(messages = emptyList()),
        reduce = UserMessageAggregateReduce(),
        bootstrap = userMessageMonitor.message
            .receiveAsFlow()
            .map(::UserMessagePosted)
            .collectAsBootstrap(DeliveryStarted.Lazily)
    )
}