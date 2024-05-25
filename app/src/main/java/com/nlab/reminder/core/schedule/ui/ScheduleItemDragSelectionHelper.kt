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

package com.nlab.reminder.core.schedule.ui

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.nlab.reminder.R
import com.nlab.reminder.core.android.content.getDimension

/**
 * @author thalys
 */
class ScheduleItemDragSelectionHelper(
    recyclerView: RecyclerView,
    hotspotHeight: Int = recyclerView.context.getDimension(R.dimen.schedule_dragged_hotspot_height).toInt(),
    onSelectChanged: (position: Int, isSelected: Boolean) -> Unit
) {
    private val dragSelectTouchListener = DragSelectTouchListener.create(
        recyclerView.context,
        object : DragSelectReceiver {
            override fun getItemCount(): Int {
                val safeAdapter = checkNotNull(recyclerView.adapter) {
                    "RecyclerView adapter must be set before using this receiver."
                }
                return safeAdapter.itemCount
            }
            override fun isIndexSelectable(index: Int): Boolean =
                recyclerView.findScheduleElementViewHolder(index) != null

            override fun isSelected(index: Int): Boolean =
                recyclerView.findScheduleElementViewHolder(index)
                    ?.binding
                    ?.buttonSelection
                    ?.isSelected
                    ?: false

            override fun setSelected(index: Int, selected: Boolean) {
                val position: Int =
                    recyclerView.findScheduleElementViewHolder(index)
                        ?.bindingAdapterPosition
                        ?: return
                val isSelected: Boolean = dragSelected ?: return
                onSelectChanged(position, isSelected)
            }
        },
        config = {
            this.hotspotHeight = hotspotHeight
        }
    )
    private var dragSelected: Boolean? = null

    val itemTouchListener: RecyclerView.OnItemTouchListener get() = dragSelectTouchListener

    fun enableDragSelection(position: Int, select: Boolean) {
        dragSelected = select
        dragSelectTouchListener.setIsActive(true, position)
    }

    fun disableDragSelection() {
        dragSelectTouchListener.setIsActive(false, 0)
    }
}

private fun RecyclerView.findScheduleElementViewHolder(position: Int): ScheduleElementViewHolder? =
    findViewHolderForAdapterPosition(position) as? ScheduleElementViewHolder