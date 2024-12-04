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

import com.nlab.reminder.core.component.tag.edit.TagEditStep
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.foundation.annotation.Generated
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.statekit.annotation.UiAction

/**
 * @author Doohyun
 */
internal sealed interface HomeAction {
    @Generated
    data class StateSynced(
        val todaySchedulesCount: NonNegativeLong,
        val timetableSchedulesCount: NonNegativeLong,
        val allSchedulesCount: NonNegativeLong,
        val tags: List<Tag>,
    ) : HomeAction

    @Generated
    data class TagEditStepSynced(val step: TagEditStep) : HomeAction

    @Generated
    data class UserMessagePosted(val message: UserMessage) : HomeAction

    @UiAction
    @Generated
    data class UserMessageShown(val message: UserMessage) : HomeAction

    @UiAction
    @Generated
    data object Interacted : HomeAction

    @UiAction
    @Generated
    data object OnTodayCategoryClicked : HomeAction

    @UiAction
    @Generated
    data object OnTimetableCategoryClicked : HomeAction

    @UiAction
    @Generated
    data object OnAllCategoryClicked : HomeAction

    @UiAction
    @Generated
    data class OnTagLongClicked(val tag: Tag) : HomeAction

    @UiAction
    @Generated
    data object OnTagRenameRequestClicked : HomeAction

    @UiAction
    @Generated
    data object OnTagRenameInputReady : HomeAction

    @UiAction
    @Generated
    data class OnTagRenameInputted(val text: String) : HomeAction

    @UiAction
    @Generated
    data object OnTagRenameConfirmClicked : HomeAction

    @UiAction
    @Generated
    data object OnTagReplaceConfirmClicked : HomeAction

    @UiAction
    @Generated
    data object OnTagDeleteRequestClicked : HomeAction

    @UiAction
    @Generated
    data object OnTagDeleteConfirmClicked : HomeAction
}