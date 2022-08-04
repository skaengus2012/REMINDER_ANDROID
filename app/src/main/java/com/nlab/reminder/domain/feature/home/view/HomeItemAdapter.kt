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

import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.recyclerview.HashIdentifierItemDiffCallback
import com.nlab.reminder.domain.common.tag.view.TagViewHolder

/**
 * @author Doohyun
 */
class HomeItemAdapter : ListAdapter<HomeItem, RecyclerView.ViewHolder>(HashIdentifierItemDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        @ItemViewType viewType: Int
    ): RecyclerView.ViewHolder = when (viewType) {
        CATEGORY_VIEW_TYPE -> CategoryViewHolder.of(parent)
        TAG_VIEW_TYPE -> TagViewHolder.of(parent)
        else -> throw IllegalArgumentException()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) = when (val item = getItem(position)) {
        is HomeItem.CategoryItem -> {
            require(holder is CategoryViewHolder)
            holder.onBind(item)
        }
        is HomeItem.TagHolderItem -> {
            require(holder is TagViewHolder)
            holder.onBind(item.tagItems)
        }
    }

    @ItemViewType
    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HomeItem.CategoryItem -> CATEGORY_VIEW_TYPE
        is HomeItem.TagHolderItem -> TAG_VIEW_TYPE
    }

    @IntDef(
        value = [
            CATEGORY_VIEW_TYPE,
            TAG_VIEW_TYPE
        ]
    )
    private annotation class ItemViewType

    companion object {
        private const val CATEGORY_VIEW_TYPE = 1
        private const val TAG_VIEW_TYPE = 2
    }
}