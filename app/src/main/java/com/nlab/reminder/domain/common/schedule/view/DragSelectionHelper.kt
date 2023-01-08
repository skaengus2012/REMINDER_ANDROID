/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.nlab.reminder.R
import com.nlab.reminder.core.android.content.getDimension
import com.nlab.reminder.domain.common.schedule.ScheduleId

/**
 * @author thalys
 */
class DragSelectionHelper(
    recyclerView: RecyclerView,
    hotspotHeight: Int = recyclerView.context.getDimension(R.dimen.schedule_dragged_hotspot_height).toInt(),
    onSelectChanged: (scheduleId: ScheduleId, isSelected: Boolean) -> Unit
) {
    private val _itemTouchListener = DragSelectTouchListener.create(
        recyclerView.context,
        object : DragSelectReceiver {
            override fun getItemCount(): Int {
                val safeAdapter = checkNotNull(recyclerView.adapter)
                return safeAdapter.itemCount
            }

            override fun isIndexSelectable(index: Int): Boolean =
                recyclerView.findScheduleUiStateViewHolder(index) != null

            override fun isSelected(index: Int): Boolean =
                recyclerView.findScheduleUiStateViewHolder(index)?.isSelected() ?: false

            override fun setSelected(index: Int, selected: Boolean) {
                onSelectChanged(
                    recyclerView.findScheduleUiStateViewHolder(index)?.bindingScheduleId() ?: return,
                    dragSelected ?: return
                )
            }
        },
        config = {
            this.hotspotHeight = hotspotHeight
        }
    )
    private var dragSelected: Boolean? = null

    val itemTouchListener: RecyclerView.OnItemTouchListener get() = _itemTouchListener

    fun enableDragSelection(position: Int, select: Boolean) {
        dragSelected = select
        _itemTouchListener.setIsActive(true, position)
    }

    fun disableDragSelection() {
        _itemTouchListener.setIsActive(false, 0)
    }

    companion object {
        private fun RecyclerView.findScheduleUiStateViewHolder(position: Int): ScheduleUiStateViewHolder? =
            findViewHolderForAdapterPosition(position) as? ScheduleUiStateViewHolder
    }
}