/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport

/**
 * @author thalys
 */
@JvmInline
@ExcludeFromGeneratedTestReport
value class Link private constructor(val value: String) {
    companion object {
        val EMPTY = Link("")
        operator fun invoke(value: String): Link = if (value.isBlank()) EMPTY else Link(value)
    }
}

fun Link.isEmpty(): Boolean = value.isBlank()
fun Link.isNotEmpty(): Boolean = isEmpty().not()
fun Link?.orEmpty(): Link = this ?: Link("")