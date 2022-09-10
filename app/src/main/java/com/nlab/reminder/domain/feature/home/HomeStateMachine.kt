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

import com.nlab.reminder.core.effect.SideEffectSender
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.kotlin.util.getValueOrThrow
import com.nlab.reminder.core.kotlin.util.onFailure
import com.nlab.reminder.core.state.util.StateMachine

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun HomeStateMachine(
    homeSideEffect: SideEffectSender<HomeSideEffect>,
    getHomeSnapshot: GetHomeSnapshotUseCase,
    getTagUsageCount: GetTagUsageCountUseCase,
    modifyTagName: ModifyTagNameUseCase,
    deleteTag: DeleteTagUseCase
): StateMachine<HomeEvent, HomeState> = StateMachine {
    update { (event, state) ->
        when (event) {
            is HomeEvent.Fetch -> {
                if (state is HomeState.Init) HomeState.Loading
                else state
            }
            is HomeEvent.OnSnapshotLoaded -> {
                if (state is HomeState.Init) state
                else HomeState.Loaded(event.snapshot)
            }
            is HomeEvent.OnSnapshotLoadFailed -> {
                HomeState.Error(event.throwable, state)
            }
            else -> state
        }
    }

    handle { (event, state) ->
        val isFetched: Boolean = (state is HomeState.Init && event is HomeEvent.Fetch)
        val isRetried: Boolean = (state is HomeState.Error && event is HomeEvent.OnRetryClicked)
        if (isFetched || isRetried) {
            catching { getHomeSnapshot().collect { snapshot -> send(HomeEvent.OnSnapshotLoaded(snapshot)) } }
                .onFailure { e -> send(HomeEvent.OnSnapshotLoadFailed(e)).join() }
                .getValueOrThrow()
        }
    }

    handleOn<HomeEvent.OnTodayCategoryClicked, HomeState.Loaded> {
        homeSideEffect.post(HomeSideEffect.NavigateToday)
    }

    handleOn<HomeEvent.OnTimetableCategoryClicked, HomeState.Loaded> {
        homeSideEffect.post(HomeSideEffect.NavigateTimetable)
    }

    handleOn<HomeEvent.OnAllCategoryClicked, HomeState.Loaded> {
        homeSideEffect.post(HomeSideEffect.NavigateAllSchedule)
    }

    handleOn<HomeEvent.OnTagClicked, HomeState.Loaded> { (event) ->
        homeSideEffect.post(HomeSideEffect.NavigateTag(event.tag))
    }

    handleOn<HomeEvent.OnTagLongClicked, HomeState.Loaded> { (event) ->
        homeSideEffect.post(HomeSideEffect.NavigateTagConfig(event.tag))
    }

    handleOn<HomeEvent.OnTagRenameRequestClicked, HomeState.Loaded> { (event) ->
        homeSideEffect.post(HomeSideEffect.NavigateTagRename(event.tag, getTagUsageCount(event.tag)))
    }

    handleOn<HomeEvent.OnTagDeleteRequestClicked, HomeState.Loaded> { (event) ->
        homeSideEffect.post(HomeSideEffect.NavigateTagDelete(event.tag, getTagUsageCount(event.tag)))
    }

    handleOn<HomeEvent.OnTagRenameConfirmClicked, HomeState.Loaded> { (event) ->
        modifyTagName(originalTag = event.originalTag, newText = event.renameText)
    }

    handleOn<HomeEvent.OnTagDeleteConfirmClicked, HomeState.Loaded> { (event) ->
        deleteTag(event.tag)
    }
}