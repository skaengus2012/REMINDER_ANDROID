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

import com.nlab.practice2021.core.effect.android.navigation.SendNavigationEffect
import kotlinx.coroutines.CoroutineScope

/**
 * @author Doohyun
 */
class HomeStateMachineFactory(
    private val getHomeSummary: GetHomeSummaryUseCase,
    private val initState: HomeState = HomeState.Init
) {
    fun create(
        scope: CoroutineScope,
        navigationEffect: SendNavigationEffect,
        onHomeSummaryLoaded: (HomeSummary) -> Unit,
        onTodayClicked: () -> Unit
    ): HomeStateMachine = HomeStateMachine(
        scope,
        initState,
        navigationEffect,
        getHomeSummary,
        onHomeSummaryLoaded,
        onTodayClicked
    )
}