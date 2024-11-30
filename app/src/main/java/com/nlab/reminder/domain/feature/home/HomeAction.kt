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

import com.nlab.reminder.core.component.tag.delegate.LazyTagEditResult
import com.nlab.reminder.core.component.tag.model.TagEditStep
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.kotlin.NonNegativeLong

/**
 * @author Doohyun
 */
internal sealed interface HomeAction {
    data class StateSynced(
        val todaySchedulesCount: NonNegativeLong,
        val timetableSchedulesCount: NonNegativeLong,
        val allSchedulesCount: NonNegativeLong,
        val tags: List<Tag>,
    ) : HomeAction

    data class UserMessagePosted(val message: UserMessage) : HomeAction

    //    @ContractUiAction
    data class UserMessageShown(val message: UserMessage) : HomeAction

    //   @ContractUiAction
    data object Interacted : HomeAction

    //  @ContractUiAction
    data object OnTodayCategoryClicked : HomeAction

    //  @ContractUiAction
    data object OnTimetableCategoryClicked : HomeAction

    //   @ContractUiAction
    data object OnAllCategoryClicked : HomeAction

    //   @ContractUiAction
    data class OnTagLongClicked(val tag: Tag) : HomeAction

    data class TagEditStarted(val intro: TagEditStep.Intro) : HomeAction

    data class TagEditChangedLazily(
        val toNextStepInteraction: LazyTagEditResult.ToNextStep
    ) : HomeAction

    //  @ContractUiAction
    data object OnTagRenameRequestClicked : HomeAction

    // @ContractUiAction
    data object OnTagRenameInputReady : HomeAction

    // @ContractUiAction
    data class OnTagRenameInputted(val text: String) : HomeAction

    // @ContractUiAction
    data object OnTagRenameConfirmClicked : HomeAction

    // @ContractUiAction
    data object OnTagReplaceConfirmClicked : HomeAction

    // @ContractUiAction
    data object OnTagDeleteRequestClicked : HomeAction

    // @ContractUiAction
    data object OnTagDeleteConfirmClicked : HomeAction
}