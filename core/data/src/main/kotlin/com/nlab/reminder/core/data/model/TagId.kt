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

package com.nlab.reminder.core.data.model

/**
 * [value] must be greater than 0. If you want to use a value of 0, use Empty.
 *
 * @author Doohyun
 */
@JvmInline
value class TagId private constructor(val value: Long) {
    companion object {
        val Empty: TagId get() = TagId(value = 0)
        operator fun invoke(value: Long): TagId {
            require(value > 0) { "value must be greater than 0." }
            return TagId(value)
        }
    }
}