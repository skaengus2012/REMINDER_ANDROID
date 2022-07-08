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

import com.nlab.reminder.core.effect.message.navigation.SendNavigationEffect
import kotlinx.coroutines.CoroutineScope

/**
 * @author Doohyun
 */
class HomeStateMachineFactory(
    private val getHomeSummary: GetHomeSummaryUseCase,
    private val getTagUsageCount: GetTagUsageCountUseCase,
    private val modifyTagName: ModifyTagNameUseCase,
    private val initState: HomeState = HomeState.Init
) {
    fun create(
        scope: CoroutineScope,
        navigationEffect: SendNavigationEffect,
        onHomeSummaryLoaded: (HomeSummary) -> Unit,
    ): HomeStateMachine = HomeStateMachine(
        scope,
        initState,
        navigationEffect,
        getHomeSummary,
        getTagUsageCount,
        modifyTagName,
        onHomeSummaryLoaded
    )
}