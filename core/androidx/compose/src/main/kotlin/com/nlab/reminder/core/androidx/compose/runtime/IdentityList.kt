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

package com.nlab.reminder.core.androidx.compose.runtime

import androidx.compose.runtime.Immutable
import com.nlab.reminder.core.kotlin.identityHashCodeOf

/**
 * @author Doohyun
 */
@Immutable
class IdentityList<out E> internal constructor(val value: List<E>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityList<E>) return false

        // Using identity comparison for the 'elements' list optimizes recomposition in Jetpack Compose.
        // It ensures that recomposition is triggered only when a new list instance is provided,
        // not just when its content changes.
        return value === other.value
    }

    override fun hashCode(): Int {
        return identityHashCodeOf(value)
    }
}

fun <T> IdentityList(): IdentityList<T> = IdentityList(emptyList())

fun <T> List<T>.toIdentityList(): IdentityList<T> = IdentityList(value = this)