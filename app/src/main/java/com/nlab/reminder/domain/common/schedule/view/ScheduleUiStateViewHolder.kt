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
import com.nlab.reminder.core.android.recyclerview.bindingAdapterOptionalPosition
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.ViewItemScheduleBinding
import com.nlab.reminder.domain.common.schedule.ScheduleUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author thalys
 */
class ScheduleUiStateViewHolder(
    private val binding: ViewItemScheduleBinding,
    onCompleteClicked: (position: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.initWithLifecycleOwner { lifecycleOwner ->
            buttonComplete
                .throttleClicks()
                .onEach { onCompleteClicked(bindingAdapterOptionalPosition ?: return@onEach) }
                .launchIn(lifecycleOwner.lifecycleScope)
        }
    }

    fun onBind(scheduleUiState: ScheduleUiState) {
        binding.textviewTitle.text = scheduleUiState.schedule.title // TODO low of demeter.
        binding.textviewNote.text = scheduleUiState.schedule.note
        binding.buttonComplete.isSelected = scheduleUiState.isCompleteMarked
    }

    companion object {
        fun of(
            parent: ViewGroup,
            onCompleteClicked: (position: Int) -> Unit
        ): ScheduleUiStateViewHolder = ScheduleUiStateViewHolder(
            ViewItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onCompleteClicked
        )
    }
}