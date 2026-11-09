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

import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Converts a [TagEditTask] into a flow of [Result] containing [TagEditStateTransition].
 *
 * The returned flow emits state transitions as they occur:
 * - If the current and next states differ, emits a [Result.success] containing the transition from `current` to `next`.
 * - Then performs the processing via [TagEditTask.processAndGet], and:
 *    - If the resulting state is different from `next`, emits another [Result.success] with the transition.
 *    - If an error occurs during processing, emits a [Result.failure] containing the exception.
 *
 * Nothing is executed on invocation; the task is processed only when the returned flow is collected.
 *
 * @author Doohyun
 */
@Reusable
class GetTagEditTaskTransitionFlowUseCase @Inject constructor() {
    /**
     * @return A cold flow that emits zero, one, or two [Result] depending on the state transitions and any errors.
     */
    operator fun invoke(task: TagEditTask): Flow<Result<TagEditStateTransition>> = flow {
        val current = task.current
        val next = task.next
        if (current != next) {
            emit(Result.success(TagEditStateTransition(current, next)))
        }
        task.processAndGet()
            .onSuccess { updated ->
                if (updated != next) {
                    emit(Result.success(TagEditStateTransition(next, updated)))
                }
            }
            .onFailure { t ->
                val errorResult = Result.failure<TagEditStateTransition>(t)
                emit(errorResult)
            }
    }
}
