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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.R
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.view.throttleLongClicks
import com.nlab.reminder.databinding.ViewItemHomeTagCardBinding
import com.nlab.reminder.databinding.ViewTagBinding
import com.nlab.reminder.domain.common.data.model.Tag
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author thalys
 */
class HomeTagCardAdapter(
    private val onTagClicked: (Tag) -> Unit,
    private val onTagLongClicked: (Tag) -> Unit
) : ListAdapter<List<Tag>, HomeTagCardAdapter.ViewHolder>(HomeTagCardDiffItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewItemHomeTagCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onTagClicked,
            onTagLongClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(
        private val binding: ViewItemHomeTagCardBinding,
        private val onTagClicked: (Tag) -> Unit,
        private val onTagLongClicked: (Tag) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val tagBindingCache: MutableList<ViewTagBinding> = arrayListOf()

        private var boundTags: List<Tag> = emptyList()

        fun onBind(tags: List<Tag>) {
            val oldSize = boundTags.size
            val newSize = tags.size
            boundTags = tags

            inflateItemViewsIfNeeded()
            invalidateItemViewsIfNeeded(oldSize, newSize)
            setVisibility()
            setTextToChildViews()
        }

        private fun onTagClicked(index: Int) {
            onTagClicked(boundTags.getOrNull(index) ?: return)
        }

        private fun onTagLongClicked(index: Int) {
            onTagLongClicked(boundTags.getOrNull(index) ?: return)
        }

        private fun setVisibility() = with(binding) {
            if (boundTags.isEmpty()) {
                textviewEmpty.visibility = View.VISIBLE
                layoutTagHolder.visibility = View.GONE
            } else {
                textviewEmpty.visibility = View.GONE
                layoutTagHolder.visibility = View.VISIBLE
            }
        }

        private fun setTextToChildViews() {
            boundTags.forEachIndexed { index, tag ->
                tagBindingCache[index].btnTag.apply { text = context.getString(R.string.format_tag, tag.name) }
            }
        }

        private fun inflateItemViewsIfNeeded() {
            val parent: ViewGroup = binding.layoutTagHolder
            val itemSize = boundTags.size
            val viewCacheSize = tagBindingCache.size
            if (itemSize > viewCacheSize) {
                val layoutInflater = LayoutInflater.from(parent.context)
                val curLastViewIndex = tagBindingCache.lastIndex
                repeat(times = itemSize - viewCacheSize) { time ->
                    val executeIndex = curLastViewIndex + time + 1
                    tagBindingCache += ViewTagBinding.inflate(layoutInflater, parent, false)
                        .initWithLifecycleOwner { lifecycleOwner ->
                            btnTag.throttleClicks()
                                .onEach { onTagClicked(executeIndex) }
                                .launchIn(lifecycleOwner.lifecycleScope)
                        }
                        .initWithLifecycleOwner { lifecycleOwner ->
                            btnTag.throttleLongClicks()
                                .onEach { onTagLongClicked(executeIndex) }
                                .launchIn(lifecycleOwner.lifecycleScope)
                        }
                }
            }
        }

        private fun invalidateItemViewsIfNeeded(oldTagSize: Int, newTagSize: Int) {
            if (oldTagSize != newTagSize) {
                val parent: ViewGroup = binding.layoutTagHolder
                parent.removeAllViews()
                repeat(newTagSize) { index -> parent.addView(tagBindingCache[index].root) }
            }
        }
    }
}