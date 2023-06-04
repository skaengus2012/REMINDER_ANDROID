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
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.statekit.core.lifecycle.PublicEvent

/**
 * @author Doohyun
 */
sealed class HomeEvent private constructor() : Event {
    object Fetch : HomeEvent()

    @ExcludeFromGeneratedTestReport
    data class OnSnapshotLoaded(val snapshot: HomeSnapshot) : HomeEvent()

   
    object OnTodayCategoryClicked : HomeEvent()

   
    object OnTimetableCategoryClicked : HomeEvent()

   
    object OnAllCategoryClicked : HomeEvent()

   
    object OnRetryClicked : HomeEvent()

   
    object OnNewScheduleClicked : HomeEvent()   // TODO impl using handling

   
    object OnPushConfigClicked : HomeEvent()    // TODO impl using handling

    @ExcludeFromGeneratedTestReport
   
    data class OnSnapshotLoadFailed(val throwable: Throwable) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagLongClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagRenameRequestClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagRenameConfirmClicked(val originalTag: Tag, val renameText: String) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagDeleteRequestClicked(val tag: Tag) : HomeEvent()

    @ExcludeFromGeneratedTestReport
   
    data class OnTagDeleteConfirmClicked(val tag: Tag) : HomeEvent()
}