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

package com.nlab.reminder.domain.feature.schedule.all.view

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import com.nlab.reminder.domain.common.schedule.view.ScheduleItem
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemDiffCallback
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemViewHolder

/**
 * @author Doohyun
 */
class DoneSchedulePagingDataAdapter(
    private val lifecycleOwner: LifecycleOwner
) : PagingDataAdapter<ScheduleItem, ScheduleItemViewHolder>(ScheduleItemDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleItemViewHolder {
        return ScheduleItemViewHolder.create(parent, lifecycleOwner)
    }

    override fun onBindViewHolder(holder: ScheduleItemViewHolder, position: Int) {
        holder.onBind(checkNotNull(getItem(position)) { "Adapter not support placeHolder" })
    }
}