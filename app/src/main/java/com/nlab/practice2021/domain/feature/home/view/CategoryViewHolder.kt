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

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.practice2021.core.android.view.throttleClicks
import com.nlab.practice2021.databinding.ViewItemHomeCategoryBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
internal class CategoryViewHolder(
    lifecycleOwner: LifecycleOwner,
    private val binding: ViewItemHomeCategoryBinding
) : RecyclerView.ViewHolder(binding.root) {
    private var onItemClicked: () -> Unit = {}

    init {
        binding.categoryLayout
            .throttleClicks()
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { onItemClicked() }
            .launchIn(lifecycleOwner.lifecycleScope)
    }

    fun onBind(categoryItem: CategoryItem) {
        val context: Context = binding.root.context
        val categoryResource = categoryItem.categoryResource
        val contentColor = context.getColorStateList(categoryResource.contentColorResource)

        onItemClicked = categoryItem.onItemClicked
        with(binding) {
            title.setText(categoryItem.categoryResource.titleResource)
            title.setTextColor(contentColor)

            notificationCount.text = categoryItem.count.toString()
            notificationCount.setTextColor(contentColor)

            iconImageview.imageTintList = contentColor
            background.setBackgroundResource(categoryResource.backgroundColorResource)
        }
    }
}