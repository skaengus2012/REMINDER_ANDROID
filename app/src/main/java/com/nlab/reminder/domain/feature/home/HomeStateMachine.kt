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
import com.nlab.reminder.core.state.util.StateMachine
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateAllEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTagEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTimetableEnd
import com.nlab.reminder.domain.common.effect.message.navigation.util.navigateTodayEnd

typealias HomeStateMachine = StateMachine<HomeEvent, HomeState>

/**
 * @author Doohyun
 */
fun HomeStateMachine(
    navigationEffect: SendNavigationEffect,
    getHomeSummary: GetHomeSummaryUseCase,
    getTagUsageCount: GetTagUsageCountUseCase,
    modifyTagName: ModifyTagNameUseCase,
    deleteTag: DeleteTagUseCase
): HomeStateMachine = StateMachine {
    update { (event, state) ->
        when (event) {
            is HomeEvent.Fetch -> {
                if (state is HomeState.Init) HomeState.Loading
                else state
            }
            is HomeEvent.OnHomeSummaryLoaded -> {
                if (state is HomeState.Init) state
                else HomeState.Loaded(event.homeSummary)
            }
            else -> state
        }
    }

    sideEffectOn<HomeEvent.Fetch, HomeState.Init> {
        getHomeSummary().collect { send(HomeEvent.OnHomeSummaryLoaded(it)) }
    }

    sideEffectOn<HomeEvent.OnTodayCategoryClicked, HomeState.Loaded> {
        navigationEffect.navigateTodayEnd()
    }

    sideEffectOn<HomeEvent.OnTimetableCategoryClicked, HomeState.Loaded> {
        navigationEffect.navigateTimetableEnd()
    }

    sideEffectOn<HomeEvent.OnAllCategoryClicked, HomeState.Loaded> {
        navigationEffect.navigateAllEnd()
    }

    sideEffectOn<HomeEvent.OnTagClicked, HomeState.Loaded> { (event) ->
        navigationEffect.navigateTagEnd(event.tag)
    }

    sideEffectOn<HomeEvent.OnTagLongClicked, HomeState.Loaded> { (event) ->
        navigationEffect.send(HomeTagConfigNavigationMessage(event.tag))
    }

    sideEffectOn<HomeEvent.OnTagRenameRequestClicked, HomeState.Loaded> { (event) ->
        navigationEffect.send(HomeTagRenameNavigationMessage(event.tag, getTagUsageCount(event.tag)))
    }

    sideEffectOn<HomeEvent.OnTagDeleteRequestClicked, HomeState.Loaded> { (event) ->
        navigationEffect.send(HomeTagDeleteNavigationMessage(event.tag, getTagUsageCount(event.tag)))
    }

    sideEffectOn<HomeEvent.OnTagRenameConfirmClicked, HomeState.Loaded> { (event) ->
        modifyTagName(originalTag = event.originalTag, newText = event.renameText)
    }

    sideEffectOn<HomeEvent.OnTagDeleteConfirmClicked, HomeState.Loaded> { (event) ->
        deleteTag(event.tag)
    }
}