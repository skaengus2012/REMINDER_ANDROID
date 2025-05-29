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

package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.kotlin.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * @author Doohyun
 */
@ExcludeFromGeneratedTestReport
class TagEditTask(
    val current: TagEditState,
    val next: TagEditState,
    val processAndGet: suspend () -> Result<TagEditState>
)

@ExcludeFromGeneratedTestReport
data class TagEditStateTransition(
    val previous: TagEditState,
    val updated: TagEditState
)

internal fun TagEditTask(current: TagEditState): TagEditTask {
    val ret = Result.Success(current)
    val processAndGet = {
        // prevent Missing in the Jacoco report by SUSPEND FUNCTION
        ret
    }
    return TagEditTask(
        current = current,
        next = current,
        processAndGet = processAndGet
    )
}

internal inline fun <reified T> TagEditTask(
    current: TagEditState,
    noinline processAndGet: suspend (T) -> Result<TagEditState>
): TagEditTask where T : TagEditState, T : Processable = TagEditTask(
    targetClazz = T::class,
    current = current,
    processAndGet = processAndGet
)

private fun <T> TagEditTask(
    targetClazz: KClass<T>,
    current: TagEditState,
    processAndGet: suspend (T) -> Result<TagEditState>
): TagEditTask where T : TagEditState, T : Processable {
    val currentAsTarget = targetClazz.safeCast(current)
    return if (currentAsTarget == null) TagEditTask(current) else {
        TagEditTask(
            current = current,
            next = TagEditState.Processing(currentAsTarget),
            processAndGet = { processAndGet(currentAsTarget) }
        )
    }
}

fun TagEditTask.processAsFlow(): Flow<Result<TagEditStateTransition>> = flow {
    if (current != next) {
        emit(Result.Success(TagEditStateTransition(current, next)))
    }
    processAndGet()
        .onSuccess { updated ->
            if (updated != next) {
                emit(Result.Success(TagEditStateTransition(next, updated)))
            }
        }
        .onFailure { t ->
            val errorResult = Result.Failure<TagEditStateTransition>(t)
            emit(errorResult)
        }
}