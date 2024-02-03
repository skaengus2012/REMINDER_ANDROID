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
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.nlab.reminder.R
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.recyclerview.absoluteAdapterOptionalPosition
import com.nlab.reminder.core.android.recyclerview.bindingAdapterOptionalPosition
import com.nlab.reminder.core.android.transition.transitionListenerOf
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.android.widget.bindSelected
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.data.model.isEmpty
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.databinding.ViewItemScheduleElementBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
internal class ScheduleElementViewHolder(
    internal val binding: ViewItemScheduleElementBinding,
    selectionEnabled: Flow<Boolean>,
    eventListener: ScheduleElementItemEventListener
) : ScheduleItemViewHolder(binding.root) {
    private val linkThumbnailPlaceHolderDrawable: Drawable? = with(itemView) {
        AppCompatResources.getDrawable(context, R.drawable.ic_schedule_link_error)
            ?.let(DrawableCompat::wrap)
            ?.apply { DrawableCompat.setTint(mutate(), context.getThemeColor(R.attr.tint_schedule_placeholder)) }
    }
    private val layoutContentNormalSet: ConstraintSet =
        ConstraintSet().apply { clone(binding.layoutContent) }
    private val layoutContentSelectionSet: ConstraintSet =
        ConstraintSet().apply { load(itemView.context, R.layout.view_item_schedule_element_content_selected) }

    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .withItemPosition()
                .onEach { (view, position) ->
                    eventListener.onCompleteClicked(position, view.isSelected.not())
                }
                .launchIn(lifecycleOwner.lifecycleScope)

            layoutDelete
                .throttleClicks()
                .mapToItemPosition()
                .onEach(eventListener::onDeleteClicked)
                .launchIn(lifecycleOwner.lifecycleScope)

            cardLink
                .throttleClicks()
                .mapToItemPosition()
                .onEach(eventListener::onLinkClicked)
                .launchIn(lifecycleOwner.lifecycleScope)

            buttonSelection
                .touches()
                .filter { event -> event.action == MotionEvent.ACTION_DOWN }
                .onEach {
                    eventListener.onSelectTouched(
                        absolutePosition = absoluteAdapterOptionalPosition ?: return@onEach,
                        isSelected = buttonSelection.isSelected.not()
                    )
                }
                .launchIn(lifecycleOwner.lifecycleScope)

            selectionEnabled
                .flowWithLifecycle(lifecycleOwner.lifecycle)
                .onEach { isSelectionMode -> buttonSelection.isEnabled = isSelectionMode }
                .onEach { isSelectionMode -> buttonComplete.isEnabled = isSelectionMode.not() }
                .onEach { isSelectionMode ->
                    TransitionManager.beginDelayedTransition(
                        layoutContent,
                        AutoTransition().apply {
                            duration = 300
                            addListener(transitionListenerOf(
                                onStart = { layoutDelete.visibility = View.INVISIBLE },
                                onEnd = { layoutDelete.visibility = View.VISIBLE },
                                onCancel = { layoutDelete.visibility = View.VISIBLE }
                            ))
                        }
                    )
                    val applyConstraintSet: ConstraintSet =
                        if (isSelectionMode) layoutContentSelectionSet else layoutContentNormalSet
                    applyConstraintSet.applyTo(layoutContent)
                }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    private fun Flow<View>.withItemPosition(): Flow<Pair<View, Int>> = mapNotNull { v ->
        val position = bindingAdapterOptionalPosition
        if (position == null) null
        else v to position
    }

    private fun Flow<*>.mapToItemPosition(): Flow<Int> = mapNotNull { bindingAdapterOptionalPosition }

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
            bindLinkMetadataImage(linkMetadataImage, linkThumbnailPlaceHolderDrawable)
        }
    }
}

private fun ScheduleElement.completeButtonDescription(context: Context): String =
    context.getString(
        if (isCompleteMarked) R.string.schedule_complete_checkbox_undo_contentDescription
        else R.string.schedule_complete_checkbox_contentDescription,
        title
    )

private fun ImageView.bindLinkMetadataImage(newImage: String?, placeHolder: Drawable?) {
    val oldImage: String? = tag as? String
    if (oldImage == newImage) return
    tag = newImage

    Glide.with(context)
        .load(newImage)
        .override(1000, 400)
        .dontTransform()
        .optionalCenterCrop()
        .placeholder(placeHolder)
        .error(placeHolder)
        .into(/* view= */this)
}