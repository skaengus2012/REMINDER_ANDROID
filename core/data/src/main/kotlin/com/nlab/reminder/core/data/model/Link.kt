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
 * If Link exists, the state becomes [Link.Present].
 * [Link.Present] cannot be a blank value.
 *
 * If the conditions are not met, the [Link.Empty] value must be used.
 *
 * @author thalys
 */
sealed class Link private constructor() {
    data object Empty : Link()
    data class Present(val value: String) : Link() {
        init {
            require(value.isNotBlank())
        }
    }

    companion object {
        fun of(value: String?): Link = if (value.isNullOrBlank()) Empty else Present(value)
    }
}