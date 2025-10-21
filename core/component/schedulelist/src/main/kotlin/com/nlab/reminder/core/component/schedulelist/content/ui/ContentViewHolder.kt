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
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.android.view.clearFocusIfNeeded
import com.nlab.reminder.core.android.view.filterActionDone
import com.nlab.reminder.core.android.view.focusChanges
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.view.throttleClicks
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.math.absoluteValue
import kotlin.time.Instant

/**
 * @author Thalys
 */
internal class ContentViewHolder(
    private val binding: LayoutScheduleAdapterItemContentBinding,
    themeState: StateFlow<ScheduleListTheme>,
    scheduleTimingDisplayFormatter: ScheduleTimingDisplayFormatter,
    tagsDisplayFormatter: TagsDisplayFormatter,
    timeZone: Flow<TimeZone>,
    entryAt: Flow<Instant>,
    selectionEnabled: StateFlow<Boolean>,
    selectedScheduleIds: StateFlow<Set<ScheduleId>>,
    onItemViewTouched: (RecyclerView.ViewHolder) -> Unit,
    onSimpleEditDone: (SimpleEdit) -> Unit,
    onDragHandleTouched: (RecyclerView.ViewHolder) -> Unit,
    onSelectButtonTouched: (RecyclerView.ViewHolder) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
) : ScheduleAdapterItemViewHolder(binding.root),
    DraggableViewHolder,
    SwipeSupportable {
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
    private val bindingId = MutableStateFlow<ScheduleId?>(null)

    private val _draggingDelegate = ContentDraggingDelegate(binding)
    override val draggingDelegate: DraggingDelegate = _draggingDelegate

    private val _swipeDelegate = ContentSwipeDelegate(binding)
    override val swipeDelegate: SwipeDelegate = _swipeDelegate

    init {
        binding.edittextDetail.initialize(
            scheduleTimingDisplayFormatter = scheduleTimingDisplayFormatter,
            tagsDisplayFormatter = tagsDisplayFormatter
        )

        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.edittextDetail.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleOwner = view.findViewTreeLifecycleOwner() ?: return@doOnAttach
            val viewLifecycleScope = viewLifecycleOwner.lifecycleScope
            val inputFocusFlow = combine(
                ContentInputFocus.entries.mapNotNull { contentInputFocus ->
                    binding.findInput(contentInputFocus)
                        ?.focusChanges(emitCurrent = true)
                        ?.distinctUntilChanged()
                        ?.map { hasFocus -> if (hasFocus) contentInputFocus else null }
                }
            ) { focuses -> focuses.find { it != null } ?: ContentInputFocus.Nothing }
                .distinctUntilChanged()
                .shareInWithJobCollector(viewLifecycleScope, jobs, replay = 1)
            val hasInputFocusChangesFlow = inputFocusFlow
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
                val touchEvents = binding.getAllInputs().map { it.touches() } + binding.layoutContent.touches()
                touchEvents.merge()
                    .filterActionDone()
                    .collect { onItemViewTouched(this@ContentViewHolder) }
            }
            jobs += viewLifecycleScope.launch {
                inputFocusFlow.collect { inputFocus ->
                    val focusedEditText = binding.findInput(inputFocus)
                    binding.getAllInputs().forEach { editText ->
                        editText.bindCursorVisible(isVisible = editText === focusedEditText)
                    }
                }
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChangesFlow.collect(binding.buttonInfo::setVisible)
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChangesFlow.collect { focused -> onFocusChanged(this@ContentViewHolder, focused) }
            }
            jobs += viewLifecycleScope.launch {
                registerEditNoteVisibility(
                    edittextNote = binding.edittextNote,
                    viewHolderEditFocusedFlow = hasInputFocusChangesFlow
                )
            }
            jobs += viewLifecycleScope.launch {
                hasInputFocusChangesFlow
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
                binding.buttonComplete
                    .throttleClicks()
                    .collect { binding.buttonComplete.apply { it.isSelected = it.isSelected.not() } }
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
                ) { enabled, _ -> enabled }.distinctUntilChanged().collect { selectionAnimDelegate.startAnimation(it) }
            }
            jobs += viewLifecycleScope.launch {
                combine(
                    selectionEnabled,
                    _draggingDelegate.draggingFlow,
                    _swipeDelegate.swipeFlow
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
                combine(bindingId.filterNotNull(), selectedScheduleIds) { id, selectedIds -> id in selectedIds }
                    .distinctUntilChanged()
                    .collect { binding.buttonSelection.isSelected = it }
            }
            jobs += viewLifecycleScope.launch {
                timeZone.collect { binding.edittextDetail.bindTimeZone(it) }
            }
            jobs += viewLifecycleScope.launch {
                entryAt.collect { binding.edittextDetail.bindEntryAt(it) }
            }
        }
        itemView.doOnDetach {
            jobs.cancelAllAndClear()
            selectionAnimDelegate.cancelAnimation()
        }
    }

    fun bind(item: ScheduleListItem.Content) {
        bindingId.value = item.schedule.resource.id
        binding.viewLine
            .setVisible(isVisible = item.isLineVisible, goneIfNotVisible = false)
        binding.edittextTitle.apply {
            bindText(item.schedule.resource.title.value)
            clearFocusIfNeeded()
        }
        binding.edittextNote.apply {
            val isChanged = bindText(item.schedule.resource.note?.value)
            if (isChanged) {
                setSelection(text?.length ?: 0)
            }
            clearFocusIfNeeded()
        }
        binding.edittextDetail.apply {
            bindScheduleData(
                scheduleCompleted = item.schedule.resource.isComplete,
                scheduleTiming = item.schedule.resource.timing,
                tags = item.schedule.resource.tags
            )
            clearFocusIfNeeded()
        }
        binding.cardLink
            .setVisible(isVisible = item.schedule.resource.link != null)
        binding.textviewLink
            .bindText(item.schedule.resource.link?.rawLink?.value)
        binding.textviewTitleLink.apply {
            val linkTitle = item.schedule.resource.linkMetadata?.title?.value
            setVisible(isVisible = linkTitle != null)
            bindText(linkTitle)
        }
        binding.imageviewBgLinkThumbnail.apply {
            val linkImageUrl = item.schedule.resource.linkMetadata?.imageUrl?.value
            setVisible(isVisible = linkImageUrl != null)
            bindImageAsync(
                url = linkImageUrl,
                placeHolder = linkThumbnailPlaceHolderDrawable,
                error = linkThumbnailPlaceHolderDrawable
            )
        }
    }
}

