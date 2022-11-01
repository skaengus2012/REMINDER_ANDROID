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

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.recyclerview.ItemMoveListener
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.kotlin.util.getOrNull
import com.nlab.reminder.databinding.ViewItemScheduleBinding

/**
 * @author thalys
 */
class ScheduleItemTouchCallback(
    private val onMoveListener: ItemMoveListener,
    private val onClearViewListener: () -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.ACTION_STATE_IDLE
) {
    override fun isItemViewSwipeEnabled(): Boolean = false
    override fun isLongPressDragEnabled(): Boolean = true

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val binding: ViewItemScheduleBinding =
            catching { ViewItemScheduleBinding.bind(viewHolder.itemView) }.getOrNull() ?: return
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
             //   binding.itemLayout.scaleX = if (isCurrentlyActive) 0.5f else 1f
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = onMoveListener.onMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        onClearViewListener()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }
}