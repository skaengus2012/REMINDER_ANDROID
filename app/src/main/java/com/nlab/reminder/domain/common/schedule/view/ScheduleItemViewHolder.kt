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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.ViewItemScheduleBinding
import com.nlab.reminder.domain.common.schedule.Schedule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class ScheduleItemViewHolder private constructor(
    lifecycleOwner: LifecycleOwner,
    private val binding: ViewItemScheduleBinding
) : RecyclerView.ViewHolder(binding.root) {
    private var onCompleteToggleClicked: () -> Unit = {}

    init {
        binding.checkboxButton
            .throttleClicks()
            .onEach { onCompleteToggleClicked() }
            .launchIn(lifecycleOwner.lifecycleScope)
    }

    fun onBind(scheduleItem: ScheduleItem) {
        onCompleteToggleClicked = scheduleItem.onCompleteToggleClicked

        binding.titleTextview.text = scheduleItem.title
        binding.noteTextview.text = scheduleItem.note
        binding.checkboxButton.isSelected = scheduleItem.isComplete
    }

    companion object {
        fun create(
            parent: ViewGroup,
            lifecycleOwner: LifecycleOwner
        ): ScheduleItemViewHolder = ScheduleItemViewHolder(
            lifecycleOwner,
            ViewItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}