/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedule.ui.view.list

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.InputType
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.view.focusState
import com.nlab.reminder.core.android.view.inputmethod.hideSoftInputFromWindow
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.widget.bindImageAsync
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.android.widget.textChanges
import com.nlab.reminder.core.component.schedule.R
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import com.nlab.reminder.core.kotlinx.coroutine.flow.withPrev
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal class ScheduleContentViewHolder(
    private val binding: LayoutScheduleAdapterItemContentBinding,
    private val selectionEnabled: Flow<Boolean>,
    private val onSimpleEditDone: (SimpleEdit) -> Unit,
    theme: ScheduleListTheme
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val linkThumbnailPlaceHolderDrawable: Drawable? = with(itemView) {
        AppCompatResources.getDrawable(context, R.drawable.ic_schedule_link_error)
            ?.let(DrawableCompat::wrap)
            ?.apply {
                DrawableCompat.setTint(
                    mutate(),
                    context.getThemeColor(AttrIds.content_2)
                )
            }
    }
    private val selectionAnimDelegate = ScheduleContentSelectionAnimDelegate(binding)
    private val bindingId = MutableStateFlow<ScheduleId?>(null)

    init {
        binding.buttonComplete.setImageResource(
            when (theme) {
                ScheduleListTheme.Point1 -> R.drawable.checkbox_schedule_check_selector_point1
                ScheduleListTheme.Point2 -> R.drawable.checkbox_schedule_check_selector_point2
                ScheduleListTheme.Point3 -> R.drawable.checkbox_schedule_check_selector_point3
            }
        )
        binding.buttonInfo.apply {
            imageTintList = ColorStateList.valueOf(
                context.getThemeColor(
                    when (theme) {
                        ScheduleListTheme.Point1 -> AttrIds.point_1
                        ScheduleListTheme.Point2 -> AttrIds.point_2
                        ScheduleListTheme.Point3 -> AttrIds.point_3
                    }
                )
            )
        }

        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.edittextNote.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleOwner = view.findViewTreeLifecycleOwner() ?: return@doOnAttach
            val viewLifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope
            val itemFocusedFlow = MutableStateFlow(false)
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    binding.edittextTitle.focusState(),
                    binding.edittextNote.focusState(),
                    transform = { s1, s2 -> s1 || s2 }
                ).collect { itemFocusedFlow.value = it }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    binding.edittextNote
                        .textChanges()
                        .map { it.isNullOrEmpty() }
                        .distinctUntilChanged(),
                    itemFocusedFlow,
                    transform = { isCurrentNoteEmpty, focused -> isCurrentNoteEmpty.not() || focused }
                ).collect { binding.edittextNote.setVisible(it) }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                itemFocusedFlow.collect { focused ->
                    binding.buttonInfo.setVisible(focused)
                    if (focused.not()) itemView.hideSoftInputFromWindow()
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                binding.buttonComplete
                    .throttleClicks()
                    .collect { binding.buttonComplete.apply { it.isSelected = it.isSelected.not() } }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                itemFocusedFlow
                    .withPrev(initial = false)
                    .distinctUntilChanged()
                    .filter { (old, new) -> old && new.not() }
                    .mapNotNull {
                        bindingId.value?.let { id ->
                            SimpleEdit(
                                id = id,
                                title = binding.edittextTitle.text?.toString().orEmpty(),
                                note = binding.edittextNote.text?.toString().orEmpty()
                            )
                        }
                    }
                    .collect { onSimpleEditDone(it) }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    selectionEnabled,
                    selectionAnimDelegate::awaitReady.asFlow()
                ) { enabled, _ -> enabled }.collect { enabled ->
                    selectionAnimDelegate.startAnimation(enabled)
                }
            }
        }
        itemView.doOnDetach {
            jobs.forEach { it.cancel() }
            selectionAnimDelegate.cancelAnimation()
        }
    }

    fun bind(item: ScheduleAdapterItem.Content) {
        bindingId.value =
            item.scheduleDetail.schedule.id
        binding.edittextTitle
            .bindText(item.scheduleDetail.schedule.content.title)
        binding.edittextNote
            .bindText(item.scheduleDetail.schedule.content.note?.value)
        binding.cardLink
            .setVisible(item.scheduleDetail.schedule.content.link != null)
        binding.textviewLink
            .bindText(item.scheduleDetail.schedule.content.link?.value)
        binding.textviewTitleLink.apply {
            val linkTitle = item.scheduleDetail.linkMetadata?.title
            setVisible(linkTitle.isNullOrBlank().not())
            bindText(linkTitle)
        }
        binding.imageviewBgLinkThumbnail.apply {
            val linkImageUrl = item.scheduleDetail.linkMetadata?.imageUrl
            setVisible(linkImageUrl.isNullOrBlank().not())
            bindImageAsync(
                url = linkImageUrl,
                placeHolder = linkThumbnailPlaceHolderDrawable,
                error = linkThumbnailPlaceHolderDrawable
            )
        }
    }
}