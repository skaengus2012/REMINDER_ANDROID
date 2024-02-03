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

package com.nlab.reminder.core.schedule.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.nlab.reminder.R
import com.nlab.reminder.core.schedule.model.ScheduleElement
import com.nlab.reminder.core.schedule.model.ScheduleItem
import com.nlab.reminder.databinding.ViewItemScheduleElementBinding
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
internal class ScheduleItemViewTypeDelegate(
    private val getItem: (position: Int) -> ScheduleItem,
    private val selectionEnabled: Flow<Boolean>,
    private val eventListener: ScheduleElementItemEventListener
) {
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.view_item_schedule_element -> ScheduleElementViewHolder(
                ViewItemScheduleElementBinding.inflate(layoutInflater, parent, /* attachToParent=*/ false),
                selectionEnabled,
                eventListener
            )

            else -> throw IllegalArgumentException("unsupported viewType inputted!!")
        }
    }

    fun onBindViewHolder(holder: ScheduleItemViewHolder, position: Int) = when (holder) {
        is ScheduleElementViewHolder -> holder.onBind(getItem(position) as ScheduleElement)
    }

    fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ScheduleElement -> R.layout.view_item_schedule_element
    }
}
