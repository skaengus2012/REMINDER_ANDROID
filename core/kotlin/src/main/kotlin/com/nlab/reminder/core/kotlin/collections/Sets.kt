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

/**
 * @author Thalys
 */
inline fun <T, U> Iterable<T>.toSet(transform: (T) -> U): Set<U> =
    buildSet { mapTo(destination = this, transform = transform) }

inline fun <T, U : Any> Iterable<T>.toSetNotNull(transform: (T) -> U?): Set<U> =
    buildSet { mapNotNullTo(destination = this, transform = transform) }

fun <T, U> Array<T>.toSet(transform: (T) -> U): Set<U> =
    buildSet { mapTo(destination = this, transform = transform) }