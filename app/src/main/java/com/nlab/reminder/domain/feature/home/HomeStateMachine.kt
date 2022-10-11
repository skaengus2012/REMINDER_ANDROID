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
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.kotlin.util.getOrThrow
import com.nlab.reminder.core.kotlin.util.onFailure
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.StateMachineHandleScope
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun HomeStateMachine(
    sideEffectHandle: SideEffectHandle<HomeSideEffect>,
    getHomeSnapshot: GetHomeSnapshotUseCase,
    getTagUsageCount: GetTagUsageCountUseCase,
    modifyTagName: ModifyTagNameUseCase,
    deleteTag: DeleteTagUseCase
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

    handled {
        suspend fun StateMachineHandleScope<in HomeEvent>.collectSnapshot() {
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

    handled {
        state<HomeState.Loaded> {
            event<HomeEvent.OnTodayCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateToday) }
            event<HomeEvent.OnTimetableCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateTimetable) }
            event<HomeEvent.OnAllCategoryClicked> { sideEffectHandle.post(HomeSideEffect.NavigateAllSchedule) }
            event<HomeEvent.OnTagClicked> { (event) -> sideEffectHandle.post(HomeSideEffect.NavigateTag(event.tag)) }
            event<HomeEvent.OnTagLongClicked> { (event) ->
                sideEffectHandle.post(HomeSideEffect.NavigateTagConfig(event.tag))
            }
            event<HomeEvent.OnTagRenameRequestClicked> { (event) ->
                sideEffectHandle.post(HomeSideEffect.NavigateTagRename(event.tag, getTagUsageCount(event.tag)))
            }
            event<HomeEvent.OnTagDeleteRequestClicked> { (event) ->
                sideEffectHandle.post(HomeSideEffect.NavigateTagDelete(event.tag, getTagUsageCount(event.tag)))
            }
            event<HomeEvent.OnTagRenameConfirmClicked> { (event) ->
                modifyTagName(originalTag = event.originalTag, newText = event.renameText)
            }
            event<HomeEvent.OnTagDeleteConfirmClicked> { (event) -> deleteTag(event.tag) }
        }
    }
}