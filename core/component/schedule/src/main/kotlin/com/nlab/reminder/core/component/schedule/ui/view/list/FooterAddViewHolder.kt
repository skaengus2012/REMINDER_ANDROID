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
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.bindText
import com.nlab.reminder.core.android.widget.textChanges
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemAddBinding
import com.nlab.reminder.core.kotlinx.coroutine.flow.withPrev
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
class FooterAddViewHolder internal constructor(
    private val binding: LayoutScheduleAdapterItemAddBinding,
    onSimpleAddDone: (SimpleAdd) -> Unit,
    onFocusChanged: (RecyclerView.ViewHolder, Boolean) -> Unit,
    onBottomPaddingVisible: (Boolean) -> Unit,
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
            val noteFocusedFlow = MutableSharedFlow<Boolean>(replay = 1)
            val editFocusedFlow = MutableSharedFlow<Boolean>(replay = 1)
            jobs += viewLifecycleCoroutineScope.launch {
                binding.edittextNote
                    .focusState()
                    .collect(noteFocusedFlow::emit)
            }
            jobs += viewLifecycleCoroutineScope.launch {
                combine(
                    binding.edittextTitle.focusState(),
                    noteFocusedFlow,
                    transform = { titleFocus, noteFocus -> titleFocus || noteFocus }
                ).distinctUntilChanged().collect { focused ->
                    onFocusChanged(this@FooterAddViewHolder, focused)
                    editFocusedFlow.emit(focused)
                }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                var needDelayOnHidden = false
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
                            if (needDelayOnHidden) {
                                // Focus momentarily lost, fixing blinking symptoms
                                delay(100)
                            } else {
                                needDelayOnHidden = true
                            }
                            false
                        }
                    }
                    .collect { binding.edittextNote.setVisible(it) }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                var needDelayOnHidden = false
                editFocusedFlow
                    .mapLatest { visible ->
                        if (visible) true
                        else {
                            // Focus momentarily lost, fixing blinking symptoms
                            if (needDelayOnHidden) {
                                // Focus momentarily lost, fixing blinking symptoms
                                delay(100)
                            } else {
                                needDelayOnHidden = true
                            }
                            false
                        }
                    }
                    .collectLatest { visible ->
                        binding.viewBottomPadding.apply {
                            setVisible(visible)
                            awaitPost()
                        }
                        onBottomPaddingVisible(visible)
                    }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                editFocusedFlow.collect { binding.buttonInfo.setVisible(it) }
            }
            jobs += viewLifecycleCoroutineScope.launch {
                noteFocusedFlow.collect { focused ->
                    if (focused && binding.edittextTitle.text.isNullOrBlank()) {
                        binding.edittextTitle.setText(StringIds.new_plan)
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
                                title = binding.edittextTitle.text?.toString().let { curTitle ->
                                    if (curTitle.isNullOrBlank()) view.context.getString(StringIds.new_plan)
                                    else curTitle
                                },
                                note = binding.edittextNote.text?.toString().orEmpty()
                            )
                        } else null
                    }
                    .collect { simpleAdd ->
                        onSimpleAddDone(simpleAdd)
                        binding.clearInput()
                    }
            }
        }

        itemView.doOnDetach {
            jobs.forEach { it.cancel() }
        }
    }

    fun bind(item: ScheduleAdapterItem.FooterAdd) {
        newScheduleSource.value = item.newScheduleSource
        binding.clearInput()
        when (item.line) {
            ScheduleAdapterItem.FooterAdd.Line.Type1 -> {
                binding.viewLine1.setVisible(true)
                binding.viewLine2.setVisible(false)
            }

            ScheduleAdapterItem.FooterAdd.Line.Type2 -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(true)
            }

            ScheduleAdapterItem.FooterAdd.Line.None -> {
                binding.viewLine1.setVisible(false)
                binding.viewLine2.setVisible(false)
            }
        }
    }
}

private fun LayoutScheduleAdapterItemAddBinding.clearInput() {
    edittextTitle.apply {
        bindText("")
        clearFocus()
    }
    edittextNote.apply {
        bindText("")
        clearFocus()
    }
}