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

package com.nlab.reminder.core.kotlinx.coroutines.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest as kotlinFlatMapLatest
import kotlinx.coroutines.flow.flatMapConcat as kotlinFlatMapConcat

/**
 * @author thalys
 */

/**
 * The flatMapLatest function does not support suspend.
 * If the transform function does not use suspend, there may be cases that Jacoco does not recognize
 */
fun <T, R> Flow<T>.flatMapLatest(transform: (value: T) -> Flow<R>): Flow<R> =
    kotlinFlatMapLatest(transform)

/**
 * The flatMapConcat function does not support suspend.
 * If the transform function does not use suspend, there may be cases that Jacoco does not recognize
 */
fun <T, R> Flow<T>.flatMapConcat(transform: (value: T) -> Flow<R>): Flow<R> =
    kotlinFlatMapConcat(transform)