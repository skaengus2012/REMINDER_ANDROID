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
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.domain.common.tag.genTags
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.genLong
import kotlinx.coroutines.flow.emptyFlow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
fun genHomeSnapshot(
    todayNotificationCount: Long = genLong(),
    timetableNotificationCount: Long = genLong(),
    allNotificationCount: Long = genLong(),
    tags: List<Tag> = genTags()
): HomeSnapshot = HomeSnapshot(
    NotificationUiState(
        todayNotificationCount.toString(),
        timetableNotificationCount.toString(),
        allNotificationCount.toString()
    ),
    tags
)

fun genHomeStates(): Set<HomeState> = setOf(
    HomeState.Init,
    HomeState.Loading(HomeState.Init),
    HomeState.Loaded(genHomeSnapshot()),
    HomeState.Error(Throwable())
)

fun genHomeEvents(): Set<HomeEvent> = setOf(
    HomeEvent.Fetch,
    HomeEvent.OnTodayCategoryClicked,
    HomeEvent.OnTimetableCategoryClicked,
    HomeEvent.OnAllCategoryClicked,
    HomeEvent.OnRetryClicked,
    HomeEvent.OnSnapshotLoaded(genHomeSnapshot()),
    HomeEvent.OnSnapshotLoadFailed(Throwable()),
    HomeEvent.OnTagClicked(genTag()),
    HomeEvent.OnTagLongClicked(genTag()),
    HomeEvent.OnTagRenameRequestClicked(genTag()),
    HomeEvent.OnTagRenameConfirmClicked(genTag(), genBothify()),
    HomeEvent.OnTagDeleteRequestClicked(genTag()),
    HomeEvent.OnTagDeleteConfirmClicked(genTag())
)

fun genHomeSideEffects(): Set<HomeSideEffect> = setOf(
    HomeSideEffect.NavigateToday,
    HomeSideEffect.NavigateTimetable,
    HomeSideEffect.NavigateAllSchedule,
    HomeSideEffect.NavigateTag(genTag()),
    HomeSideEffect.NavigateTagConfig(genTag()),
    HomeSideEffect.NavigateTagRename(genTag(), genLong()),
    HomeSideEffect.NavigateTagDelete(genTag(), genLong())
)

fun genHomeStateSample(): HomeState = genHomeStates().first()
fun genHomeEventSample(): HomeEvent = genHomeEvents().first()
fun genHomeSideEffectSample(): HomeSideEffect = genHomeSideEffects().first()

fun genHomeStateMachine(
    homeSideEffectHandle: SideEffectHandle<HomeSideEffect> = mock(),
    getHomeSnapshot: GetHomeSnapshotUseCase = mock { onBlocking { mock() } doReturn emptyFlow() },
    getTagUsageCount: GetTagUsageCountUseCase = mock(),
    modifyTagName: ModifyTagNameUseCase = mock(),
    deleteTag: DeleteTagUseCase = mock()
) = HomeStateMachine(
    homeSideEffectHandle,
    getHomeSnapshot,
    getTagUsageCount,
    modifyTagName,
    deleteTag
)