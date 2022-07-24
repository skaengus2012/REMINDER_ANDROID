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

package com.nlab.reminder.domain.common.tag.view

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.nlab.reminder.domain.common.android.view.recyclerview.ItemModel
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagStyleResource

/**
 * @author Doohyun
 */
@ItemModel
data class TagItem(
    private val tag: Tag,
    private val tagStyleResource: TagStyleResource,
    val onClicked: () -> Unit,
    val onLongClicked: () -> Unit
) {
    val tagId: Long
        get() = tag.tagId

    val name: String
        get() = tag.name

    @get:ColorRes
    val textColorResource: Int
        get() = tagStyleResource.textColorResource

    @get:DrawableRes
    val backgroundDrawableResource: Int
        get() = tagStyleResource.backgroundDrawableResource
}