private class ContentDraggingDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) : DraggingDelegate() {
    private val _draggingFlow = MutableStateFlow(false)
    val draggingFlow: StateFlow<Boolean> = _draggingFlow.asStateFlow()

    // Prevent start dragging, when input selection double click!
    override fun userDraggable(): Boolean {
        return binding.isInteractable()
    }

    override fun isScaleOnDraggingNeeded(): Boolean {
        return binding.imageviewBgLinkThumbnail.isVisible
    }

    override fun onDragStateChanged(isActive: Boolean) {
        _draggingFlow.value = isActive
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
                        root.alpha = 0.9f
                        buttonComplete.setImageDrawable(binding.buttonComplete.drawable)
                    }
                    .also { viewPool.put(key, it.root) }
            }
        mirrorBinding.apply {
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

            // TODO migrate animation
            buttonComplete.apply {
                isSelected = binding.buttonComplete.isSelected
                alpha = binding.buttonComplete.alpha
                translationX = binding.buttonComplete.translationX
            }
            buttonSelection.apply {
                isSelected = binding.buttonSelection.isSelected
                alpha = binding.buttonSelection.alpha
                translationX = binding.buttonSelection.translationX
            }
            buttonDragHandle.apply {
                alpha = binding.buttonDragHandle.alpha
                translationX = binding.buttonDragHandle.translationX
            }
            layoutData.updateLayoutParams { width = binding.layoutData.width }
        }
        return mirrorBinding.root
    }
}

private class ContentSwipeDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) : SwipeDelegate() {
    private val clampAlphaOrigin: Float = binding.layoutClampDim.alpha
    private val _swipeFlow = MutableStateFlow(false)
    val swipeFlow: StateFlow<Boolean> = _swipeFlow.asStateFlow()

    override val userSwipeable: Boolean get() = binding.isInteractable()
    override val swipeView: View get() = binding.layoutContent
    override val clampView: View get() = binding.buttonDelete

    override fun onSwipe(dx: Float) {
        val isActive = dx.absoluteValue != 0f
        _swipeFlow.value = isActive
        binding.layoutClamp.setVisible(isVisible = true, goneIfNotVisible = false)
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