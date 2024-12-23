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

package com.nlab.reminder.apps

import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.StoreViewModel
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.createStore
import com.nlab.statekit.annotation.UiActionMapping
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap
import com.nlab.reminder.apps.MainActivityAction.*
import com.nlab.reminder.apps.MainActivityUiState.Success
import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.UserMessageMonitor
import com.nlab.statekit.annotation.UiAction
import com.nlab.statekit.dsl.reduce.DslReduce
import com.nlab.statekit.reduce.Reduce
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

typealias MainActivityReduce = Reduce<MainActivityAction, MainActivityUiState>

/**
 * @author Thalys
 */
fun MainActivityReduce(): MainActivityReduce = DslReduce {
    stateScope<Success> {
        transition<UserMessagePosted> {
            current.copy(userMessages = current.userMessages + action.message)
        }
        transition<UserMessageShown> {
            current.copy(userMessages = current.userMessages - action.message)
        }
    }
}

sealed class MainActivityAction private constructor() {
    data class UserMessagePosted(val message: UserMessage) : MainActivityAction()

    @UiAction
    data class UserMessageShown(val message: UserMessage) : MainActivityAction()
}

sealed class MainActivityUiState private constructor() {
    data object Loading : MainActivityUiState()

    data class Success(
        val userMessages: List<UserMessage>
    ) : MainActivityUiState()
}


class MainActivityEnvironment @Inject constructor(
    val userMessageMonitor: UserMessageMonitor
)

@ExcludeFromGeneratedTestReport
@UiActionMapping(MainActivityAction::class)
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val environment: MainActivityEnvironment
) : StoreViewModel<MainActivityAction, MainActivityUiState>() {
    override fun onCreateStore() = createStore(
        initState = MainActivityUiState.Loading,
        reduce = MainActivityReduce(),
        bootstrap = environment.userMessageMonitor.message
            .map(::UserMessagePosted)
            .collectAsBootstrap(DeliveryStarted.Lazily)
    )
}