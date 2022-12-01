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

package com.nlab.reminder.domain.common.schedule.view

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlab.reminder.R
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.recyclerview.bindingAdapterOptionalPosition
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.ViewItemScheduleBinding
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author thalys
 */
class ScheduleUiStateViewHolder(
    private val binding: ViewItemScheduleBinding,
    onCompleteClicked: (Int) -> Unit,
    onDeleteClicked: (Int) -> Unit,
    onLinkClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private val linkThumbnailPlaceHolderDrawable: Drawable? = with(itemView) {
        AppCompatResources.getDrawable(context, R.drawable.ic_schedule_link_error)
            ?.let(DrawableCompat::wrap)
            ?.apply { DrawableCompat.setTint(mutate(), context.getThemeColor(R.attr.tint_schedule_placeholder)) }
    }

    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .onEach { onCompleteClicked(bindingAdapterOptionalPosition ?: return@onEach) }
                .launchIn(lifecycleOwner.lifecycleScope)

            layoutDelete
                .throttleClicks()
                .onEach { onDeleteClicked(bindingAdapterOptionalPosition ?: return@onEach) }
                .launchIn(lifecycleOwner.lifecycleScope)

            cardLink
                .throttleClicks()
                .onEach { onLinkClicked(bindingAdapterOptionalPosition ?: return@onEach) }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    fun onBind(scheduleUiState: ScheduleUiState) {
        binding.textviewTitle.text = scheduleUiState.title
        binding.textviewNote.text = scheduleUiState.note
        binding.buttonComplete.isSelected = scheduleUiState.isCompleteMarked
        binding.cardLink.visibility = if (scheduleUiState.isLinkCardVisible) View.VISIBLE else View.GONE
        binding.textviewLink.text = scheduleUiState.link
        binding.textviewTitleLink
            .apply { visibility = if (scheduleUiState.linkMetadata.isTitleVisible) View.VISIBLE else View.GONE }
            .apply { text = scheduleUiState.linkMetadata.title }
        binding.imageviewBgLinkThumbnail
            .apply { visibility = if (scheduleUiState.linkMetadata.isImageVisible) View.VISIBLE else View.GONE }
            .apply {
                Glide.with(context)
                    .load(scheduleUiState.linkMetadata.imageUrl)
                    .centerCrop()
                    .placeholder(linkThumbnailPlaceHolderDrawable)
                    .into(this)
            }
    }

    companion object {
        fun of(
            parent: ViewGroup,
            onCompleteClicked: (position: Int) -> Unit,
            onDeleteClicked: (position: Int) -> Unit,
            onLinkClicked: (position: Int) -> Unit
        ): ScheduleUiStateViewHolder = ScheduleUiStateViewHolder(
            ViewItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onCompleteClicked,
            onDeleteClicked,
            onLinkClicked
        )
    }
}