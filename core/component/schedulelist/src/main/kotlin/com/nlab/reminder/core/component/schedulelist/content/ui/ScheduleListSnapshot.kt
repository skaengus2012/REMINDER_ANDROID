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

import androidx.compose.runtime.Immutable
import com.nlab.reminder.core.component.schedulelist.content.ScheduleListElement
import com.nlab.reminder.core.kotlin.identityHashCodeOf

/**
 * @author Doohyun
 */
@Immutable
class ScheduleListSnapshot<T : ScheduleListElement>(internal val elements: List<T>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScheduleListSnapshot<T>) return false

        // Using identity comparison for the 'elements' list optimizes recomposition in Jetpack Compose.
        // It ensures that recomposition is triggered only when a new list instance is provided,
        // not just when its content changes.
        return elements === other.elements
    }

    override fun hashCode(): Int {
        return identityHashCodeOf(elements)
    }
}