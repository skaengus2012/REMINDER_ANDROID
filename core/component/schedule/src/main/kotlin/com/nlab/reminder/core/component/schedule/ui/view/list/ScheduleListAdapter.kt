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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemAddBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemFooterAddBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlineBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlinePaddingBinding
import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update

private const val ITEM_VIEW_TYPE_ADD = 1
private const val ITEM_VIEW_TYPE_CONTENT = 2
private const val ITEM_VIEW_TYPE_FOOTER_ADD = 3
private const val ITEM_VIEW_TYPE_HEADLINE = 4
private const val ITEM_VIEW_TYPE_HEADLINE_PADDING = 5

/**
 * @author Thalys
 */
class ScheduleListAdapter(
    private val theme: ScheduleListTheme
) : RecyclerView.Adapter<ScheduleAdapterItemViewHolder>() {
    private val differ = ScheduleListDiffer(listUpdateCallback = AdapterListUpdateCallback(/* adapter = */ this))

    private val selectionEnabled = MutableStateFlow(false)
    private val selectedScheduleIds = MutableStateFlow<Set<ScheduleId>>(emptySet())

    private val _addRequest = MutableEventSharedFlow<SimpleAdd>()
    val addRequest: Flow<SimpleAdd> = _addRequest.asSharedFlow()

    private val _editRequest = MutableEventSharedFlow<SimpleEdit>()
    val editRequest: Flow<SimpleEdit> = _editRequest.distinctUntilChanged()

    private val _dragHandleTouch = MutableEventSharedFlow<RecyclerView.ViewHolder>()
    val dragHandleTouch: Flow<RecyclerView.ViewHolder> = _dragHandleTouch.conflate()

    private val _selectButtonTouch = MutableEventSharedFlow<RecyclerView.ViewHolder>()
    val selectButtonTouch: Flow<RecyclerView.ViewHolder> = _selectButtonTouch.conflate()

    private val _focusChange = MutableEventSharedFlow<FocusChange>()
    val focusChange: Flow<FocusChange> = _focusChange.asSharedFlow()

    private val _footerAddBottomPaddingVisible = MutableStateFlow(false)
    val footerAddBottomPaddingVisible: StateFlow<Boolean> = _footerAddBottomPaddingVisible.asStateFlow()

    private fun getItem(position: Int): ScheduleAdapterItem {
        return differ.getCurrentList()[position]
    }

    override fun getItemCount(): Int = differ.getCurrentList().size

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ScheduleAdapterItem.Add -> ITEM_VIEW_TYPE_ADD
        is ScheduleAdapterItem.Content -> ITEM_VIEW_TYPE_CONTENT
        is ScheduleAdapterItem.FooterAdd -> ITEM_VIEW_TYPE_FOOTER_ADD
        is ScheduleAdapterItem.Headline -> ITEM_VIEW_TYPE_HEADLINE
        is ScheduleAdapterItem.HeadlinePadding -> ITEM_VIEW_TYPE_HEADLINE_PADDING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapterItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_ADD -> {
                AddViewHolder(
                    binding = LayoutScheduleAdapterItemAddBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    theme = theme,
                    onSimpleAddDone = { _addRequest.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused -> _focusChange.tryEmit(FocusChange(viewHolder, focused)) }
                )
            }
            ITEM_VIEW_TYPE_CONTENT -> {
                ContentViewHolder(
                    binding = LayoutScheduleAdapterItemContentBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    theme = theme,
                    selectionEnabled = selectionEnabled,
                    selectedScheduleIds = selectedScheduleIds,
                    onSimpleEditDone = { _editRequest.tryEmit(it) },
                    onDragHandleTouched = { _dragHandleTouch.tryEmit(it) },
                    onSelectButtonTouched = { _selectButtonTouch.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused -> _focusChange.tryEmit(FocusChange(viewHolder, focused)) },
                )
            }

            ITEM_VIEW_TYPE_FOOTER_ADD -> {
                FooterAddViewHolder(
                    binding = LayoutScheduleAdapterItemFooterAddBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    theme = theme,
                    onSimpleAddDone = { _addRequest.tryEmit(it) },
                    onFocusChanged = { viewHolder, focused -> _focusChange.tryEmit(FocusChange(viewHolder, focused)) },
                    onBottomPaddingVisible = { _footerAddBottomPaddingVisible.value = it }
                )
            }

            ITEM_VIEW_TYPE_HEADLINE -> {
                HeadlineViewHolder(
                    binding = LayoutScheduleAdapterItemHeadlineBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    theme = theme
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

            else -> {
                throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: ScheduleAdapterItemViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AddViewHolder -> holder.bind(item as ScheduleAdapterItem.Add)
            is ContentViewHolder -> holder.bind(item as ScheduleAdapterItem.Content)
            is FooterAddViewHolder -> holder.bind(item as ScheduleAdapterItem.FooterAdd)
            is HeadlineViewHolder -> holder.bind(item as ScheduleAdapterItem.Headline)
            is HeadlinePaddingViewHolder -> Unit
        }
    }

    fun setSelectionEnabled(isEnabled: Boolean) {
        selectionEnabled.value = isEnabled
    }

    fun setSelected(scheduleId: ScheduleId, selected: Boolean) {
        selectedScheduleIds.update { old ->
            if (selected) old + scheduleId
            else old - scheduleId
        }
    }

    fun submitMoving(fromPosition: Int, toPosition: Int): Boolean {
        return differ.tryMove(fromPosition, toPosition)
    }

    fun submitMoveDone() {
        differ.syncMoving(commitCallback = {})
    }

    fun submitList(items: List<ScheduleAdapterItem>?) {
        differ.submitList(items, commitCallback = {})
    }

    fun getCurrentList(): List<ScheduleAdapterItem> {
        return differ.getCurrentList()
    }

    fun getCurrentSelected(): Set<ScheduleId> {
        return selectedScheduleIds.value
    }
}

@Suppress("FunctionName")
private fun <T> MutableEventSharedFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    extraBufferCapacity = 128,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)