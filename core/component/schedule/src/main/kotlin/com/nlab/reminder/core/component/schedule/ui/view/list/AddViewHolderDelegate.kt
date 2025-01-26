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
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nlab.reminder.core.android.view.clearFocusIfNeeded
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.bindCursorVisible
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemAddBinding
import com.nlab.reminder.core.kotlinx.coroutine.flow.withPrev
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal class AddViewHolderDelegate(
    private val binding: LayoutScheduleAdapterItemAddBinding
) {
    private val bindingNewScheduleSource = MutableStateFlow<Any?>(null) // TODO implements

    fun init(theme: ScheduleListTheme) {
        binding.buttonInfo.apply {
            imageTintList = ColorStateList.valueOf(theme.getButtonInfoColor(context))
        }
        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    fun onAttached(
        view: View,
        addInputFocusFlow: SharedFlow<AddInputFocus>,
        hasInputFocusFlow: SharedFlow<Boolean>,
        onSimpleAddDone: (SimpleAdd) -> Unit
    ): List<Job> {
        val lifecycleScope = view.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?: return emptyList()
        val jobs = mutableListOf<Job>()
        jobs += lifecycleScope.launch {
            addInputFocusFlow.collect { inputFocus ->
                when (inputFocus) {
                    AddInputFocus.Title -> with(binding) {
                        edittextTitle.bindCursorVisible(true)
                        edittextNote.bindCursorVisible(false)
                    }
                    AddInputFocus.Note -> with(binding) {
                        edittextTitle.bindCursorVisible(false)
                        edittextNote.bindCursorVisible(true)
                    }
                    AddInputFocus.Nothing -> with(binding) {
                        edittextTitle.bindCursorVisible(false)
                        edittextNote.bindCursorVisible(false)
                    }
                }
            }
        }

        jobs += lifecycleScope.launch {
            hasInputFocusFlow.collect(binding.buttonInfo::setVisible)
        }
        jobs += lifecycleScope.launch {
            registerEditNoteVisibility(
                edittextNote = binding.edittextNote,
                viewHolderEditFocusedFlow = hasInputFocusFlow
            )
        }
        jobs += lifecycleScope.launch {
            addInputFocusFlow
                .map { it == AddInputFocus.Note }
                .distinctUntilChanged()
                .collect { focused ->
                    if (focused && binding.edittextTitle.text.isNullOrBlank()) {
                        binding.edittextTitle.setText(StringIds.new_plan)
                    }
                }
        }
        jobs += lifecycleScope.launch {
            hasInputFocusFlow
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
                            title = binding.edittextTitle.text?.toString().let { curTitle ->
                                if (curTitle.isNullOrBlank()) view.context.getString(StringIds.new_plan)
                                else curTitle
                            },
                            note = binding.edittextNote.text?.toString().orEmpty()
                        )
                    } else null
                }
                .collect { simpleAdd -> binding.clearInput(); onSimpleAddDone(simpleAdd) }
        }
        return jobs
    }

    fun bind(
        newScheduleSource: Any?,
        line: AddLine
    ) {
        binding.clearInput()
        bindingNewScheduleSource.value = newScheduleSource
        when (line) {
            AddLine.Type1 -> {
                binding.viewLine1.setVisible(true)
                binding.viewLine2.setVisible(false)
            }

            AddLine.Type2 -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(true)
            }

            AddLine.None -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(false)
            }
        }
    }
}

private fun LayoutScheduleAdapterItemAddBinding.clearInput() {
    edittextTitle.apply { bindText(""); clearFocusIfNeeded() }
    edittextNote.apply { bindText(""); clearFocusIfNeeded() }
}