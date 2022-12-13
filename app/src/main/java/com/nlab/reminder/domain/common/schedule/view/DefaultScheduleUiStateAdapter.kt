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
import androidx.recyclerview.widget.ListAdapter
import com.nlab.reminder.core.android.recyclerview.DragSnapshot
import com.nlab.reminder.core.android.recyclerview.DraggableAdapter
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author thalys
 */
class DefaultScheduleUiStateAdapter(
    private val onCompleteClicked: (ScheduleUiState) -> Unit,
    private val onDeleteClicked: (ScheduleUiState) -> Unit,
    private val onLinkClicked: (ScheduleUiState) -> Unit
) : ListAdapter<ScheduleUiState, ScheduleUiStateViewHolder>(ScheduleUiStateDiffCallback()),
    DraggableAdapter<ScheduleUiState> {
    private val selectionEnabledFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val draggableAdapterDelegate = ListItemDraggableAdapterDelegate(
        getSnapshot = this::getCurrentList,
        getItem = this::getItem,
        notifyItemMoved = this::notifyItemMoved
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleUiStateViewHolder =
        ScheduleUiStateViewHolder.of(
            parent,
            selectionEnabledFlow.asStateFlow(),
            onCompleteClicked = { position -> getItem(position)?.also(onCompleteClicked) },
            onDeleteClicked = { position -> getItem(position)?.also(onDeleteClicked) },
            onLinkClicked = { position -> getItem(position)?.also(onLinkClicked) }
        )

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

    fun setSelectionEnabled(isEnable: Boolean) {
        selectionEnabledFlow.tryEmit(isEnable)
    }
}
