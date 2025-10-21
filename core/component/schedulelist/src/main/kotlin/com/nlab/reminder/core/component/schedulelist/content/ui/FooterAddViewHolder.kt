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

package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemFooterAddBinding
import com.nlab.reminder.core.kotlinx.coroutines.cancelAllAndClear
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal class FooterAddViewHolder(
    private val binding: LayoutScheduleAdapterItemFooterAddBinding,
    themeState: StateFlow<ScheduleListTheme>,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val addViewHolderDelegate = AddViewHolderDelegate(binding = binding.layoutAdd)

    init {
        addViewHolderDelegate.init()

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val lifecycleScope = view.findViewTreeLifecycleOwner()
                ?.lifecycleScope
                ?: return@doOnAttach
            val inputFocusFlow = binding.layoutAdd.addInputFocusSharedFlow(lifecycleScope, jobs)
            val hasInputFocusFlow = inputFocusFlow
                .map { it != AddInputFocus.Nothing }
                .distinctUntilChanged()
                .shareInWithJobCollector(lifecycleScope, jobs, replay = 1)
            jobs += addViewHolderDelegate.onAttached(
                themeState = themeState,
                addInputFocus = inputFocusFlow,
                hasInputFocus = hasInputFocusFlow,
                onSimpleAddDone = onSimpleAddDone
            )
            jobs += lifecycleScope.launch {
                hasInputFocusFlow.collect { focused -> onFocusChanged(this@FooterAddViewHolder, focused) }
            }
            jobs += lifecycleScope.launch {
                hasInputFocusFlow.collectWithHiddenDebounce(binding.viewBottomPadding::setVisible)
            }
        }
        itemView.doOnDetach {
            jobs.cancelAllAndClear()
        }
    }

    fun bind(item: ScheduleListItem.FooterAdd) {
        addViewHolderDelegate.bind(
            newScheduleSource = item.newScheduleSource,
            line = item.line
        )
    }
}