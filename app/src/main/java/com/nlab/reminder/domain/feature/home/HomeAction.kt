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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.component.text.UiText
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.statekit.annotation.UiAction

/**
 * @author Doohyun
 */
internal sealed class HomeAction private constructor() {
    data class StateSynced(
        val todaySchedulesCount: NonNegativeLong,
        val timetableSchedulesCount: NonNegativeLong,
        val allSchedulesCount: NonNegativeLong,
        val sortedTags: List<Tag>,
    ) : HomeAction()

    data class TagEditStateSynced(val state: TagEditState?) : HomeAction()

    data class UserMessagePosted(val message: UiText) : HomeAction()

    @UiAction
    data class UserMessageShown(val message: UiText) : HomeAction()

    @UiAction
    data object Interacted : HomeAction()

    @UiAction
    data object OnTodayCategoryClicked : HomeAction()

    @UiAction
    data object OnTimetableCategoryClicked : HomeAction()

    @UiAction
    data object OnAllCategoryClicked : HomeAction()

    @UiAction
    data class OnTagLongClicked(val tag: Tag) : HomeAction()

    @UiAction
    data object OnTagRenameRequestClicked : HomeAction()

    @UiAction
    data object OnTagRenameInputReady : HomeAction()

    @UiAction
    data class OnTagRenameInputted(val text: String) : HomeAction()

    @UiAction
    data object OnTagRenameConfirmClicked : HomeAction()

    @UiAction
    data object OnTagReplaceConfirmClicked : HomeAction()

    @UiAction
    data object OnTagDeleteRequestClicked : HomeAction()

    @UiAction
    data object OnTagDeleteConfirmClicked : HomeAction()
}