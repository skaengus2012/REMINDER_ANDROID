/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

package com.nlab.practice2021.domain.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.practice2021.core.worker.DispatcherProvider
import com.nlab.practice2021.domain.home.model.NavigateMenuRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
class HomeViewModel(
    dispatcherProvider: DispatcherProvider,
    navigateMenuRepository: NavigateMenuRepository,
    homeItemViewModelFactory: HomeItemViewModel.Factory
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: Flow<State> = _stateFlow

    init {
        viewModelScope.launch {
            _stateFlow.emit(State(isLoading = true))
            _stateFlow.emit(withContext(dispatcherProvider.io()) {
                State(
                    items = navigateMenuRepository.getNavigateMenus().map { menu ->
                        homeItemViewModelFactory.create(
                            viewModelScope,
                            menu.destination,
                            menu.titleRes,
                            menu.descriptionRes,
                            menu.backgroundColorRes
                        )
                    },
                    isComplete = true
                )
            })
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val isComplete: Boolean = false,
        val items: List<HomeItemViewModel> = emptyList()
    )

}