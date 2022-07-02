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
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.util.StateMachine
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateAllEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTagEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTimetableEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTodayEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias HomeStateMachine = StateMachine<HomeAction, HomeState>

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun HomeStateMachine(
    scope: CoroutineScope,
    initState: HomeState,
    navigationEffect: SendNavigationEffect,
    getHomeSummary: GetHomeSummaryUseCase,
    getTagUsageCount: GetTagUsageCountUseCase,
    onHomeSummaryLoaded: (HomeSummary) -> Unit
): HomeStateMachine = StateMachine(scope, initState) {
    updateTo { (action, oldState) ->
        when (action) {
            is HomeAction.Fetch -> {
                if (oldState is HomeState.Init) HomeState.Loading
                else oldState
            }
            is HomeAction.HomeSummaryLoaded -> HomeState.Loaded(action.homeSummary)
            else -> oldState
        }
    }

    sideEffectWhen<HomeAction.Fetch, HomeState.Init> {
        scope.launch { getHomeSummary().collect { onHomeSummaryLoaded(it) } }
    }

    sideEffectWhen<HomeAction.OnTodayCategoryClicked, HomeState.Loaded> {
        scope.launch { navigationEffect.navigateTodayEnd() }
    }

    sideEffectWhen<HomeAction.OnTimetableCategoryClicked, HomeState.Loaded> {
        scope.launch { navigationEffect.navigateTimetableEnd() }
    }

    sideEffectWhen<HomeAction.OnAllCategoryClicked, HomeState.Loaded> {
        scope.launch { navigationEffect.navigateAllEnd() }
    }

    sideEffectWhen<HomeAction.OnTagClicked, HomeState.Loaded> { (action) ->
        scope.launch { navigationEffect.navigateTagEnd(action.tag) }
    }

    sideEffectWhen<HomeAction.OnTagLongClicked, HomeState.Loaded> { (action) ->
        scope.launch { navigationEffect.send(HomeTagConfigNavigationMessage(action.tag)) }
    }

    sideEffectWhen<HomeAction.OnTagRenameRequestClicked, HomeState.Loaded> { (action) ->
        scope.launch { navigationEffect.send(HomeTagRenameNavigationMessage(action.tag, getTagUsageCount.invoke(action.tag))) }
    }

    sideEffectWhen<HomeAction.OnTagDeleteRequestClicked, HomeState.Loaded> { (action) ->
        scope.launch { navigationEffect.send(HomeTagDeleteConfirmNavigationMessage(action.tag)) }
    }
}