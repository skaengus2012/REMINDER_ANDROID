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

package com.nlab.reminder.core.component.schedulelist.content.ui

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.view.clearFocusIfNeeded
import com.nlab.reminder.core.android.view.clicks
import com.nlab.reminder.core.android.view.filterActionDone
import com.nlab.reminder.core.android.view.focusChanges
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.android.widget.bindCursorVisible
import com.nlab.reminder.core.android.widget.bindImageAsync
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.component.schedulelist.R
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentMirrorBinding
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import com.nlab.reminder.core.kotlinx.coroutines.cancelAllAndClear
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.math.absoluteValue
import kotlin.time.Instant

/**
 * @author Thalys
 */
internal class ContentViewHolder(
    private val binding: LayoutScheduleAdapterItemContentBinding,
    tagsDisplayFormatter: TagsDisplayFormatter,
    themeState: StateFlow<ScheduleListTheme>,
    timeZoneState: StateFlow<TimeZone?>,
    entryAtState: StateFlow<Instant?>,
    scheduleTimingDisplayFormatterState: StateFlow<ScheduleTimingDisplayFormatter?>,
    selectionEnabled: StateFlow<Boolean>,
    selectedScheduleIds: StateFlow<Set<ScheduleId>>,
    completionCheckedScheduleIds: StateFlow<Set<ScheduleId>>,
    onCompletionUpdated: (ScheduleId, Boolean) -> Unit,
    onSimpleEditDone: (SimpleEdit) -> Unit,
    onDragHandleTouched: (RecyclerView.ViewHolder) -> Unit,
    onSelectButtonTouched: (RecyclerView.ViewHolder) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
) : ScheduleAdapterItemViewHolder(binding.root),
    DraggableViewHolder,
    SwipeableViewHolder {
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
    private val selectionAnimDelegate = ContentSelectionAnimDelegate(binding)
    private val draggableViewHolderDelegate = DraggableViewHolderDelegate(binding, selectionAnimDelegate)
    private val swipeableViewHolderDelegate = SwipeableViewHolderDelegate(binding)
    private val bindingId = MutableStateFlow<ScheduleId?>(null)

    override val swipeView: View get() = swipeableViewHolderDelegate.swipeView
    override val clampView: View get() = swipeableViewHolderDelegate.clampView

    init {
        binding.edittextDetail.initialize(tagsDisplayFormatter = tagsDisplayFormatter)

        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.edittextDetail.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleOwner = view.findViewTreeLifecycleOwner() ?: return@doOnAttach
            val viewLifecycleScope = viewLifecycleOwner.lifecycleScope
            val inputFocuses = combine(
                ContentInputFocus.entries.mapNotNull { contentInputFocus ->
                    binding.findInput(contentInputFocus)
                        ?.focusChanges(emitCurrent = true)
                        ?.distinctUntilChanged()
                        ?.map { hasFocus -> if (hasFocus) contentInputFocus else null }
                }
            ) { focuses -> focuses.find { it != null } ?: ContentInputFocus.Nothing }
                .distinctUntilChanged()
                .shareInWithJobCollector(viewLifecycleScope, jobs, replay = 1)
            val hasInputFocusChanges = inputFocuses
                .map { it != ContentInputFocus.Nothing }
                .distinctUntilChanged()
                .shareInWithJobCollector(viewLifecycleScope, jobs, replay = 1)

            jobs += viewLifecycleScope.launch {
                themeState.collect { theme ->
                    binding.buttonComplete.setImageResource(
                        when (theme) {
                            ScheduleListTheme.Point1 -> R.drawable.checkbox_schedule_check_selector_point1
                            ScheduleListTheme.Point2 -> R.drawable.checkbox_schedule_check_selector_point2
                            ScheduleListTheme.Point3 -> R.drawable.checkbox_schedule_check_selector_point3
                        }
                    )
                }
            }
            jobs += viewLifecycleScope.launch {
                themeState.collect { theme ->
                    binding.buttonInfo.apply {
                        imageTintList = ColorStateList.valueOf(theme.getButtonInfoColor(context))
                    }
                }
            }
            jobs += viewLifecycleScope.launch {
                inputFocuses.collect { inputFocus ->
                    val focusedEditText = binding.findInput(inputFocus)
                    binding.getAllInputs().forEach { editText ->
                        editText.bindCursorVisible(isVisible = editText === focusedEditText)
                    }
                }
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChanges.collect(binding.buttonInfo::setVisible)
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChanges.collect { focused -> onFocusChanged(this@ContentViewHolder, focused) }
            }
            jobs += viewLifecycleScope.launch {
                registerEditNoteVisibility(
                    edittextNote = binding.edittextNote,
                    viewHolderEditFocusedFlow = hasInputFocusChanges
                )
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChanges
                    .focusLostCompletelyChanges()
                    .mapNotNull { savable ->
                        if (savable) {
                            bindingId.value?.let { id ->
                                SimpleEdit(
                                    id = id,
                                    binding.edittextTitle.text?.toString().orEmpty(),
                                    binding.edittextNote.text?.toString().orEmpty()
                                )
                            }
                        } else null
                    }
                    .collect(onSimpleEditDone)
            }
            jobs += viewLifecycleScope.launch {
                combine(
                    bindingId.filterNotNull(),
                    completionCheckedScheduleIds
                ) { id, completionCheckedIds -> id in completionCheckedIds }
                    .distinctUntilChanged()
                    .collect { binding.buttonComplete.isSelected = it }
            }
            jobs += viewLifecycleScope.launch {
                bindingId.filterNotNull().collectLatest { id ->
                    // Processed as clicks for quick user response
                    binding.buttonComplete.clicks().collect { v ->
                        onCompletionUpdated(id, v.isSelected.not())
                    }
                }
            }
            jobs += viewLifecycleScope.launch {
                selectionEnabled.collect { enabled ->
                    binding.buttonSelection.isEnabled = enabled
                    binding.buttonDragHandle.isEnabled = enabled
                    binding.buttonComplete.isEnabled = enabled.not()
                }
            }
            jobs += viewLifecycleScope.launch {
                combine(
                    selectionEnabled,
                    selectionAnimDelegate::awaitReady.asFlow()
                ) { enabled, _ -> enabled }
                    .distinctUntilChanged()
                    .collect { selectionAnimDelegate.startAnimation(it) }
            }
            jobs += viewLifecycleScope.launch {
                combine(
                    selectionEnabled,
                    draggableViewHolderDelegate.draggingState,
                    swipeableViewHolderDelegate.swipingState
                ) { selectionUsable, dragging, swiping -> selectionUsable || dragging || swiping }
                    .distinctUntilChanged()
                    .map { it.not() }
                    .collect { enabled ->
                        binding.getAllInputs().forEach { it.isEnabled = enabled }
                    }
            }
            jobs += viewLifecycleScope.launch {
                binding.buttonDragHandle
                    .touches()
                    .filterActionDone()
                    .collect { onDragHandleTouched(this@ContentViewHolder) }
            }
            jobs += viewLifecycleScope.launch {
                binding.buttonSelection
                    .touches()
                    .filter { it.action == MotionEvent.ACTION_DOWN }
                    .collect { onSelectButtonTouched(this@ContentViewHolder) }
            }
            jobs += viewLifecycleScope.launch {
                combine(
                    bindingId.filterNotNull(),
                    selectedScheduleIds
                ) { id, selectedIds -> id in selectedIds }
                    .distinctUntilChanged()
                    .collect { binding.buttonSelection.isSelected = it }
            }
            jobs += viewLifecycleScope.launch {
                timeZoneState.collect { timeZone ->
                    if (timeZone == null) return@collect
                    binding.edittextDetail.bindTimeZone(timeZone)
                }
            }
            jobs += viewLifecycleScope.launch {
                entryAtState.collect { entryAt ->
                    if (entryAt == null) return@collect
                    binding.edittextDetail.bindEntryAt(entryAt)
                }
            }
            jobs += viewLifecycleScope.launch {
                scheduleTimingDisplayFormatterState.collect { formatter ->
                    if (formatter == null) return@collect
                    binding.edittextDetail.bindScheduleTimingDisplayFormatter(formatter)
                }
            }
        }
        itemView.doOnDetach {
            selectionAnimDelegate.clearResources()
            jobs.cancelAllAndClear()
        }
    }

    fun bind(item: ScheduleListItem.Content) {
        bindingId.value = item.resource.schedule.id
        binding.viewLine
            .setVisible(isVisible = item.isLineVisible, goneIfNotVisible = false)
        binding.edittextTitle.apply {
            bindText(item.resource.schedule.title.value)
            clearFocusIfNeeded()
        }
        binding.edittextNote.apply {
            val isChanged = bindText(item.resource.schedule.note?.value)
            if (isChanged) {
                setSelection(text?.length ?: 0)
            }
            clearFocusIfNeeded()
        }
        binding.edittextDetail.apply {
            bindScheduleData(
                scheduleCompleted = item.resource.schedule.isComplete,
                scheduleTiming = item.resource.schedule.timing,
                tags = item.resource.schedule.tags
            )
            clearFocusIfNeeded()
        }
        binding.cardLink
            .setVisible(isVisible = item.resource.schedule.link != null)
        binding.textviewLink
            .bindText(item.resource.schedule.link?.rawLink?.value)
        binding.textviewTitleLink.apply {
            val linkTitle = item.resource.schedule.linkMetadata?.title?.value
            setVisible(isVisible = linkTitle != null)
            bindText(linkTitle)
        }
        binding.imageviewBgLinkThumbnail.apply {
            val linkImageUrl = item.resource.schedule.linkMetadata?.imageUrl?.value
            setVisible(isVisible = linkImageUrl != null)
            bindImageAsync(
                url = linkImageUrl,
                placeHolder = linkThumbnailPlaceHolderDrawable,
                error = linkThumbnailPlaceHolderDrawable,
                enableCrossfade = true
            )
        }
    }

    override fun userDraggable(): Boolean {
        return draggableViewHolderDelegate.userDraggable()
    }

    override fun isScaleOnDraggingNeeded(): Boolean {
        return draggableViewHolderDelegate.isScaleOnDraggingNeeded()
    }

    override fun onDragStateChanged(isActive: Boolean) {
        draggableViewHolderDelegate.onDragStateChanged(isActive)
    }

    override fun mirrorView(parent: ViewGroup, viewPool: DraggingMirrorViewPool): View {
        return draggableViewHolderDelegate.mirrorView(parent, viewPool)
    }

    override fun userSwipeable(): Boolean {
        return swipeableViewHolderDelegate.userSwipeable()
    }

    override fun onSwipe(dx: Float) {
        swipeableViewHolderDelegate.onSwipe(dx)
    }
}

