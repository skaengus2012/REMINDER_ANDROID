/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.schedule.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.nlab.reminder.R
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.recyclerview.bindingAdapterOptionalPosition
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.widget.bindSelected
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.data.model.isEmpty
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.databinding.ViewItemScheduleElementBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
internal class ScheduleElementViewHolder(
    internal val binding: ViewItemScheduleElementBinding,
    onCompleteClicked: (position: Int, isComplete: Boolean) -> Unit
) : ScheduleItemViewHolder(binding.root) {
    private val linkThumbnailPlaceHolderDrawable: Drawable? = with(itemView) {
        AppCompatResources.getDrawable(context, R.drawable.ic_schedule_link_error)
            ?.let(DrawableCompat::wrap)
            ?.apply { DrawableCompat.setTint(mutate(), context.getThemeColor(R.attr.tint_schedule_placeholder)) }
    }
    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .onEach { view ->
                    val position = bindingAdapterOptionalPosition ?: return@onEach
                    onCompleteClicked(position, view.isSelected.not())
                }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    fun onBind(scheduleElement: ScheduleElement) {
        val isTitleChanged: Boolean

        binding.edittextTitle
            .bindText(scheduleElement.title)
            .also { isTitleChanged = it }
        binding.edittextNote.bindText(scheduleElement.note)
        binding.buttonComplete.apply {
            bindSelected(scheduleElement.isCompleteMarked)
            if (isTitleChanged) {
                contentDescription = scheduleElement.completeButtonDescription(context)
            }
        }
        // link binding
        binding.cardLink.visibility = if (scheduleElement.link.isEmpty()) View.GONE else View.VISIBLE
        binding.textviewLink.bindText(scheduleElement.link.value)
        binding.textviewTitleLink.apply {
            val linkMetadataTitle = scheduleElement.linkMetadata?.title
            visibility = if (linkMetadataTitle.isNullOrBlank()) View.GONE else View.VISIBLE
            bindText(linkMetadataTitle)
        }
        binding.imageviewBgLinkThumbnail.apply {
            val linkMetadataImage = scheduleElement.linkMetadata?.imageUrl
            visibility = if (linkMetadataImage.isNullOrBlank()) View.GONE else View.VISIBLE
            Glide.with(context)
                .load(linkMetadataImage)
                .override(1000, 400)
                .dontTransform()
                .optionalCenterCrop()
                .placeholder(linkThumbnailPlaceHolderDrawable)
                .error(linkThumbnailPlaceHolderDrawable)
                .into(this)
        }
    }
}

private fun ScheduleElement.completeButtonDescription(context: Context): String =
    context.getString(
        if (isCompleteMarked) R.string.schedule_complete_checkbox_undo_contentDescription
        else R.string.schedule_complete_checkbox_contentDescription,
        title
    )