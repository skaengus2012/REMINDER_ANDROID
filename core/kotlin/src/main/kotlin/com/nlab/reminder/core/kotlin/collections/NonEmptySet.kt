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

package com.nlab.reminder.core.kotlin.collections

import kotlinx.collections.immutable.toImmutableSet

/**
 * @author Thalys
 */
@JvmInline
value class NonEmptySet<out E> internal constructor(val value: Set<E>) {
    init {
        require(value.isNotEmpty()) { "Value should not be empty" }
    }
}

fun <T> NonEmptySet(
    head: T,
    vararg tails: T
): NonEmptySet<T> = buildSet { add(head); addAll(tails) }.toNonEmptySet()

fun <T> Iterable<T>.toNonEmptySet(): NonEmptySet<T> = NonEmptySet(value = toImmutableSet())

fun <T> Collection<T>.tryToNonEmptySetOrNull(): NonEmptySet<T>? =
    if (isEmpty()) null
    else toNonEmptySet()