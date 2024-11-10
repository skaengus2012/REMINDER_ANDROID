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

package com.nlab.reminder.domain.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.reminder.core.foundation.annotation.Generated
import com.nlab.statekit.lifecycle.UiActionDispatchable
import com.nlab.statekit.util.createStore
import com.nlab.statekit.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * @author Doohyun
 */
@Generated
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    reducer: HomeReducer,
    interceptor: HomeInterceptor,
    epic: HomeEpic
) : ViewModel(),
    UiActionDispatchable<HomeAction> {
    private val store = createStore(
        initState = HomeUiState.Loading,
        reducer = reducer,
        interceptor = interceptor,
        epic = epic
    )

    val uiState: StateFlow<HomeUiState> =
        store.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000))

    override fun dispatch(action: HomeAction): Job = store.dispatch(action)
}