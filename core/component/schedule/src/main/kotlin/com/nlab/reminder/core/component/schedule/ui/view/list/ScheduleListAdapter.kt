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
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlineBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlinePaddingBinding
import com.nlab.reminder.core.data.model.ScheduleId
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

private const val ITEM_VIEW_TYPE_CONTENT = 1
private const val ITEM_VIEW_TYPE_FOOTER_ADD = 2
private const val ITEM_VIEW_TYPE_HEADLINE = 3
private const val ITEM_VIEW_TYPE_HEADLINE_PADDING = 4

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

    private val compoundEditFocusedFlow = MutableStateFlow<Set<StateFlow<Boolean>>>(emptySet())
    val editFocused: Flow<Boolean> = compoundEditFocusedFlow
        .flatMapLatest { childFocusedFlows ->
            combine(childFocusedFlows, transform = { focuses -> focuses.any { it } })
        }
        .distinctUntilChanged()

    private fun getItem(position: Int): ScheduleAdapterItem {
        return differ.getCurrentList()[position]
    }

    override fun getItemCount(): Int = differ.getCurrentList().size

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ScheduleAdapterItem.Content -> ITEM_VIEW_TYPE_CONTENT
        is ScheduleAdapterItem.FooterAdd -> ITEM_VIEW_TYPE_FOOTER_ADD
        is ScheduleAdapterItem.Headline -> ITEM_VIEW_TYPE_HEADLINE
        is ScheduleAdapterItem.HeadlinePadding -> ITEM_VIEW_TYPE_HEADLINE_PADDING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapterItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_CONTENT -> {
                val editFocusedFlow = MutableStateFlow(false)
                    .also { compoundEditFocusedFlow.update { cur -> cur + it } }
                ContentViewHolder(
                    binding = LayoutScheduleAdapterItemContentBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    selectionEnabled = selectionEnabled,
                    selectedScheduleIds = selectedScheduleIds,
                    onSimpleEditDone = { _editRequest.tryEmit(it) },
                    onDragHandleTouched = { _dragHandleTouch.tryEmit(it) },
                    onSelectButtonTouched = { _selectButtonTouch.tryEmit(it) },
                    onEditFocused = { editFocusedFlow.value = it },
                    theme = theme,
                )
            }

            ITEM_VIEW_TYPE_FOOTER_ADD -> {
                val editFocusedFlow = MutableStateFlow(false)
                    .also { compoundEditFocusedFlow.update { cur -> cur + it } }
                FooterAddViewHolder(
                    binding = LayoutScheduleAdapterItemAddBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    onSimpleAddDone = { _addRequest.tryEmit(it) },
                    onEditFocused = { editFocusedFlow.value = it },
                    theme = theme
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