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
import com.nlab.reminder.core.util.test.annotation.Generated
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
sealed class HomeEvent private constructor() : Event {
    object Fetch : HomeEvent()
    object OnTodayCategoryClicked : HomeEvent()
    object OnTimetableCategoryClicked : HomeEvent()
    object OnAllCategoryClicked : HomeEvent()
    object OnRetryClicked : HomeEvent()
    object OnNewScheduleClicked : HomeEvent()   // TODO impl using handling
    object OnPushConfigClicked : HomeEvent()    // TODO impl using handling
    @Generated data class OnSnapshotLoaded(val snapshot: HomeSnapshot) : HomeEvent()
    @Generated data class OnSnapshotLoadFailed(val throwable: Throwable) : HomeEvent()
    @Generated data class OnTagClicked(val tag: Tag) : HomeEvent()
    @Generated data class OnTagLongClicked(val tag: Tag) : HomeEvent()
    @Generated data class OnTagRenameRequestClicked(val tag: Tag) : HomeEvent()
    @Generated data class OnTagRenameConfirmClicked(val originalTag: Tag, val renameText: String) : HomeEvent()
    @Generated data class OnTagDeleteRequestClicked(val tag: Tag) : HomeEvent()
    @Generated data class OnTagDeleteConfirmClicked(val tag: Tag) : HomeEvent()
}