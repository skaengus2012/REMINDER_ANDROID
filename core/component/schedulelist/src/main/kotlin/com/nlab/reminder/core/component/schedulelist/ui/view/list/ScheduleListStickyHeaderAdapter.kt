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

package com.nlab.reminder.core.component.schedulelist.ui.view.list

import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.androix.recyclerview.stickyheader.StickyHeaderAdapter

/**
 * @author Doohyun
 */
class ScheduleListStickyHeaderAdapter(
    private val getCurrentList: () -> List<ScheduleAdapterItem>,
) : RecyclerView.AdapterDataObserver(),
    StickyHeaderAdapter {
    private val headers = mutableListOf<Int>()

    override fun isStickyHeaderAt(position: Int): Boolean {
        return getCurrentList().getOrNull(position) is StickyHeadAdapterItem
    }

    override fun findStickyHeaderForItem(position: Int): Int {
        if (headers.isEmpty() || position < 0) return RecyclerView.NO_POSITION
        val idx = headers.binarySearch(position)
        return if (idx >= 0) headers[idx]
        else {
            val ins = -idx - 1
            if (ins == 0) RecyclerView.NO_POSITION else headers[ins - 1]
        }
    }

    override fun onChanged() {
        rebuild()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        if (itemCount <= 0) return
        // Check only the scope of changes: add if the header changes, remove if it doesn't
        val end = positionStart + itemCount
        var p = positionStart
        while (p < end) {
            val shouldHeader = isStickyHeaderAt(p)
            val has = headers.binarySearch(p) >= 0
            if (shouldHeader && has.not()) insertHeader(p)
            if (shouldHeader.not() && has) removeHeader(p)
            ++p
        }
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (itemCount <= 0) return

        // Rear Sector Shift (+count)
        val shiftStart = lowerBound(positionStart)
        for (i in shiftStart until headers.size) {
            headers[i] += itemCount
        }

        // Insert binary only header within the insertion range
        // (Judging isStickyHeaderAt based on the adapter index immediately after insertion)
        val end = positionStart + itemCount
        var p = positionStart
        while (p < end) {
            if (isStickyHeaderAt(p)) insertHeader(p)
            ++p
        }
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        if (itemCount <= 0) return
        val removedStart = positionStart
        val removedEnd = positionStart + itemCount

        // Batch remove header in the removal range (all at once using subList)
        val l = lowerBound(removedStart)
        val r = lowerBound(removedEnd)
        if (l < r) headers.subList(l, r).clear()

        // Rear Segment Shift (-count)
        val shiftStart = lowerBound(removedEnd)
        for (i in shiftStart until headers.size) {
            headers[i] -= itemCount
        }
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (itemCount <= 0 || fromPosition == toPosition || headers.isEmpty()) return

        var changed = false
        if (toPosition < fromPosition) {
            for (idx in headers.indices) {
                val i = headers[idx]
                if (i in toPosition until fromPosition) {
                    headers[idx] = i + itemCount
                    changed = true
                } else if (i in fromPosition until (fromPosition + itemCount)) {
                    headers[idx] = (i - fromPosition) + toPosition
                    changed = true
                }
            }
        } else {
            for (idx in headers.indices) {
                val i = headers[idx]
                if (i in (fromPosition + itemCount) until (toPosition + itemCount)) {
                    headers[idx] = i - itemCount
                    changed = true
                } else if (i in fromPosition until (fromPosition + itemCount)) {
                    headers[idx] = (i - fromPosition) + (toPosition - itemCount)
                    changed = true
                }
            }
        }

        if (changed.not()) return

        headers.sort()

        var prev = Int.MIN_VALUE
        var i = 0
        while (i < headers.size) {
            val v = headers[i]
            if (v == prev) headers.removeAt(i)
            else {
                prev = v
                ++i
            }
        }
    }

    private fun rebuild() {
        headers.clear()
        for (i in 0 until getCurrentList().size) {
            if (isStickyHeaderAt(i)) {
                headers.add(i)
            }
        }
    }

    private fun lowerBound(key: Int): Int {
        var l = 0; var r = headers.size
        while (l < r) {
            val m = (l + r) ushr 1
            if (headers[m] < key) l = m + 1 else r = m
        }
        return l
    }

    private fun insertHeader(pos: Int) {
        val idx = headers.binarySearch(pos)
        if (idx < 0) headers.add(-idx - 1, pos) // Insert Keep Alignment
    }

    private fun removeHeader(pos: Int) {
        val idx = headers.binarySearch(pos)
        if (idx >= 0) headers.removeAt(idx)
    }
}