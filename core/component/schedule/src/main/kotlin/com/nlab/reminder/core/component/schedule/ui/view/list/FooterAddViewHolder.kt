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
import com.nlab.reminder.core.android.view.focusState
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.textChanges
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemAddBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
class FooterAddViewHolder internal constructor(
    private val binding: LayoutScheduleAdapterItemAddBinding,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onEditFocused: (Boolean) -> Unit,
    theme: ScheduleListTheme,
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val newScheduleSource = MutableStateFlow<Any?>(null) // TODO implements

    init {
        binding.buttonInfo.apply {
            imageTintList = ColorStateList.valueOf(theme.getButtonInfoColor(context))
        }

        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val jobs = mutableListOf<Job>()
        itemView.doOnAttach { view ->
            val viewLifecycleOwner = view.findViewTreeLifecycleOwner() ?: return@doOnAttach
            val viewLifecycleCoroutineScope = viewLifecycleOwner.lifecycleScope
            val editFocusedFlow = MutableStateFlow(binding.editableViews().any { it.isFocused })
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    binding.editableViews().map { it.focusState() },
                    transform = { focusedStates -> focusedStates.any { it } }
                ).distinctUntilChanged().collect { focused ->
                    onEditFocused(focused)
                    editFocusedFlow.value = focused
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    binding.edittextNote.run {
                        textChanges()
                            .onStart { emit(text) }
                            .map { it.isNullOrEmpty() }
                            .distinctUntilChanged()
                    },
                    editFocusedFlow,
                    transform = { isCurrentNoteEmpty, focused -> isCurrentNoteEmpty.not() || focused }
                ).distinctUntilChanged()
                    .mapLatest { visible ->
                        if (visible) true
                        else {
                            // Focus momentarily lost, fixing blinking symptoms
                            delay(100)
                            false
                        }
                    }
                    .collect { binding.edittextNote.setVisible(it) }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                editFocusedFlow.collect { focused ->
                    onEditFocused(focused)
                    binding.buttonInfo.setVisible(focused)
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                /**
                editFocusedFlow
                    .mapLatest { focused ->
                        if (focused) true
                        else {
                            delay(100)
                            false
                        }
                    }
                    .distinctUntilChanged()
                    .filter { it.not() }
                    .collect { itemView.hideSoftInputFromWindow() }*/
            }
            jobs += viewLifecycleCoroutineScope.launch {
              /**
                allEditNotFocusedFlow
                    .withPrev(initial = false)
                    .distinctUntilChanged()
                    .filter { (old, new) -> old && new.not() }
                    .mapNotNull {
                        SimpleAdd(
                            headerKey = null, // TODO implements header key
                            binding.edittextTitle.text?.toString().orEmpty(),
                            binding.edittextNote.text?.toString().orEmpty()
                        )
                    }
                    .collect { onSimpleAddDone(it) }*/
            }
        }

        itemView.doOnDetach {
            jobs.forEach { it.cancel() }
        }
    }

    fun bind(item: ScheduleAdapterItem.FooterAdd) {
        newScheduleSource.value = item.newScheduleSource
        binding.editableViews().forEach {
            it.setText("")
            it.clearFocus()
        }
    }
}