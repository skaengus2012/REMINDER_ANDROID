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

import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.androix.recyclerview.selection.MultiSelectReceiver
import com.nlab.reminder.core.androix.recyclerview.selection.MultiSelectTouchListener
import com.nlab.reminder.core.data.model.ScheduleId
import kotlin.math.max

/**
 * @author Thalys
 */
class ScheduleListSelectionHelper(
    private val selectionSource: ScheduleListSelectionSource,
    private val onSelectedStateChanged: (id: ScheduleId, selected: Boolean) -> Unit
) {
    private var dragSelected: Boolean? = null
    private var attachedListener: MultiSelectTouchListener? = null

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        check(attachedListener == null) { "It's already attached to RecyclerView." }
        val listener = MultiSelectTouchListener.create(
            context = recyclerView.context,
            receiver = object : MultiSelectReceiver {
                override fun setSelected(index: Int, selected: Boolean) {
                    val id = selectionSource.findScheduleId(index) ?: return
                    val dragSelected = dragSelected ?: return
                    onSelectedStateChanged(id, dragSelected)
                }

                override fun isIndexSelectable(index: Int): Boolean {
                    return selectionSource.findScheduleId(index) != null
                }
            },
            config = {
                recyclerView.doOnLayout { v ->
                    hotspotHeight = max(
                        v.height * 0.25f,
                        dpToPx(/*dpValue = */ 120f, v.resources.displayMetrics)
                    ).toInt()
                    autoScrollDelayTimeInMillis = 15L
                }
            }
        )
        recyclerView.addOnItemTouchListener(listener)
        attachedListener = listener
    }

    fun enable(viewHolder: RecyclerView.ViewHolder) {
        val listener = checkNotNull(attachedListener) { "It's not attached to RecyclerView." }
        val scheduleId = selectionSource.findScheduleId(viewHolder.bindingAdapterPosition) ?: return
        dragSelected = selectionSource.findSelected(scheduleId).not()
        listener.setIsActive(
            active = true,
            initialSelectIndex = viewHolder.bindingAdapterPosition
        )
    }

    fun disable() {
        val listener = checkNotNull(attachedListener) { "It's not attached to RecyclerView." }
        listener.setIsActive(active = false, initialSelectIndex = -1)
    }

    fun clearResource() {
        dragSelected = null
        attachedListener?.clearResource()
    }
}