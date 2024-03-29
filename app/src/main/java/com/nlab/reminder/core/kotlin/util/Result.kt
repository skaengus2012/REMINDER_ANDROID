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

package com.nlab.reminder.core.kotlin.util

/**
 * Replication of Kotlin Result
 *
 * Because Kotlin's Result is a value class, jacoco doesn't work properly.
 * So, I remastered it in the form of a data class.
 * @author Doohyun
 */
sealed class Result<T> {
    data class Failure<T>(val throwable: Throwable) : Result<T>()
    data class Success<T>(val value: T) : Result<T>()
}