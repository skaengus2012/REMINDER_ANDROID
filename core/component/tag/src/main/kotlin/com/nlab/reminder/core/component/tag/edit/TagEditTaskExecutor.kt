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
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.errorMessage
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.onSuccess
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author Thalys
 */
@Reusable
class TagEditTaskExecutor @Inject constructor(
    private val userMessageFactory: UserMessageFactory
) {
    fun process(task: TagEditTask, current: TagEditState): Flow<TagEditStateTransition> = flow {
        emit(TagEditStateTransition(current, task.nextState))
        task.processAndGet()
            .onSuccess { emit(TagEditStateTransition(task.nextState, it)) }
            .onFailure { errorMessage(userMessageFactory.createExceptionSource(), it) }
    }
}

@ExcludeFromGeneratedTestReport
data class TagEditStateTransition(
    val previousState: TagEditState,
    val updatedState: TagEditState
)