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

package com.nlab.practice2021.domain.feature.home.view

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.nlab.practice2021.R

/**
 * @author Doohyun
 */
internal enum class CategoryResource(
    @StringRes val titleResource: Int,
    @ColorRes val contentColorResource: Int,
    @ColorRes val backgroundColorResource: Int
) {
    TODAY(
        titleResource = R.string.category_today,
        contentColorResource = R.color.home_category_today_pressed,
        backgroundColorResource = R.color.home_category_today_pressed_rerverse
    ),

    TIME_TABLE(
        titleResource = R.string.category_timetable,
        contentColorResource = R.color.home_category_timetable_pressed,
        backgroundColorResource = R.color.home_category_timetable_pressed_rerverse
    ),

    ALL(
        titleResource = R.string.category_all,
        contentColorResource = R.color.home_category_all_pressed,
        backgroundColorResource = R.color.home_category_all_pressed_reverse
    )
}