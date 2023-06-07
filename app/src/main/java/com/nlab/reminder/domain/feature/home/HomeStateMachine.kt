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

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.kotlin.util.*
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.util.generateHandleScopeFunction
import com.nlab.reminder.domain.common.data.repository.TagRepository
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun HomeStateMachine(
    sideEffectHandle: SideEffectHandle<HomeSideEffect>,
    getHomeSnapshot: GetHomeSnapshotUseCase,
    tagRepository: TagRepository
): StateMachine<HomeEvent, HomeState> = StateMachine {
    reduce {
        event<HomeEvent.Fetch> {
            state<HomeState.Init> { HomeState.Loading(it.before) }
        }

        event<HomeEvent.OnRetryClicked> {
            state<HomeState.Error> { HomeState.Loading(it.before) }
        }

        event<HomeEvent.OnSnapshotLoaded> {
            stateNot<HomeState.Init> { (event) -> HomeState.Loaded(event.snapshot) }
        }

        event<HomeEvent.OnSnapshotLoadFailed> {
            anyState { (event) -> HomeState.Error(event.throwable) }
        }
    }

    handle {
        val collectSnapshot = generateHandleScopeFunction {
            val collectResult = catching {
                getHomeSnapshot()
                    .map(HomeEvent::OnSnapshotLoaded)
                    .collectWhileSubscribed { event -> send(event) }
            }
            collectResult
                .onFailure { send(HomeEvent.OnSnapshotLoadFailed(it)).join() }
                .getOrThrow()
        }

        state<HomeState.Init> {
            event<HomeEvent.Fetch> { collectSnapshot() }
        }

        state<HomeState.Error> {
            event<HomeEvent.OnRetryClicked> { collectSnapshot() }
        }
    }

    handle {
        state<HomeState.Loaded> {
            event<HomeEvent.OnTodayCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateToday) }
            event<HomeEvent.OnTimetableCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateTimetable) }
            event<HomeEvent.OnAllCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateAllSchedule) }
            event<HomeEvent.OnTagClicked> { (event) -> sideEffectHandle.post(HomeSideEffect.NavigateTag(event.tag)) }
            event<HomeEvent.OnTagLongClicked> { (event) ->
                sideEffectHandle.post(HomeSideEffect.ShowTagConfigPopup(event.tag))
            }
            event<HomeEvent.OnTagRenameRequestClicked> { (event) ->
                sideEffectHandle.post(
                    sideEffect = tagRepository.getUsageCount(event.tag)
                        .map { usageCount -> HomeSideEffect.ShowTagRenamePopup(event.tag, usageCount.value) }
                        .getOrNull()
                        ?: HomeSideEffect.ShowErrorPopup
                )
            }
            event<HomeEvent.OnTagDeleteRequestClicked> { (event) ->
                sideEffectHandle.post(
                    sideEffect = tagRepository.getUsageCount(event.tag)
                        .map { usageCount -> HomeSideEffect.ShowTagDeletePopup(event.tag, usageCount.value) }
                        .getOrNull()
                        ?: HomeSideEffect.ShowErrorPopup
                )
            }
            event<HomeEvent.OnTagRenameConfirmClicked> { (event) ->
                tagRepository
                    .updateName(event.originalTag, event.renameText)
                    .onFailure { sideEffectHandle.post(HomeSideEffect.ShowErrorPopup) }
            }
            event<HomeEvent.OnTagDeleteConfirmClicked> { (event) ->
                tagRepository.delete(event.tag).onFailure { sideEffectHandle.post(HomeSideEffect.ShowErrorPopup) }
            }
        }
    }
}