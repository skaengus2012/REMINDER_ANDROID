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

import kotlinx.collections.immutable.*

/**
 * @author Thalys
 */
// Cannot make value class
// There is inject problem with hilt.
// issue link : https://github.com/google/dagger/issues/3448#issuecomment-1243857366
class NonEmptySet<out E> internal constructor(val value: Set<E>) {
    init {
        require(value.isNotEmpty()) { "Value should not be empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NonEmptySet<*>

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "NonEmptySet(value=$value)"
    }
}

fun <T> NonEmptySet(
    head: T,
    vararg tails: T
): NonEmptySet<T> = persistentSetOf(head, *tails).toNonEmptySet()

fun <T> Iterable<T>.toNonEmptySet(): NonEmptySet<T> = NonEmptySet(value = toImmutableSet())

fun <T> Iterable<T>.tryToNonEmptySetOrNull(): NonEmptySet<T>? = try { toNonEmptySet() } catch (e: Throwable) { null }