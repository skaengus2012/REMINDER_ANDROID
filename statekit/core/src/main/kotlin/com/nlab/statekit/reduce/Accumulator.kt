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

package com.nlab.statekit.reduce

/**
 * @author Doohyun
 */
class Accumulator<T : Any> internal constructor() {
    private val acc = ArrayDeque<Any>()
    internal var isReady: Boolean = false
        private set

    internal fun ready() {
        isReady = true
    }

    internal fun release() {
        isReady = false
        acc.clear()
    }

    fun add(value: T) {
        acc.add(value)
    }

    @Suppress("UNCHECKED_CAST")
    fun removeLastOrNull(): T? = acc.removeLastOrNull() as? T

    @Suppress("UNCHECKED_CAST")
    fun removeLast(): T = acc.removeLast() as T
}