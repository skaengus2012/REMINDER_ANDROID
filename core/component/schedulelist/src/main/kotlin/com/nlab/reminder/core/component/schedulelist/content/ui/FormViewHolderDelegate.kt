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

import android.content.res.ColorStateList
import android.text.InputType
import android.widget.EditText
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.nlab.reminder.core.android.view.clearFocusIfNeeded
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.bindCursorVisible
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemFormBinding
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal class FormViewHolderDelegate(
    private val binding: LayoutScheduleAdapterItemFormBinding
) {
    private val bindingNewScheduleSource = MutableStateFlow<Any?>(null) // TODO implements

    fun init() {
        // Processing for multiline input and actionDone support
        binding.edittextTitle.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    fun onAttached(
        themeState: StateFlow<ScheduleListTheme>,
        formInputFocus: SharedFlow<FormInputFocus>,
        hasInputFocus: SharedFlow<Boolean>,
        onSimpleAddDone: (SimpleAdd) -> Unit,
    ): List<Job> {
        val itemView = binding.root
        val viewLifecycleScope = itemView.findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?: return emptyList()
        val jobs = mutableListOf<Job>()

        jobs += viewLifecycleScope.launch {
            themeState.collect { theme ->
                binding.buttonInfo.imageTintList = ColorStateList.valueOf(
                    /*color = */ theme.getButtonInfoColor(context = itemView.context)
                )
            }
        }
        jobs += viewLifecycleScope.launch {
            formInputFocus.collect { inputFocus ->
                val focusedEditText = binding.findInput(inputFocus)
                binding.getAllInputs().forEach { editText ->
                    editText.bindCursorVisible(isVisible = editText === focusedEditText)
                }
            }
        }
        jobs += viewLifecycleScope.launch {
            hasInputFocus.collect(binding.buttonInfo::setVisible)
        }
        jobs += viewLifecycleScope.launch {
            registerEditNoteVisibility(
                edittextNote = binding.edittextNote,
                viewHolderEditFocusedFlow = hasInputFocus
            )
        }
        jobs += viewLifecycleScope.launch {
            formInputFocus
                .map { it == FormInputFocus.Note }
                .distinctUntilChanged()
                .collect { focused ->
                    if (focused && binding.edittextTitle.text.isNullOrBlank()) {
                        binding.edittextTitle.setText(StringIds.new_plan)
                    }
                }
        }
        jobs += viewLifecycleScope.launch {
            hasInputFocus
                .focusLostCompletelyChanges()
                .mapNotNull { savable ->
                    if (savable.not()) return@mapNotNull null
                    val title = binding.edittextTitle.text
                        ?.toString()
                        .tryToNonBlankStringOrNull()
                        ?: return@mapNotNull null
                    SimpleAdd(
                        headerKey = null, // TODO implements
                        title = title,
                        note = binding.edittextNote.text?.toString().orEmpty()
                    )
                }
                .collect { simpleAdd -> binding.clearInput(); onSimpleAddDone(simpleAdd) }
        }

        return jobs
    }

    fun bind(
        newScheduleSource: Any?,
        formBottomLine: FormBottomLine
    ) {
        binding.clearInput()
        bindingNewScheduleSource.value = newScheduleSource
        when (formBottomLine) {
            FormBottomLine.Type1 -> {
                binding.viewLine1.setVisible(true)
                binding.viewLine2.setVisible(false)
            }

            FormBottomLine.Type2 -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(true)
            }

            FormBottomLine.None -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(false)
            }
        }
    }
}

private fun LayoutScheduleAdapterItemFormBinding.getAllInputs(): Iterable<EditText> = listOf(
    edittextTitle,
    edittextNote
)

private fun LayoutScheduleAdapterItemFormBinding.clearInput() {
    getAllInputs().forEach { editText ->
        editText.bindText(text = "")
        editText.clearFocusIfNeeded()
    }
}