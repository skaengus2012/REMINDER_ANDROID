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

package com.nlab.reminder.core.component.schedulelist.internal.ui

import androidx.recyclerview.widget.RecyclerView

/**
 * @author Doohyun
 */
class FocusChange internal constructor(
    val viewHolder: RecyclerView.ViewHolder,
    val focused: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FocusChange

        if (viewHolder != other.viewHolder) return false
        if (focused != other.focused) return false

        return true
    }

    override fun hashCode(): Int {
        var result = viewHolder.hashCode()
        result = 31 * result + focused.hashCode()
        return result
    }
}