private class DraggableViewHolderDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
    private val selectionAnimDelegate: ContentSelectionAnimDelegate
) : DraggableViewHolder {
    private val _draggingState = MutableStateFlow(false)
    val draggingState: StateFlow<Boolean> = _draggingState.asStateFlow()

    override fun userDraggable(): Boolean {
        return binding.isInteractable()
    }

    override fun isScaleOnDraggingNeeded(): Boolean {
        return binding.imageviewBgLinkThumbnail.isVisible
    }

    override fun onDragStateChanged(isActive: Boolean) {
        _draggingState.value = isActive
    }

    override fun mirrorView(
        parent: ViewGroup,
        viewPool: DraggingMirrorViewPool
    ): View {
        val key = ContentViewHolder::class
        val mirrorBinding = viewPool.get(key)
            ?.let { LayoutScheduleAdapterItemContentMirrorBinding.bind(it) }
            ?: run {
                LayoutScheduleAdapterItemContentMirrorBinding
                    .inflate(
                        /*inflater = */ LayoutInflater.from(parent.context),
                        /*parent = */ parent,
                        /*attachToParent = */ false
                    )
                    .apply {
                        root.alpha = 0.8f
                        buttonComplete.setImageDrawable(binding.buttonComplete.drawable)
                    }
                    .also { viewPool.put(key, it.root) }
            }
        with(mirrorBinding) {
            edittextTitle.bindText(binding.edittextTitle.text)

            edittextNote.bindText(binding.edittextNote.text)
            edittextNote.visibility = binding.edittextNote.visibility

            edittextDetail.bindText(binding.edittextDetail.text)

            cardLink.visibility = binding.cardLink.visibility
            textviewLink.bindText(binding.textviewLink.text)

            textviewTitleLink.bindText(binding.textviewTitleLink.text)
            textviewTitleLink.visibility = binding.textviewTitleLink.visibility

            imageviewBgLinkThumbnail.visibility = binding.imageviewBgLinkThumbnail.visibility
            imageviewBgLinkThumbnail.setImageDrawable(binding.imageviewBgLinkThumbnail.drawable)
        }
        selectionAnimDelegate.applyStateToMirror(mirrorBinding)

        return mirrorBinding.root
    }
}

