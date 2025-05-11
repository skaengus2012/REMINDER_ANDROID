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

package com.nlab.reminder.core.kotlin

/**
 * @author Thalys
 */
@JvmInline
value class PositiveInt internal constructor(val value: Int) {
    init {
        require(value > 0) { "Value that will have a value of 0 or more" }
    }
}

fun Int.toPositiveInt(): PositiveInt = PositiveInt(value = this)

fun Int?.tryToPositiveInt(): PositiveInt? =
    if (this == null || this < 1) null
    else PositiveInt(value = this)