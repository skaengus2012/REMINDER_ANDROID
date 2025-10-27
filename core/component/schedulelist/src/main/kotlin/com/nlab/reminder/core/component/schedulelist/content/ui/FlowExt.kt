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
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.widget.textChanges
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
private const val FOCUS_LOST_CONFIRMATION_DURATION = 100L

internal suspend inline fun Flow<Boolean>.collectWithHiddenDebounce(
    crossinline collect: suspend (Boolean) -> Unit
) {
    var needDelayOnHidden = false
    collectLatest { visible ->
        if (visible) collect(true)
        else {
            if (needDelayOnHidden) {
                // Focus momentarily lost, fixing blinking symptoms
                delay(FOCUS_LOST_CONFIRMATION_DURATION)
            } else {
                needDelayOnHidden = true
            }
            collect(false)
        }
    }
}

internal fun Flow<Boolean>.focusLostCompletelyChanges(): Flow<Boolean> =
    withPrev(initial = false).distinctUntilChanged().mapLatest { (old, new) ->
        if (old && new.not()) {
            delay(FOCUS_LOST_CONFIRMATION_DURATION)
            true
        } else false
    }

internal suspend inline fun registerEditNoteVisibility(
    edittextNote: EditText,
    viewHolderEditFocusedFlow: Flow<Boolean>
) {
    combine(
        edittextNote.run {
            textChanges()
                .onStart { emit(text) }
                .map { it.isNullOrEmpty() }
                .distinctUntilChanged()
        },
        viewHolderEditFocusedFlow,
        transform = { isCurrentNoteEmpty, focused -> isCurrentNoteEmpty.not() || focused }
    ).distinctUntilChanged().collectWithHiddenDebounce(edittextNote::setVisible)
}

internal fun <T> Flow<T>.shareInWithJobCollector(
    scope: CoroutineScope,
    jobCollector: MutableCollection<Job>,
    replay: Int,
): SharedFlow<T> {
    val sharedFlow = MutableSharedFlow<T>(replay = replay)
    jobCollector += scope.launch {
        collect { value -> sharedFlow.emit(value) }
    }

    return sharedFlow
}