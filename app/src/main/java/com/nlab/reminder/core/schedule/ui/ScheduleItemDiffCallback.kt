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

import androidx.recyclerview.widget.DiffUtil
/**
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.core.schedule.model.ScheduleItem

/**
 * @author Doohyun
 */
internal class ScheduleItemDiffCallback : DiffUtil.ItemCallback<ScheduleItem>() {
    private var isDragCompare: Boolean = false

    override fun areItemsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean =
        if (oldItem::class != newItem::class) false
        else when (oldItem) {
            // add another ScheduleItem type
            is ScheduleElement -> {
                newItem as ScheduleElement
                if (isDragCompare) {
                    // When updating by Drag, fade animation is prevented from occurring unnecessarily.
                    oldItem.id == newItem.id
                } else {
                    // When performing the bulk select function, it prevents the scroll from going to the end.
                    //
                    // Ex) If you can see the completed schedule on the Full Schedule screen,
                    // the scroll will be moved to the end when you perform a full completion.
                    oldItem.schedule == newItem.schedule
                }
            }
        }

    override fun areContentsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean {
        return oldItem == newItem
    }

    fun onUserDragEnded() {
        isDragCompare = true
    }

    fun onItemUpdated() {
        isDragCompare = false
    }
}*/