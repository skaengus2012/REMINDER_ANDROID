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

package com.nlab.practice2021.domain.feature.home

import com.nlab.practice2021.domain.common.tag.Tag

/**
 * @author Doohyun
 */
class HomeStateLoadedFactory(
    private val onTodayCategoryClicked: () -> Unit,
    private val onTimetableCategoryClicked: () -> Unit,
    private val onAllCategoryClicked: () -> Unit,
    private val onTagClicked: (Tag) -> Unit
) {
    fun create(homeSummary: HomeSummary): HomeState.Loaded = HomeState.Loaded(
        homeSummary,
        onTodayCategoryClicked,
        onTimetableCategoryClicked,
        onAllCategoryClicked,
        onTagClicked
    )
}