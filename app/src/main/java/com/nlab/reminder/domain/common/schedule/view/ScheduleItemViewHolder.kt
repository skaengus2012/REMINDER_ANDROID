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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.ViewItemScheduleBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class ScheduleItemViewHolder(
    private val binding: ViewItemScheduleBinding
) : RecyclerView.ViewHolder(binding.root) {
    private var onCompleteToggleClicked: () -> Unit = {}

    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .onEach { onCompleteToggleClicked() }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    fun onBind(scheduleItem: ScheduleItem) {
        onCompleteToggleClicked = scheduleItem.onCompleteToggleClicked

        binding.textviewTitle.text = scheduleItem.title
        binding.textviewNote.text = scheduleItem.note
        binding.buttonComplete.isSelected = scheduleItem.isComplete
    }

    companion object {
        fun of(parent: ViewGroup): ScheduleItemViewHolder = ScheduleItemViewHolder(
            ViewItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}