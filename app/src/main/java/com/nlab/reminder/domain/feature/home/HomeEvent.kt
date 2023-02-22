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

import com.nlab.reminder.core.state.Event
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.state.core.lifecycle.PublicEvent

/**
 * @author Doohyun
 */
sealed class HomeEvent private constructor() : Event {
    object Fetch : HomeEvent()

    @ExcludeFromGeneratedTestReport
    data class OnSnapshotLoaded(val snapshot: HomeSnapshot) : HomeEvent()

    @PublicEvent(HomeViewModel::class)
    object OnTodayCategoryClicked : HomeEvent()

    @PublicEvent(HomeViewModel::class)
    object OnTimetableCategoryClicked : HomeEvent()

    @PublicEvent(HomeViewModel::class)
    object OnAllCategoryClicked : HomeEvent()

    @PublicEvent(HomeViewModel::class)
    object OnRetryClicked : HomeEvent()

    @PublicEvent(HomeViewModel::class)
    object OnNewScheduleClicked : HomeEvent()   // TODO impl using handling

    @PublicEvent(HomeViewModel::class)
    object OnPushConfigClicked : HomeEvent()    // TODO impl using handling

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnSnapshotLoadFailed(val throwable: Throwable) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagLongClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagRenameRequestClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagRenameConfirmClicked(val originalTag: Tag, val renameText: String) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagDeleteRequestClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
    @PublicEvent(HomeViewModel::class)
    data class OnTagDeleteConfirmClicked(val tag: Tag) : HomeEvent()
}