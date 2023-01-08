/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.common.schedule.view

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nlab.reminder.core.android.recyclerview.DragSnapshot
import com.nlab.reminder.core.android.recyclerview.DraggableAdapter
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * @author thalys
 */
class DefaultScheduleUiStateAdapter(
    diffCallback: DiffUtil.ItemCallback<ScheduleUiState>
) : ListAdapter<ScheduleUiState, ScheduleUiStateViewHolder>(diffCallback),
    DraggableAdapter<ScheduleUiState> {
    private val _itemEvent = MutableSharedFlow<ItemEvent>(extraBufferCapacity = 1)
    private val selectionEnabled = MutableStateFlow(false)
    private val draggableAdapterDelegate = ListItemDraggableAdapterDelegate(
        getSnapshot = this::getCurrentList,
        getItem = this::getItem,
        notifyItemMoved = this::notifyItemMoved
    )

    val itemEvent: SharedFlow<ItemEvent> = _itemEvent.asSharedFlow()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleUiStateViewHolder =
        ScheduleUiStateViewHolder.of(
            parent,
            selectionEnabled = selectionEnabled.asStateFlow(),
            onCompleteClicked = { position -> sendItemEventWithUiState(position, ItemEvent::OnCompleteClicked) },
            onDeleteClicked = { position -> sendItemEventWithUiState(position, ItemEvent::OnDeleteClicked) },
            onLinkClicked = { position -> sendItemEventWithUiState(position, ItemEvent::OnLinkClicked) },
            onSelectTouched = { absolutePosition, curSelected ->
                _itemEvent.tryEmit(ItemEvent.OnSelectTouched(absolutePosition, curSelected))
            },
            onDragHandleClicked = { viewHolder -> _itemEvent.tryEmit(ItemEvent.OnDragHandleClicked(viewHolder)) }
        )

    private inline fun sendItemEventWithUiState(position: Int, getEvent: (ScheduleUiState) -> ItemEvent) {
        getItem(position)
            ?.let(getEvent)
            ?.also(_itemEvent::tryEmit)
    }

    override fun onBindViewHolder(holder: ScheduleUiStateViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onMove(fromPosition: Int, toPosition: Int): Boolean {
        return draggableAdapterDelegate.onMove(fromPosition, toPosition)
    }

    override fun calculateDraggedSnapshot(): DragSnapshot<ScheduleUiState> {
        return draggableAdapterDelegate.calculateDraggedSnapshot()
    }

    override fun adjustRecentSwapPositions() {
        draggableAdapterDelegate.adjustRecentSwapPositions()
    }

    suspend fun setSelectionEnabled(isEnable: Boolean) = withContext(Dispatchers.Main) {
        if (isEnable.not()) {
            // Include a delay period because selection animation conflicts occur.
            delay(100L)
        }
        selectionEnabled.emit(isEnable)
    }

    sealed class ItemEvent private constructor() {
        data class OnCompleteClicked(val uiState: ScheduleUiState) : ItemEvent()
        data class OnDeleteClicked(val uiState: ScheduleUiState) : ItemEvent()
        data class OnLinkClicked(val uiState: ScheduleUiState) : ItemEvent()
        data class OnSelectTouched(val absolutePosition: Int, val curSelected: Boolean) : ItemEvent()
        data class OnDragHandleClicked(val viewHolder: ViewHolder) : ItemEvent()
    }
}