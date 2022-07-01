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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.R
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.view.throttleLongClicks
import com.nlab.reminder.databinding.ViewItemTagHolderBinding
import com.nlab.reminder.databinding.ViewTagBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class TagViewHolder(
    private val lifecycleOwner: LifecycleOwner,
    binding: ViewItemTagHolderBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private val tagHolderLayout: ViewGroup = binding.tagHolderLayout
    private val tagBindingCache: MutableList<ViewTagBinding> = arrayListOf()

    private var tagItems: List<TagItem> = emptyList()

    fun onBind(newTagItems: List<TagItem>) {
        tagItems = newTagItems

        val itemSize = tagItems.size
        val viewCacheSize = tagBindingCache.size
        if (itemSize > viewCacheSize) {
            val layoutInflater = LayoutInflater.from(tagHolderLayout.context)
            val curLastViewIndex = tagBindingCache.lastIndex
            repeat(times = itemSize - viewCacheSize) { time ->
                val executeIndex = curLastViewIndex + time + 1
                tagBindingCache += ViewTagBinding
                    .inflate(layoutInflater, tagHolderLayout, false)
                    .apply {
                        tagButton.throttleClicks()
                            .flowWithLifecycle(lifecycleOwner.lifecycle)
                            .onEach { onTagClicked(executeIndex) }
                            .launchIn(lifecycleOwner.lifecycleScope)
                    }
                    .apply {
                        tagButton.throttleLongClicks()
                            .flowWithLifecycle(lifecycleOwner.lifecycle)
                            .onEach { onTagLongClicked(executeIndex) }
                            .launchIn(lifecycleOwner.lifecycleScope)
                    }
            }
        }

        with(tagHolderLayout) {
            tagItems.forEachIndexed { index, tagItem ->
                with(tagBindingCache[index].tagButton) {
                    text = context.getString(R.string.tag_format, tagItem.tagText)
                    background = context.getDrawable(tagItem.backgroundDrawableResource)
                    setTextColor(context.getColorStateList(tagItem.textColorResource))
                }
            }

            if (itemSize != viewCacheSize) {
                removeAllViews()
                repeat(times = tagItems.size) { index -> addView(tagBindingCache[index].root) }
            }
        }
    }

    private fun onTagClicked(index: Int) {
        tagItems.getOrNull(index)?.onClicked?.invoke()
    }

    private fun onTagLongClicked(index: Int) {
        tagItems.getOrNull(index)?.onLongClicked?.invoke()
    }
}