private class SwipeableViewHolderDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) : SwipeableViewHolder {
    private val clampAlphaOrigin: Float = binding.layoutClampDim.alpha

    private val _swipingState = MutableStateFlow(false)
    val swipingState: StateFlow<Boolean> = _swipingState.asStateFlow()

    override val swipeView: View = binding.layoutContent
    override val clampView: View = binding.buttonDelete

    override fun userSwipeable(): Boolean {
        return binding.isInteractable()
    }

    override fun onSwipe(dx: Float) {
        val isActive = dx.absoluteValue != 0f
        _swipingState.value = isActive
        binding.layoutClamp.setVisible(isVisible = isActive, goneIfNotVisible = false)
        binding.layoutClampDim.alpha = clampAlphaOrigin - dx.absoluteValue / clampView.width * clampAlphaOrigin
    }
}

private enum class ContentInputFocus {
    Title, Note, Detail, Nothing
}

private fun LayoutScheduleAdapterItemContentBinding.getAllInputs(): Iterable<EditText> = listOf(
    edittextTitle,
    edittextNote,
    edittextDetail
)

private fun LayoutScheduleAdapterItemContentBinding.isInteractable(): Boolean {
    return edittextTitle.hasSelection().not()
            && edittextNote.hasSelection().not()
            // Tag interactions often conflict with drag or swipe events
            // Should block interactions when focus is present
            && edittextDetail.hasFocus().not()
}

private fun LayoutScheduleAdapterItemContentBinding.findInput(contentInputFocus: ContentInputFocus): EditText? {
    return when (contentInputFocus) {
        ContentInputFocus.Title -> edittextTitle
        ContentInputFocus.Note -> edittextNote
        ContentInputFocus.Detail -> edittextDetail
        ContentInputFocus.Nothing -> null
    }
}