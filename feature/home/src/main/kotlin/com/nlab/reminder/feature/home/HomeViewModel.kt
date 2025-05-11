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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.statekit.bootstrap.collectAsBootstrap
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.StoreViewModel
import com.nlab.reminder.core.statekit.store.androidx.lifecycle.createStore
import com.nlab.statekit.annotation.UiActionMapping
import com.nlab.statekit.store.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Doohyun
 */
@ExcludeFromGeneratedTestReport
@UiActionMapping(HomeAction::class)
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val environment: HomeEnvironment
) : StoreViewModel<HomeAction, HomeUiState>() {
    override fun onCreateStore(): Store<HomeAction, HomeUiState> = createStore(
        initState = HomeUiState.Loading,
        reduce = HomeReduce(environment),
        bootstrap = StateSyncFlow(environment).collectAsBootstrap()
    )
}