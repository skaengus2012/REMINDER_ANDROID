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

package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.recyclerview.widget.RecyclerView

/**
 * @author Thalys
 */
internal class ScrollGuard : RecyclerView.OnScrollListener() {
    private var block = false
    private var selfAdjust = false

    fun setBlocked(block: Boolean) {
        this.block = block
    }

    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int
    ) {
        if (recyclerView.isLaidOut.not()) return
        if (block.not() || dy == 0) return
        if (selfAdjust) return
        // Instantly offset auto-scrolling/user scrolling, keeping it 'at rest'
        selfAdjust = true
        recyclerView.scrollBy(0, -dy)
        selfAdjust = false
    }
}