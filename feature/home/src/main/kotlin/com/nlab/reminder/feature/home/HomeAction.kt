/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.kotlin.NonNegativeLong

/**
 * @author Doohyun
 */
internal sealed class HomeAction {
    data class StateSynced(
        val todaySchedulesCount: NonNegativeLong,
        val timetableSchedulesCount: NonNegativeLong,
        val allSchedulesCount: NonNegativeLong,
        val sortedTags: List<Tag>,
    ) : HomeAction()

    data class CompareAndSetTagEditState(
        val expectedState: TagEditState,
        val newState: TagEditState
    ) : HomeAction()

    data object TodayCategoryClicked : HomeAction()

    data object TimetableCategoryClicked : HomeAction()

    data object AllCategoryClicked : HomeAction()

    data class TagLongClicked(val tag: Tag) : HomeAction()

    data object TagRenameRequestClicked : HomeAction()

    data object TagRenameInputReady : HomeAction()

    data class TagRenameInputted(val text: String) : HomeAction()

    data object TagRenameConfirmClicked : HomeAction()

    data object TagReplaceConfirmClicked : HomeAction()

    data object TagReplaceCancelClicked : HomeAction()

    data object TagDeleteRequestClicked : HomeAction()

    data object TagDeleteConfirmClicked : HomeAction()

    data object TagEditCancelClicked : HomeAction()
}