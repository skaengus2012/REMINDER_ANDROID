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

package com.nlab.reminder.core.androix.recyclerview.stickyheader

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 * @author Doohyun
 */
class StickyHeaderHelper {
    private var stickyHeaderContext: StickyHeaderContext? = null

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            if (recyclerView.isLaidOut && stickyHeaderContext != null) {
                invalidate()
            }
        }
    }
    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = invalidate()
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = invalidate()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = invalidate()
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = invalidate()
        override fun onChanged() = invalidate()
    }
    private val initialUpdateRunnable = Runnable { invalidate() }


    fun attach(
        recyclerView: RecyclerView,
        stickyHeaderContainer: FrameLayout,
        stickyHeaderAdapter: StickyHeaderAdapter
    ) {
        if (stickyHeaderContext != null) {
            detach()
        }
        val adapter = requireNotNull(recyclerView.adapter) {
            "The RecyclerView adapter must be attached before calling attach."
        }
        val layoutManager = requireNotNull(recyclerView.layoutManager as? LinearLayoutManager) {
            "StickyHeaderHelper requires the RecyclerView to have a LinearLayoutManager set before calling attach()."
        }

        recyclerView.addOnScrollListener(scrollListener)
        adapter.registerAdapterDataObserver(adapterDataObserver)

        recyclerView.post(/*action = */ initialUpdateRunnable)

        stickyHeaderContext = StickyHeaderContext(
            recyclerView = recyclerView,
            layoutManager = layoutManager,
            adapter = adapter,
            stickyHeaderContainer = stickyHeaderContainer,
            stickyHeaderAdapter = stickyHeaderAdapter
        )
    }

    fun detach() {
        stickyHeaderContext?.let { attachedStickyHeaderContext ->
            with(attachedStickyHeaderContext.recyclerView) {
                removeCallbacks(/*action = */ initialUpdateRunnable)
                removeOnScrollListener(scrollListener)
            }
            attachedStickyHeaderContext.adapter.unregisterAdapterDataObserver(adapterDataObserver)
            attachedStickyHeaderContext.clearHeaderViews()
        }
        stickyHeaderContext = null
    }

    fun invalidate() {
        val currentStickyHeaderContext = stickyHeaderContext
        if (currentStickyHeaderContext == null) {
            Timber.tag("StickyHeaderHelper").w(
                "StickyHeaderHelper has not been attached or has already been detached."
            )
            return
        }
        currentStickyHeaderContext.invalidate()
    }
}

private class StickyHeaderContext(
    val recyclerView: RecyclerView,
    val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    private val layoutManager: LinearLayoutManager,
    private val stickyHeaderContainer: FrameLayout,
    private val stickyHeaderAdapter: StickyHeaderAdapter
) {
    private var currentHeader: View? = null
    private var currentHeaderPosition = RecyclerView.NO_POSITION
    private val viewTypeToHeaderViewHolderTable = mutableMapOf<Int, RecyclerView.ViewHolder>()
    private val refreshCurrentHeaderViewRunnable = Runnable {
        currentHeader?.requestLayout()
        adjustTranslation()
    }

    fun invalidate() {
        check(recyclerView.layoutManager === layoutManager) {
            "The LayoutManager associated with the RecyclerView has been unexpectedly changed after attaching StickyHeaderHelper."
        }
        check(layoutManager.orientation == LinearLayoutManager.VERTICAL) {
            "StickyHeaderHelper only supports LinearLayoutManager with a VERTICAL orientation."
        }
        check(recyclerView.adapter === adapter) {
            "The Adapter associated with the RecyclerView has been unexpectedly changed after attaching StickyHeaderHelper."
        }

        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItemPosition == RecyclerView.NO_POSITION) {
            return
        }
        val headerPosition = stickyHeaderAdapter.findStickyHeaderForItem(position = firstVisibleItemPosition)
        if (headerPosition == RecyclerView.NO_POSITION) {
            clearCurrentHeaderInfo()
            return
        }
        if (headerPosition != currentHeaderPosition) {
            clearCurrentHeaderInfo()
            bindHeader(position = headerPosition)
        }

        val header = currentHeader ?: return
        if (header.isLaidOut.not() && header.height == 0) {
            // When making changes, such as configuration changes, update the current header.
            // If not, the header will disappear.
            header.removeCallbacks(/* action =*/ refreshCurrentHeaderViewRunnable)
            header.post(/* action= */ refreshCurrentHeaderViewRunnable)
        } else {
            adjustTranslation()
        }
    }

    private fun clearCurrentHeaderInfo() {
        currentHeader?.removeCallbacks(/* action =*/ refreshCurrentHeaderViewRunnable)
        currentHeader = null
        currentHeaderPosition = RecyclerView.NO_POSITION
        stickyHeaderContainer.removeAllViews()
    }

    private fun bindHeader(position: Int) {
        val viewType = adapter.getItemViewType(position)
        val viewHolder = viewTypeToHeaderViewHolderTable.getOrPut(key = viewType) {
            adapter.createViewHolder(/*parent=*/ recyclerView, viewType)
        }
        adapter.onBindViewHolder(viewHolder, position)
        currentHeader = viewHolder.itemView.apply {
            layoutParams = FrameLayout.LayoutParams(
                /*width = */ ViewGroup.LayoutParams.MATCH_PARENT,
                /*height = */ ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
        currentHeaderPosition = position
        stickyHeaderContainer.addView(currentHeader)
    }

    private fun adjustTranslation() {
        val header = currentHeader ?: return
        val h = header.height.takeIf { it > 0 } ?: header.measuredHeight

        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION) {
            header.translationY = 0f
            return
        }

        // Based on the currently displayed items, search for the presence of the following headers:
        var nextHeaderTop: Int? = null
        for (i in maxOf(currentHeaderPosition + 1, firstVisiblePosition)..lastVisiblePosition) {
            if (stickyHeaderAdapter.isStickyHeaderAt(i)) {
                nextHeaderTop = layoutManager.findViewByPosition(i)?.top
                break
            }
        }

        header.translationY = if (nextHeaderTop == null || h < nextHeaderTop) {
            0f
        } else {
            (nextHeaderTop - h).toFloat().coerceAtMost(maximumValue = 0f)
        }
    }

    fun clearHeaderViews() {
        clearCurrentHeaderInfo()
        viewTypeToHeaderViewHolderTable.clear()
    }
}