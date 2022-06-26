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

package com.nlab.practice2021.domain.common.tag.view

import androidx.recyclerview.widget.DiffUtil

class TagItemsDiffCallback : DiffUtil.ItemCallback<List<TagItem>>() {
    override fun areItemsTheSame(oldItem: List<TagItem>, newItem: List<TagItem>): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: List<TagItem>, newItem: List<TagItem>): Boolean {
        return if (oldItem.size != newItem.size) false
        else oldItem.map { it.tagText } == newItem.map { it.tagText }
    }
}