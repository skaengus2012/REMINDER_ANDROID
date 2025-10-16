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

package com.nlab.reminder.core.component.schedulelist.internal.ui

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemAddBinding
import com.nlab.reminder.core.kotlinx.coroutines.cancelAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
class AddViewHolder internal constructor(
    binding: LayoutScheduleAdapterItemAddBinding,
    theme: ScheduleListTheme,
    onItemViewTouched: (RecyclerView.ViewHolder) -> Unit,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
) : ScheduleAdapterItemViewHolder(binding.root),
    MovableViewHolder {
    private val addViewHolderDelegate = AddViewHolderDelegate(binding)

    init {
        addViewHolderDelegate.init(theme)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleScope = view.findViewTreeLifecycleOwner()
                ?.lifecycleScope
                ?: return@doOnAttach
            val inputFocusFlow = binding.addInputFocusSharedFlow(viewLifecycleScope, jobs)
            val hasInputFocusFlow = inputFocusFlow
                .map { it != AddInputFocus.Nothing }
                .distinctUntilChanged()
                .shareInWithJobCollector(viewLifecycleScope, jobs, replay = 1)
            jobs += addViewHolderDelegate.onAttached(
                addInputFocusFlow = inputFocusFlow,
                hasInputFocusFlow = hasInputFocusFlow,
                onSimpleAddDone = onSimpleAddDone,
                onItemViewTouched = { onItemViewTouched(this) }
            )
            jobs += viewLifecycleScope.launch {
                hasInputFocusFlow.collect { focused -> onFocusChanged(this@AddViewHolder, focused) }
            }
        }
        itemView.doOnDetach {
            jobs.cancelAll()
        }
    }

    fun bind(item: ScheduleAdapterItem.Add) {
        addViewHolderDelegate.bind(
            newScheduleSource = item.newScheduleSource,
            line = item.line
        )
    }
}