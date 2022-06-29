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

package com.nlab.reminder.domain.common.tag

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.nlab.reminder.R
import com.nlab.reminder.core.util.annotation.test.Generated

/**
 * @author Doohyun
 */
@Generated
enum class TagStyleResource(
    val code: Int,
    @ColorRes val textColorResource: Int,
    @DrawableRes val backgroundDrawableResource: Int
) {
    TYPE1(
        code = 1,
        textColorResource = R.color.tag_text_style_1,
        backgroundDrawableResource = R.drawable.bg_tag_style_1
    ),

    TYPE2(
        code = 2,
        textColorResource = R.color.tag_text_style_2,
        backgroundDrawableResource = R.drawable.bg_tag_style_2
    ),

    TYPE3(
        code = 3,
        textColorResource = R.color.tag_text_style_3,
        backgroundDrawableResource = R.drawable.bg_tag_style_3
    ),

    TYPE4(
        code = 4,
        textColorResource = R.color.tag_text_style_4,
        backgroundDrawableResource = R.drawable.bg_tag_style_4
    ),

    TYPE5(
        code = 5,
        textColorResource = R.color.tag_text_style_5,
        backgroundDrawableResource = R.drawable.bg_tag_style_5
    ),

    TYPE6(
        code = 6,
        textColorResource = R.color.tag_text_style_6,
        backgroundDrawableResource = R.drawable.bg_tag_style_6
    );

    companion object {
        fun findByCode(code: Int): TagStyleResource =
            requireNotNull(values().find { it.code == code }) { "$code was not invalie" }
    }
}