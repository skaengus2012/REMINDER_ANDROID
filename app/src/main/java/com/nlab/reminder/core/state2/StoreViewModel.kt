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

package com.nlab.reminder.core.state2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.statekit.*
import com.nlab.statekit.lifecycle.*
import com.nlab.statekit.store.Store
import com.nlab.statekit.util.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Doohyun
 */
abstract class StoreViewModel<A : Action, S : State> : ViewModel(), UiActionDispatchable<A> {
    private val store: Store<A, S> by lazy(LazyThreadSafetyMode.NONE) { onCreateStore() }

    val uiState: StateFlow<S> by lazy(LazyThreadSafetyMode.NONE) {
        // Set the timeout to 5000 as per the following reference
        // https://medium.com/androiddevelopers/things-to-know-about-flows-sharein-and-statein-operators-20e6ccb2bc74
        store.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))
    }

    protected abstract fun onCreateStore(): Store<A, S>

    final override fun dispatch(action: A): Job {
        return store.dispatch(action)
    }
}