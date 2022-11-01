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

import com.nlab.reminder.core.android.recyclerview.DragPositionHolder
import com.nlab.reminder.core.android.recyclerview.DraggableAdapter
import com.nlab.reminder.core.android.recyclerview.impl.DraggableAdapterDelegate
import com.nlab.reminder.core.android.recyclerview.impl.ListItemDragSnapshotCalculator
import com.nlab.reminder.domain.common.schedule.ScheduleUiState

/**
 * @author thalys
 */
@Suppress("FunctionName")
fun ListItemDraggableAdapterDelegate(
    getSnapshot: () -> List<ScheduleUiState>,
    getItem: (position: Int) -> ScheduleUiState,
    notifyItemMoved: (fromPosition: Int, toPosition: Int) -> Unit
): DraggableAdapter<ScheduleUiState> {
    val dragPositionHolder = DragPositionHolder()
    return DraggableAdapterDelegate(
        dragPositionHolder,
        notifyItemMoved,
        ScheduleItemMoveListener(getItem, notifyItemMoved = { fromPosition, toPosition ->
            dragPositionHolder.setPosition(fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }),
        ListItemDragSnapshotCalculator(dragPositionHolder, getSnapshot)
    )
}