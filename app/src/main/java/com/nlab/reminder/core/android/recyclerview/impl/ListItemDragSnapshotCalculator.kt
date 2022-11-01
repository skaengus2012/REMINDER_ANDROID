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

package com.nlab.reminder.core.android.recyclerview.impl

import com.nlab.reminder.core.android.recyclerview.DragPosition
import com.nlab.reminder.core.android.recyclerview.DragPositionHolder
import com.nlab.reminder.core.android.recyclerview.DragSnapshot
import com.nlab.reminder.core.android.recyclerview.DragSnapshotCalculator

/**
 * @author thalys
 */
class ListItemDragSnapshotCalculator<T : Any>(
    private val dragPositionHolder: DragPositionHolder,
    private val getSnapshot: () -> List<T>
) : DragSnapshotCalculator<T> {
    override fun calculateDraggedSnapshot(): DragSnapshot<T> {
        val snapshot: List<T> =
            getSnapshot().takeUnless { it.isEmpty() } ?: return DragSnapshot.Empty
        val dragPosition: DragPosition.Success =
            (dragPositionHolder.snapshot() as? DragPosition.Success) ?: return DragSnapshot.Empty
        return DragSnapshot.Success(
            items = snapshot.toMutableList().apply { add(dragPosition.to, removeAt(dragPosition.from)) }
        )
    }
}