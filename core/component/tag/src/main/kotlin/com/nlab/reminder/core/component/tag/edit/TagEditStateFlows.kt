/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.kotlin.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
internal inline fun <reified T : TagEditState> MutableStateFlow<TagEditState?>.updateIfTypeOf(
    block: (T) -> TagEditState
) = update { current ->
    if (current is T) block(current)
    else current
}

internal inline fun <reified T, reified U> MutableStateFlow<TagEditState?>.processingScope(
    request: (emitState: T) -> U,
    onFinished: MutableStateFlow<TagEditState?>.(T, U) -> Result<Unit>
): Result<Unit> where T : TagEditState, T : Processable {
    val emitStep = getAndUpdate { current ->
        if (current is T) TagEditState.Processing(current)
        else current
    }
    return if (emitStep is T) onFinished(emitStep, request(emitStep)) else Result.Success(Unit)
}

internal inline fun <reified T> MutableStateFlow<TagEditState?>.processingScope(
    request: (emitState: T) -> Result<Unit>
): Result<Unit> where T : TagEditState, T : Processable = processingScope(
    request,
    onFinished = { emitState, result ->
        updateIfProcessingStateEquals(target = emitState, to = null)
        result
    }
)

internal fun <T> MutableStateFlow<TagEditState?>.updateIfProcessingStateEquals(
    target: T,
    to: TagEditState?,
) where T : TagEditState, T : Processable = update { current ->
    if (current.isProcessingStateEquals(target)) to
    else current
}

private fun <T> TagEditState?.isProcessingStateEquals(
    target: T
): Boolean where T : TagEditState, T : Processable = this is TagEditState.Processing && state == target