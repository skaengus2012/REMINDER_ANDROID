/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.kotlin.flow

import com.nlab.reminder.core.util.annotation.test.InlineRequired
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

/**
 * @author Doohyun
 */

/**
 * jacoco cannot recognize this function..
 */
@InlineRequired
@Suppress("NOTHING_TO_INLINE")
inline fun <T, R> Flow<T>.map(noinline transform: (value: T) -> R): Flow<R> = map { transform(it) }

fun <T : Any> Flow<T>.withOld(): Flow<Pair<T?, T>> =
    scan(Pair(null, null)) { acc: Pair<T?, T?>, value -> acc.second to value }
        .filter { (_, new) -> new != null }
        .map { (old, new) -> old to requireNotNull(new) }