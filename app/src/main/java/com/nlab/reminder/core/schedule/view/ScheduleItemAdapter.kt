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

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.core.schedule.model.ScheduleItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * @author Doohyun
 */
class ScheduleItemAdapter(
    diffCallback: DiffUtil.ItemCallback<ScheduleItem> = ScheduleItemDiffCallback(),
    private val scheduleElementItemMovePolicy: ScheduleElementItemMovePolicy = DefaultScheduleElementItemMovePolicy()
) : ListAdapter<ScheduleItem, ScheduleItemViewHolder>(diffCallback),
    ScheduleItemTouchCallback.ItemMoveListener {
    private val _itemEvent = MutableSharedFlow<ItemEvent>(
        extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val viewTypeAdapterDelegate = ScheduleItemViewTypeDelegate(
        getItem = ::getItem,
        onCompleteClicked = { position, isComplete ->
            _itemEvent.tryEmit(ItemEvent.OnCompleteClicked(position, isComplete))
        }
    )
    private val dragPositionHolder = DragPositionHolder()

    val itemEvent: Flow<ItemEvent> = _itemEvent.asSharedFlow()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleItemViewHolder =
        viewTypeAdapterDelegate.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: ScheduleItemViewHolder, position: Int) =
        viewTypeAdapterDelegate.onBindViewHolder(holder, position)

    override fun getItemViewType(position: Int): Int =
        viewTypeAdapterDelegate.getItemViewType(position)

    override fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean {
        val fromItem = getItem(fromPosition) as? ScheduleElement ?: return false
        val toItem = getItem(toPosition) as? ScheduleElement ?: return false
        val isMovable = scheduleElementItemMovePolicy.isMovableInternal(fromItem, toItem)
        if (isMovable) {
            dragPositionHolder.setPosition(fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }
        return isMovable
    }

    override fun onItemMoveEnded() {
        val (from, to) = dragPositionHolder.snapshot() ?: return
        _itemEvent.tryEmit(ItemEvent.OnItemMoveEnded(from, to))
    }

    /**
    override fun calculateDraggedSnapshot(): DragSnapshot<ScheduleItem> =
        draggableAdapterDelegate.calculateDraggedSnapshot()

    override fun adjustRecentSwapPositions() =
        draggableAdapterDelegate.adjustRecentSwapPositions()

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean =
        draggableAdapterDelegate.onItemMove(fromPosition, toPosition)

    override fun onItemMoveEnded() {

    }*/

    sealed class ItemEvent private constructor() {
        data class OnCompleteClicked(val position: Int, val isComplete: Boolean) : ItemEvent()
        data class OnItemMoveEnded(val fromPosition: Int, val toPosition: Int) : ItemEvent() // todo 구현
    }
}