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
 * Identity of [Tag].
 * If Tag exists status becomes [TagId.Present], and the value is greater than 0.
 *
 * If the conditions are not met, the [TagId.Empty] value must be used.
 * If an Id has not yet been specified for Tag, [TagId.Empty] can be used.
 *
 * ```
 * val tag = Tag(
 *      id = TagId.Empty,
 *      value = "My Tag"
 * )
 * ```
 *
 * @author Doohyun
 */
sealed class TagId private constructor() {
    data object Empty : TagId()

    /**
     * [value] must be greater than 0. If you want to use a value of 0, use Empty.
     */
    data class Present(val value: Long) : TagId() {
        init {
            require(value > 0) { "value must be greater than 0." }
        }
    }
}