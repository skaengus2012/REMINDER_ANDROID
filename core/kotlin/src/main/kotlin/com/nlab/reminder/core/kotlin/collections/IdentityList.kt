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

import com.nlab.reminder.core.kotlin.identityHashCodeOf

/**
 * @author Doohyun
 */
class IdentityList<out E> internal constructor(val value: List<E>) : List<E> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityList<*>) {
            return if (other is List<*>) value == other else false
        }

        // Using identity comparison for the 'elements' list optimizes recomposition in Jetpack Compose.
        // It ensures that recomposition is triggered only when a new list instance is provided,
        // not just when its content changes.
        return value === other.value
    }

    override fun hashCode(): Int {
        return identityHashCodeOf(value)
    }

    override fun contains(element: @UnsafeVariance E): Boolean {
        return value.contains(element)
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        return value.containsAll(elements)
    }

    override fun get(index: Int): E {
        return value[index]
    }

    override fun indexOf(element: @UnsafeVariance E): Int {
        return value.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return value.isEmpty()
    }

    override fun iterator(): Iterator<E> {
        return value.iterator()
    }

    override fun lastIndexOf(element: @UnsafeVariance E): Int {
        return value.lastIndexOf(element)
    }

    override fun listIterator(): ListIterator<E> {
        return value.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<E> {
        return value.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        return value.subList(fromIndex, toIndex)
    }

    override val size: Int
        get() = value.size
}

fun <T> IdentityList(): IdentityList<T> = IdentityList(emptyList())

fun <T> List<T>.toIdentityList(): IdentityList<T> = IdentityList(value = this)