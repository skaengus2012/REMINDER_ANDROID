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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemFooterFormBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemFormBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemHeadlineBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemHeadlinePaddingBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterListGroupHeaderDefaultBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterListGroupHeaderSubDefaultBinding
import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.collections.immutable.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

private const val ITEM_VIEW_TYPE_CONTENT = 1
private const val ITEM_VIEW_TYPE_FOOTER_FORM = 2
private const val ITEM_VIEW_TYPE_FORM = 3
private const val ITEM_VIEW_TYPE_HEADLINE = 4
private const val ITEM_VIEW_TYPE_HEADLINE_PADDING = 5
private const val ITEM_VIEW_TYPE_GROUP_HEADER = 6
private const val ITEM_VIEW_TYPE_SUB_GROUP_HEADER = 7

/**
 * @author Thalys
 */
internal class ScheduleListAdapter(
    lifecycleScope: LifecycleCoroutineScope,
) : RecyclerView.Adapter<ScheduleAdapterItemViewHolder>() {
    private val differ = ScheduleListDiffer(listUpdateCallback = AdapterListUpdateCallback(/* adapter = */ this))
    private val dateTimeFormatPool = DateTimeFormatPool()
    private val tagsDisplayFormatter = TagsDisplayFormatter()
    private val themeState = MutableStateFlow(ScheduleListTheme.Point1)
    private val timeZoneState = MutableStateFlow<TimeZone?>(null)
    private val entryAtState = MutableStateFlow<Instant?>(null)
    private val scheduleTimingDisplayFormatterState = MutableStateFlow<ScheduleTimingDisplayFormatter?>(null)
    private val selectionEnabled = MutableStateFlow(false)

    private val completionCheckedScheduleIds = SyncableState<PersistentSet<ScheduleId>>(
        lifecycleScope = lifecycleScope,
        initialValue = persistentHashSetOf(),
        syncDuration = 300L
    )
    private val _completionUpdateRequests = MutableEventSharedFlow<CompletionUpdate>()
    val completionUpdateRequests: SharedFlow<CompletionUpdate> = _completionUpdateRequests.asSharedFlow()

    private val selectedScheduleIds = SyncableState<PersistentSet<ScheduleId>>(
        lifecycleScope = lifecycleScope,
        initialValue = persistentHashSetOf(),
        syncDuration = 300L
    )
    private val _selectionUpdateRequests = MutableIdentityStateFlow<SelectionUpdate>()
    val selectionUpdateRequests: IdentityStateFlow<SelectionUpdate> = _selectionUpdateRequests.asStateFlow()

    private val _addRequests = MutableEventSharedFlow<SimpleAdd>()
    val addRequests: SharedFlow<SimpleAdd> = _addRequests.asSharedFlow()

    private val _editRequests = MutableEventSharedFlow<SimpleEdit>()
    val editRequests: SharedFlow<SimpleEdit> = _editRequests.asSharedFlow()

    private val _dragHandleTouches = MutableEventSharedFlow<RecyclerView.ViewHolder>()
    val dragHandleTouches: SharedFlow<RecyclerView.ViewHolder> = _dragHandleTouches.asSharedFlow()

    private val _selectButtonTouches = MutableEventSharedFlow<RecyclerView.ViewHolder>()
    val selectButtonTouches: SharedFlow<RecyclerView.ViewHolder> = _selectButtonTouches.asSharedFlow()

    private val _focusChanges = MutableEventSharedFlow<FocusChange>()
    val focusChanges: SharedFlow<FocusChange> = _focusChanges.asSharedFlow()

    private fun getItem(position: Int): ScheduleListItem {
        return differ.getCurrentList()[position]
    }

    override fun getItemCount(): Int = differ.getCurrentList().size

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ScheduleListItem.Form -> ITEM_VIEW_TYPE_FORM
        is ScheduleListItem.Content -> ITEM_VIEW_TYPE_CONTENT
        is ScheduleListItem.FooterForm -> ITEM_VIEW_TYPE_FOOTER_FORM
        is ScheduleListItem.Headline -> ITEM_VIEW_TYPE_HEADLINE
        is ScheduleListItem.HeadlinePadding -> ITEM_VIEW_TYPE_HEADLINE_PADDING
        is ScheduleListItem.GroupHeader -> ITEM_VIEW_TYPE_GROUP_HEADER
        is ScheduleListItem.SubGroupHeader -> ITEM_VIEW_TYPE_SUB_GROUP_HEADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapterItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_CONTENT -> {
                ContentViewHolder(
                    binding = LayoutScheduleAdapterItemContentBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    tagsDisplayFormatter = tagsDisplayFormatter,
                    themeState = themeState,
                    timeZoneState = timeZoneState,
                    entryAtState = entryAtState,
                    scheduleTimingDisplayFormatterState = scheduleTimingDisplayFormatterState,
                    selectionEnabled = selectionEnabled,
                    selectedScheduleIds = selectedScheduleIds.state,
                    completionCheckedScheduleIds = completionCheckedScheduleIds.state,
                    onCompletionUpdated = { completionUpdate ->
                        val currentIds = completionCheckedScheduleIds.snapshot()
                        completionCheckedScheduleIds.set(
                            if (completionUpdate.targetCompleted) {
                                currentIds + completionUpdate.id
                            } else {
                                currentIds - completionUpdate.id
                            }
                        )
                        _completionUpdateRequests.tryEmit(completionUpdate)
                    },
                    onSimpleEditDone = { _editRequests.tryEmit(it) },
                    onDragHandleTouched = { _dragHandleTouches.tryEmit(it) },
                    onSelectButtonTouched = { _selectButtonTouches.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused ->
                        _focusChanges.tryEmit(FocusChange(viewHolder, focused))
                    },
                )
            }

            ITEM_VIEW_TYPE_FOOTER_FORM -> {
                FooterFormViewHolder(
                    binding = LayoutScheduleAdapterItemFooterFormBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    themeState = themeState,
                    onSimpleAddDone = { _addRequests.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused -> _focusChanges.tryEmit(FocusChange(viewHolder, focused)) },
                )
            }

            ITEM_VIEW_TYPE_FORM -> {
                FormViewHolder(
                    binding = LayoutScheduleAdapterItemFormBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    themeState = themeState,
                    onSimpleAddDone = { _addRequests.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused -> _focusChanges.tryEmit(FocusChange(viewHolder, focused)) }
                )
            }

            ITEM_VIEW_TYPE_HEADLINE -> {
                HeadlineViewHolder(
                    binding = LayoutScheduleAdapterItemHeadlineBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    themeState = themeState
                )
            }

            ITEM_VIEW_TYPE_HEADLINE_PADDING -> {
                HeadlinePaddingViewHolder(
                    binding = LayoutScheduleAdapterItemHeadlinePaddingBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                )
            }

            ITEM_VIEW_TYPE_GROUP_HEADER -> {
                GroupHeaderViewHolder(
                    binding = LayoutScheduleAdapterListGroupHeaderDefaultBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    )
                )
            }

            ITEM_VIEW_TYPE_SUB_GROUP_HEADER -> {
                SubGroupHeaderViewHolder(
                    binding = LayoutScheduleAdapterListGroupHeaderSubDefaultBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    )
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: ScheduleAdapterItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FormViewHolder -> holder.bind(item as ScheduleListItem.Form)
            is ContentViewHolder -> holder.bind(item as ScheduleListItem.Content)
            is FooterFormViewHolder -> holder.bind(item as ScheduleListItem.FooterForm)
            is HeadlineViewHolder -> holder.bind(item as ScheduleListItem.Headline)
            is GroupHeaderViewHolder -> holder.bind(item as ScheduleListItem.GroupHeader)
            is SubGroupHeaderViewHolder -> holder.bind(item as ScheduleListItem.SubGroupHeader)
            is HeadlinePaddingViewHolder -> Unit
        }
    }

    fun setSelectionEnabled(isEnabled: Boolean) {
        selectionEnabled.value = isEnabled
    }

    fun syncCompletionChecked(completionCheckedIds: PersistentSet<ScheduleId>) {
        completionCheckedScheduleIds.sync(completionCheckedIds)
    }

    fun syncSelected(selectedIds: PersistentSet<ScheduleId>) {
        selectedScheduleIds.sync(selectedIds)
    }

    fun setSelected(scheduleId: ScheduleId, selected: Boolean) {
        val current = selectedScheduleIds.snapshot()
        val updated = if (selected) current + scheduleId else current - scheduleId
        if (current != updated) {
            selectedScheduleIds.set(updated)
            _selectionUpdateRequests.update(SelectionUpdate(selectedIds = updated))
        }
    }

    fun submitMoving(fromPosition: Int, toPosition: Int): Boolean {
        return differ.tryMove(fromPosition, toPosition)
    }

    fun submitMoveDone() {
        differ.syncMoving(commitCallback = {})
    }

    fun submitList(items: List<ScheduleListItem>?, commitCallback: CommitCallback) {
        scheduleTimingDisplayFormatterState.value?.releaseCache()
        tagsDisplayFormatter.releaseCache()
        differ.submitList(items, commitCallback)
    }

    fun getCurrentList(): List<ScheduleListItem> {
        return differ.getCurrentList()
    }

    fun getCurrentSelectedIds(): Set<ScheduleId> {
        return selectedScheduleIds.state.value
    }

    fun updateTheme(theme: ScheduleListTheme) {
        themeState.value = theme
    }

    fun updateTimeZone(timeZone: TimeZone) {
        timeZoneState.value = timeZone
    }

    fun updateEntryAt(entryAt: Instant) {
        entryAtState.value = entryAt
    }

    fun updateTriggerAtFormatPatterns(patterns: TriggerAtFormatPatterns) {
        scheduleTimingDisplayFormatterState.value?.releaseCache()
        scheduleTimingDisplayFormatterState.value = ScheduleTimingDisplayFormatter(
            triggerAtFormatPatterns = patterns,
            dateTimeFormatPool = dateTimeFormatPool
        )
    }
}

@Suppress("FunctionName")
private fun <T> MutableEventSharedFlow(extraBufferCapacity: Int = 128): MutableSharedFlow<T> = MutableSharedFlow(
    extraBufferCapacity = extraBufferCapacity,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)