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
import androidx.paging.PagingDataAdapter
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleUiState

/**
 * @author thalys
 */
class DefaultSchedulePagingAdapter(
    private val emptyUiState: ScheduleUiState = ScheduleUiState(Schedule.empty(), isCompleteMarked = false),
    private val onCompleteClicked: (ScheduleUiState) -> Unit
) : PagingDataAdapter<ScheduleUiState, ScheduleUiStateViewHolder>(ScheduleUiStateDiffCallback()) {
    override fun onBindViewHolder(holder: ScheduleUiStateViewHolder, position: Int) {
        holder.onBind(getItem(position) ?: emptyUiState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleUiStateViewHolder {
        return ScheduleUiStateViewHolder.of(
            parent,
            onCompleteClicked = { position -> getItem(position)?.also(onCompleteClicked) }
        )
    }
}