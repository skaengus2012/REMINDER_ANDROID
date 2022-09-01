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

import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author Doohyun
 */
fun HomeViewModel.onTodayCategoryClicked() = invoke(HomeEvent.OnTodayCategoryClicked)
fun HomeViewModel.onTimetableCategoryClicked() = invoke(HomeEvent.OnTimetableCategoryClicked)
fun HomeViewModel.onAllCategoryClicked() = invoke(HomeEvent.OnAllCategoryClicked)
fun HomeViewModel.onTagClicked(tag: Tag) = invoke(HomeEvent.OnTagClicked(tag))
fun HomeViewModel.onTagLongClicked(tag: Tag) = invoke(HomeEvent.OnTagLongClicked(tag))
fun HomeViewModel.onTagRenameRequestClicked(tag: Tag) = invoke(HomeEvent.OnTagRenameRequestClicked(tag))
fun HomeViewModel.onTagRenameConfirmClicked(originalTag: Tag, renameText: String) = invoke(HomeEvent.OnTagRenameConfirmClicked(originalTag, renameText))
fun HomeViewModel.onTagDeleteRequestClicked(tag: Tag) = invoke(HomeEvent.OnTagDeleteRequestClicked(tag))
fun HomeViewModel.onTagDeleteConfirmClicked(tag: Tag) = invoke(HomeEvent.OnTagDeleteConfirmClicked(tag))