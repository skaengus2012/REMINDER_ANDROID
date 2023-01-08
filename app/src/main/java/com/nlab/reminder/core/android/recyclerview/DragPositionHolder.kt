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

package com.nlab.reminder.core.android.recyclerview

/**
 * @author thalys
 */
class DragPositionHolder {
    private var from: Int? = null
    private var to: Int? = null

    fun snapshot(): DragPosition {
        val curFrom: Int = from ?: return DragPosition.Empty
        val curTo: Int = to ?: return DragPosition.Empty
        return DragPosition.Success(curFrom, curTo)
    }

    fun setPosition(from: Int, to: Int) {
        if (this.from == null) this.from = from
        this.to = to
    }

    fun clearPosition() {
        from = null
        to = null
    }
}