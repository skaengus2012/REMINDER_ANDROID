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

import com.nlab.reminder.core.android.recyclerview.HashIdentifier
import com.nlab.reminder.domain.common.android.view.recyclerview.ItemModel
import com.nlab.reminder.domain.common.tag.view.TagItem
import java.util.*

@ItemModel
internal sealed class HomeItem private constructor() : HashIdentifier {
    data class CategoryItem(
        val categoryResource: CategoryResource,
        val count: Long,
        val onItemClicked: () -> Unit
    ) : HomeItem() {
        override val hashId: Int = Objects.hash(categoryResource, count)
    }

    data class TagHolderItem(
        val tagItems: List<TagItem>
    ) : HomeItem() {
        override val hashId: Int = Objects.hash(tagItems.map { it.tagText })
    }
}