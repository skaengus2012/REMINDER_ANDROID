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

package com.nlab.practice2021.domain.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.practice2021.core.effect.android.navigation.NavigationEffect
import com.nlab.practice2021.core.effect.android.navigation.SendNavigationEffect
import com.nlab.practice2021.core.effect.util.sideEffect
import com.nlab.practice2021.core.state.util.fetchedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * @author Doohyun
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    stateMachineFactory: HomeStateMachineFactory
) : ViewModel() {
    private val _navigationEffect: SendNavigationEffect by sideEffect()
    private val stateMachine: HomeStateMachine = stateMachineFactory.create(
        viewModelScope,
        _navigationEffect,
        onHomeSummaryLoaded = { homeSummary -> onAction(HomeAction.HomeSummaryRefreshed(homeSummary)) },
        onTodayCategoryClicked = { onAction(HomeAction.OnTodayCategoryClicked) },
        onTimeTableCategoryClicked = { onAction(HomeAction.OnTimetableCategoryClicked) },
        onAllCategoryClicked = { onAction(HomeAction.OnAllCategoryClicked) },
        onTagClicked = { tag -> onAction(HomeAction.OnTagClicked(tag)) }
    )

    val navigationEffect: NavigationEffect = _navigationEffect
    val state: StateFlow<HomeState> =
        stateMachine
            .state
            .fetchedFlow(viewModelScope, this::onFetch)

    fun onAction(action: HomeAction) {
        stateMachine.send(action)
    }

    private fun onFetch() {
        onAction(HomeAction.Fetch)
    }
}