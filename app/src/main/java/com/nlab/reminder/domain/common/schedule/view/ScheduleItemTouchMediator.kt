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

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * @author thalys
 */
class ScheduleItemTouchMediator(
    private val lifecycleOwner: LifecycleOwner,
    private val adapter: RecyclerView.Adapter<*>
) {
    private val _dragEndedFlow = MutableSharedFlow<Unit>()
    private val itemTouchCallback = ScheduleItemTouchHelperCallback(
        onMoveListener = { currentPosition, targetPosition ->
            adapter.notifyItemMoved(currentPosition, targetPosition)
            true
        },
        onClearViewListener = {
            lifecycleOwner.lifecycleScope.launchWhenStarted { _dragEndedFlow.emit(Unit) }
        }
    )

    val dragEndedFlow: SharedFlow<Unit> = _dragEndedFlow.asSharedFlow()

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recyclerView)
    }
}