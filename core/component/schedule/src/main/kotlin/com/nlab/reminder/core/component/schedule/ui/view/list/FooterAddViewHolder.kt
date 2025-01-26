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

import android.content.res.ColorStateList
import android.text.InputType
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.awaitPost
import com.nlab.reminder.core.android.view.focusState
import com.nlab.reminder.core.android.view.clearFocusIfNeeded
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.bindCursorVisible
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemAddBinding
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemFooterAddBinding
import com.nlab.reminder.core.kotlinx.coroutine.flow.withPrev
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
class FooterAddViewHolder internal constructor(
    private val binding: LayoutScheduleAdapterItemFooterAddBinding,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
    onBottomPaddingVisible: (Boolean) -> Unit,
    theme: ScheduleListTheme,
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val newScheduleSource = MutableStateFlow<Any?>(null) // TODO implements

    init {
        binding.layoutAdd.buttonInfo.apply {
            imageTintList = ColorStateList.valueOf(theme.getButtonInfoColor(context))
        }

        // Processing for multiline input and actionDone support
        binding.layoutAdd.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleOwner = view.findViewTreeLifecycleOwner() ?: return@doOnAttach
            val viewLifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope
            val titleFocusedFlow = binding.layoutAdd.edittextTitle
                .focusState(scope = viewLifecycleCoroutineScope, started = SharingStarted.WhileSubscribed())
            val noteFocusedFlow = binding.layoutAdd.edittextNote
                .focusState(scope = viewLifecycleCoroutineScope, started = SharingStarted.WhileSubscribed())
            val editFocusedFlow = combine(
                titleFocusedFlow,
                noteFocusedFlow,
                transform = { titleFocused, notFocused -> titleFocused || notFocused }
            ).distinctUntilChanged()
                .shareIn(scope = viewLifecycleCoroutineScope, started = SharingStarted.WhileSubscribed(), replay = 1)

            jobs += viewLifecycleCoroutineScope.launch {
                titleFocusedFlow.collect { focused ->
                    binding.layoutAdd.edittextTitle.bindCursorVisible(focused)
                    binding.layoutAdd.edittextNote.bindCursorVisible(focused.not())
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                noteFocusedFlow.collect { focused ->
                    binding.layoutAdd.edittextTitle.bindCursorVisible(focused.not())
                    binding.layoutAdd.edittextNote.bindCursorVisible(focused)
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                editFocusedFlow.collect { focused ->
                    onFocusChanged(this@FooterAddViewHolder, focused)
                    binding.layoutAdd.buttonInfo.setVisible(focused)
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                registerEditNoteVisibility(
                    edittextNote = binding.layoutAdd.edittextNote,
                    viewHolderEditFocusedFlow = editFocusedFlow
                )
            }
            jobs += viewLifecycleCoroutineScope.launch {
                editFocusedFlow.collectWithHiddenDebounce { visible ->
                    binding.viewBottomPadding.apply { setVisible(visible); awaitPost() }
                    onBottomPaddingVisible(visible)
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                noteFocusedFlow.collect { focused ->
                    if (focused && binding.layoutAdd.edittextTitle.text.isNullOrBlank()) {
                        binding.layoutAdd.edittextTitle.setText(StringIds.new_plan)
                    }
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                editFocusedFlow
                    .withPrev(initial = false)
                    .distinctUntilChanged()
                    .mapLatest { (old, new) ->
                        if (old && new.not()) {
                            delay(100)
                            true
                        } else false
                    }
                    .mapNotNull { savable ->
                        if (savable) {
                            SimpleAdd(
                                headerKey = null, // TODO implements
                                title = binding.layoutAdd.edittextTitle.text?.toString().let { curTitle ->
                                    if (curTitle.isNullOrBlank()) view.context.getString(StringIds.new_plan)
                                    else curTitle
                                },
                                note = binding.layoutAdd.edittextNote.text?.toString().orEmpty()
                            )
                        } else null
                    }
                    .collect { simpleAdd ->
                        onSimpleAddDone(simpleAdd)
                        binding.layoutAdd.clearInput()
                    }
            }
        }

        itemView.doOnDetach {
            jobs.forEach { it.cancel() }
        }
    }

    fun bind(item: ScheduleAdapterItem.FooterAdd) {
        newScheduleSource.value = item.newScheduleSource
        binding.layoutAdd.clearInput()
        when (item.line) {
            AddLine.Type1 -> {
                binding.layoutAdd.viewLine1.setVisible(true)
                binding.layoutAdd.viewLine2.setVisible(false)
            }

            AddLine.Type2 -> {
                binding.layoutAdd.viewLine1.setVisible(false)
                binding.layoutAdd.viewLine2.setVisible(true)
            }

            AddLine.None -> {
                binding.layoutAdd.viewLine1.setVisible(false)
                binding.layoutAdd.viewLine2.setVisible(false)
            }
        }
    }
}

private fun LayoutScheduleAdapterItemAddBinding.clearInput() {
    edittextTitle.apply {
        bindText("")
        clearFocusIfNeeded()
    }
    edittextNote.apply {
        bindText("")
        clearFocusIfNeeded()
    }
}