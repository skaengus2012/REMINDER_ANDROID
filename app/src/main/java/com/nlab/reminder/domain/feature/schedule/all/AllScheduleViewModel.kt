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

package com.nlab.reminder.domain.feature.schedule.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.reminder.core.annotation.test.ExcludeFromGeneratedTestReport
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
@ExcludeFromGeneratedTestReport
@HiltViewModel
internal class AllScheduleViewModel @Inject constructor(
    reducer: AllScheduleReducer,
    interceptor: AllScheduleInterceptor,
    epic: AllScheduleEpic,
) : ViewModel(),
    UiActionDispatchable<AllScheduleAction> {
    private val store = createStore(initState = AllScheduleUiState.Empty, reducer, interceptor, epic)

    val uiState: StateFlow<AllScheduleUiState> =
        store.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000))

    override fun dispatch(action: AllScheduleAction): Job = store.dispatch(action)
}