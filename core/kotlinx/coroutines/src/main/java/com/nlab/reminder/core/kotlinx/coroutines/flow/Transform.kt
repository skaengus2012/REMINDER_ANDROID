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

package com.nlab.reminder.core.kotlinx.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter as kotlinxCoroutineFilter
import kotlinx.coroutines.flow.map as kotlinxCoroutineMap

/**
 * There exists coverage that Jacoco does not recognize for Coroutine functions.
 * Therefore, We create simple lambda functions to replace them.
 *
 * @author thalys
 */
fun <T> Flow<T>.filter(predicate: (T) -> Boolean): Flow<T> = kotlinxCoroutineFilter(predicate)

fun <T, R> Flow<T>.map(transform: (value: T) -> R): Flow<R> = kotlinxCoroutineMap(transform)
