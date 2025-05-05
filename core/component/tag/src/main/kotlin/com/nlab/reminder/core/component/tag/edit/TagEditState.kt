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

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.kotlin.NonNegativeInt

/**
 * @author Doohyun
 */
sealed class TagEditState private constructor() {
    @ExcludeFromGeneratedTestReport
    data object None : TagEditState()

    @ExcludeFromGeneratedTestReport
    data class AwaitTaskSelection(val tag: Tag) : TagEditState(), Processable

    @ExcludeFromGeneratedTestReport
    data class Rename(
        val tag: Tag,
        val usageCount: NonNegativeInt,
        val renameText: String,
        val shouldUserInputReady: Boolean,
    ) : TagEditState(), Processable

    @ExcludeFromGeneratedTestReport
    data class Merge(
        val from: Tag,
        val fromUsageCount: NonNegativeInt,
        val to: Tag,
    ) : TagEditState(), Processable

    @ExcludeFromGeneratedTestReport
    data class Delete(
        val tag: Tag,
        val usageCount: NonNegativeInt,
    ) : TagEditState(), Processable

    @ExcludeFromGeneratedTestReport
    class Processing private constructor(val state: TagEditState) : TagEditState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Processing

            return state == other.state
        }

        override fun hashCode(): Int {
            return state.hashCode()
        }

        companion object {
            operator fun <T> invoke(
                state: T
            ): Processing where T : TagEditState, T : Processable = Processing(state)
        }
    }
}