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

import com.nlab.reminder.core.state.Action
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
sealed class HomeAction private constructor() : Action {
    object Fetch : HomeAction()
    data class HomeSummaryLoaded(val homeSummary: HomeSummary) : HomeAction()
    object OnTodayCategoryClicked : HomeAction()
    object OnTimetableCategoryClicked : HomeAction()
    object OnAllCategoryClicked : HomeAction()
    data class OnTagClicked(val tag: Tag) : HomeAction()
    data class OnTagLongClicked(val tag: Tag) : HomeAction()
    data class OnTagRenameRequestClicked(val tag: Tag) : HomeAction()
    data class OnTagRenameConfirmClicked(val originalTag: Tag, val renameText: String) : HomeAction()
    data class OnTagDeleteRequestClicked(val tag: Tag) : HomeAction()
    data class OnTagDeleteConfirmClicked(val tag: Tag) : HomeAction()
}