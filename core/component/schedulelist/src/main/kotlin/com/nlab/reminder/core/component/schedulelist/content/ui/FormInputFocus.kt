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

import android.widget.EditText
import com.nlab.reminder.core.android.view.focusChanges
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemFormBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * @author Doohyun
 */
internal enum class FormInputFocus {
    Title, Note, Nothing
}

internal fun LayoutScheduleAdapterItemFormBinding.findInput(formInputFocus: FormInputFocus): EditText? {
    return when (formInputFocus) {
        FormInputFocus.Title -> edittextTitle
        FormInputFocus.Note -> edittextNote
        FormInputFocus.Nothing -> null
    }
}

internal fun LayoutScheduleAdapterItemFormBinding.formInputFocusSharedFlow(
    scope: CoroutineScope,
    jobCollector: MutableCollection<Job>
): SharedFlow<FormInputFocus> {
    return combine(
        FormInputFocus.entries.mapNotNull { addInputFocus ->
            findInput(addInputFocus)
                ?.focusChanges(emitCurrent = true)
                ?.distinctUntilChanged()
                ?.map { hasFocus -> if (hasFocus) addInputFocus else null }
        }
    ) { focuses -> focuses.find { it != null } ?: FormInputFocus.Nothing }
        .distinctUntilChanged()
        .shareInWithJobCollector(scope, jobCollector, replay = 1)
}