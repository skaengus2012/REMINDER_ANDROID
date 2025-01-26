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

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.awaitPost
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemFooterAddBinding
import com.nlab.reminder.core.kotlinx.coroutine.cancelAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
class FooterAddViewHolder internal constructor(
    private val binding: LayoutScheduleAdapterItemFooterAddBinding,
    theme: ScheduleListTheme,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
    onBottomPaddingVisible: (Boolean) -> Unit,
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val addViewHolderDelegate = AddViewHolderDelegate(binding = binding.layoutAdd)

    init {
        addViewHolderDelegate.init(theme)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val lifecycleScope = view.findViewTreeLifecycleOwner()
                ?.lifecycleScope
                ?: return@doOnAttach
            val inputFocusFlow = binding.layoutAdd.addInputFocusSharedFlow(lifecycleScope)
            val hasInputFocusFlow = inputFocusFlow
                .map { it != AddInputFocus.Nothing }
                .distinctUntilChanged()
                .shareIn(scope = lifecycleScope, started = SharingStarted.WhileSubscribed(), replay = 1)
            jobs += addViewHolderDelegate.onAttached(view, inputFocusFlow, hasInputFocusFlow, onSimpleAddDone)
            jobs += lifecycleScope.launch {
                hasInputFocusFlow.collect { focused -> onFocusChanged(this@FooterAddViewHolder, focused) }
            }
            jobs += lifecycleScope.launch {
                hasInputFocusFlow.collectWithHiddenDebounce { visible ->
                    binding.viewBottomPadding.apply { setVisible(visible); awaitPost() }
                    onBottomPaddingVisible(visible)
                }
            }
        }
        itemView.doOnDetach {
            jobs.cancelAll()
        }
    }

    fun bind(item: ScheduleAdapterItem.FooterAdd) {
        addViewHolderDelegate.bind(
            newScheduleSource = item.newScheduleSource,
            line = item.line
        )
    }
}