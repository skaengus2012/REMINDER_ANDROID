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

import androidx.recyclerview.widget.DiffUtil

/**
 * @author Thalys
 */
internal class ScheduleAdapterItemDiffCallback : DiffUtil.ItemCallback<ScheduleAdapterItem>() {
    override fun areItemsTheSame(oldItem: ScheduleAdapterItem, newItem: ScheduleAdapterItem): Boolean = when {
        oldItem is ScheduleAdapterItem.Content && newItem is ScheduleAdapterItem.Content -> {
            oldItem.scheduleDetail.schedule.id == newItem.scheduleDetail.schedule.id
        }

        else -> oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ScheduleAdapterItem, newItem: ScheduleAdapterItem): Boolean =
        oldItem == newItem
}