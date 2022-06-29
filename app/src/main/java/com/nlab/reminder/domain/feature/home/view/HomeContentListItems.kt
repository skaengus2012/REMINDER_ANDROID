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

package com.nlab.reminder.domain.feature.home.view

import com.nlab.reminder.domain.common.tag.view.TagItem
import com.nlab.reminder.domain.feature.home.HomeState

/**
 * @author Doohyun
 */
internal fun HomeState.Loaded.toListItem(): HomeListItem = HomeListItem(
    categoryItems = listOf(
        CategoryItem(
            categoryResource = CategoryResource.TODAY,
            count = homeSummary.todayNotificationCount,
            onItemClicked = onTodayCategoryClicked
        ),
        CategoryItem(
            categoryResource = CategoryResource.TIME_TABLE,
            count = homeSummary.timetableNotificationCount,
            onItemClicked = onTimetableCategoryClicked
        ),
        CategoryItem(
            categoryResource = CategoryResource.ALL,
            count = homeSummary.allNotificationCount,
            onItemClicked = onAllCategoryClicked
        )
    ),
    tagItems = homeSummary.tags.map { tag ->
        TagItem(
            tag,
            onClicked = { onTagClicked(tag) },
            onLongClicked = {}
        )
    }
)