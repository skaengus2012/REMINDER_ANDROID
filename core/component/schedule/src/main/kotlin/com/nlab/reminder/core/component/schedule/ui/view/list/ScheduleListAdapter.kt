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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlineBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemHeadlinePaddingBinding
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

private const val ITEM_VIEW_TYPE_HEADLINE = 1
private const val ITEM_VIEW_TYPE_HEADLINE_PADDING = 2
private const val ITEM_VIEW_TYPE_CONTENT = 3

/**
 * @author Thalys
 */
class ScheduleListAdapter(
    private val theme: ScheduleListTheme
) : ListAdapter<ScheduleAdapterItem, ScheduleAdapterItemViewHolder>(ScheduleAdapterItemDiffCallback()) {
    private val selectionEnabled = MutableStateFlow(false)

    private val _editRequest = MutableEventSharedFlow<SimpleEdit>()
    val editRequest: Flow<SimpleEdit> = _editRequest.distinctUntilChanged()

    private val _dragHandleTouch = MutableEventSharedFlow<RecyclerView.ViewHolder>()
    val dragHandleTouch: Flow<RecyclerView.ViewHolder> = _dragHandleTouch.conflate()

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ScheduleAdapterItem.Headline -> ITEM_VIEW_TYPE_HEADLINE
        is ScheduleAdapterItem.HeadlinePadding -> ITEM_VIEW_TYPE_HEADLINE_PADDING
        is ScheduleAdapterItem.Content -> ITEM_VIEW_TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapterItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADLINE -> {
                ScheduleHeadlineViewHolder(
                    binding = LayoutScheduleAdapterItemHeadlineBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    theme = theme
                )
            }

            ITEM_VIEW_TYPE_HEADLINE_PADDING -> {
                ScheduleHeadlinePaddingViewHolder(
                    binding = LayoutScheduleAdapterItemHeadlinePaddingBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                )
            }

            ITEM_VIEW_TYPE_CONTENT -> {
                ScheduleContentViewHolder(
                    binding = LayoutScheduleAdapterItemContentBinding.inflate(
                        layoutInflater,
                        parent,
                        /* attachToParent = */ false
                    ),
                    selectionEnabled = selectionEnabled,
                    onSimpleEditDone = { _editRequest.tryEmit(it) },
                    onDragHandleTouched = { _dragHandleTouch.tryEmit(it) },
                    theme = theme,
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
            is ScheduleHeadlineViewHolder -> holder.bind(item as ScheduleAdapterItem.Headline)
            is ScheduleContentViewHolder -> holder.bind(item as ScheduleAdapterItem.Content)
            is ScheduleHeadlinePaddingViewHolder -> Unit
        }
    }

    fun setSelectionEnabled(isEnabled: Boolean) {
        selectionEnabled.value = isEnabled
    }
}

@Suppress("FunctionName")
private fun <T> MutableEventSharedFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    extraBufferCapacity = 128,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)