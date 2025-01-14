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

package com.nlab.reminder.core.androix.recyclerview.selection

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.content.getDimension
import com.nlab.reminder.core.androix.recyclerview.R
import timber.log.Timber

private typealias AutoScrollListener = (scrolling: Boolean) -> Unit

/**
 * @see <a href="https://github.com/afollestad/drag-select-recyclerview">Drag Select Recycler View</a>
 * @author Thalys
 */
class MultiSelectTouchListener(
    context: Context,
    private val receiver: MultiSelectReceiver
) : RecyclerView.OnItemTouchListener {
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (inTopHotspot) {
                recyclerView?.scrollBy(0, -autoScrollVelocity)
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY)
            } else if (inBottomHotspot) {
                recyclerView?.scrollBy(0, autoScrollVelocity)
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY)
            }
        }
    }

    var hotspotHeight: Int = context.getDimension(R.dimen.multi_select_defaultHotspotHeight).toInt()
    var hotspotOffsetTop: Int = 0
    var hotspotOffsetBottom: Int = 0
    var autoScrollListener: AutoScrollListener? = null

    var mode: Mode = Mode.RANGE
        set(mode) {
            field = mode
            // Shouldn't maintain an active state through mode changes
            setIsActive(false, -1)
        }

    private var recyclerView: RecyclerView? = null

    private var lastDraggedIndex = -1
    private var initialSelection: Int = 0
    private var dragSelectActive: Boolean = false
    private var minReached: Int = 0
    private var maxReached: Int = 0

    private var hotspotTopBoundStart: Int = 0
    private var hotspotTopBoundEnd: Int = 0
    private var hotspotBottomBoundStart: Int = 0
    private var hotspotBottomBoundEnd: Int = 0
    private var inTopHotspot: Boolean = false
    private var inBottomHotspot: Boolean = false

    private var autoScrollVelocity: Int = 0
    private var isAutoScrolling: Boolean = false

    private fun notifyAutoScrollListener(scrolling: Boolean) {
        if (this.isAutoScrolling == scrolling) return
        Timber.d(if (scrolling) "Auto scrolling is active" else "Auto scrolling is inactive")
        this.isAutoScrolling = scrolling
        this.autoScrollListener?.invoke(scrolling)
    }

    /**
     * Initializes drag selection.
     *
     * @param active True if we are starting drag selection, false to terminate it.
     * @param initialSelection The index of the item which was pressed while starting drag selection.
     */
    fun setIsActive(
        active: Boolean,
        initialSelection: Int
    ): Boolean {
        if (active && dragSelectActive) {
            Timber.d("Drag selection is already active.")
            return false
        }

        this.lastDraggedIndex = -1
        this.minReached = -1
        this.maxReached = -1
        this.autoScrollHandler.removeCallbacks(autoScrollRunnable)
        this.notifyAutoScrollListener(false)
        this.inTopHotspot = false
        this.inBottomHotspot = false

        if (!active) {
            // Don't do any of the initialization below since we are terminating
            this.initialSelection = -1
            return false
        }

        if (!receiver.isIndexSelectable(initialSelection)) {
            this.dragSelectActive = false
            this.initialSelection = -1
            Timber.d("Index $initialSelection is not selectable.")
            return false
        }

        receiver.setSelected(
            index = initialSelection,
            selected = true
        )
        this.dragSelectActive = true
        this.initialSelection = initialSelection
        this.lastDraggedIndex = initialSelection

        Timber.d("Drag selection initialized, starting at index $initialSelection.")
        return true
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    override fun onInterceptTouchEvent(
        view: RecyclerView,
        event: MotionEvent
    ): Boolean {
        val adapterIsEmpty = view.adapter?.let { it.itemCount == 0 } ?: true
        val result = dragSelectActive && !adapterIsEmpty

        if (result) {
            recyclerView = view
            Timber.d("RecyclerView height = ${view.measuredHeight}")

            if (hotspotHeight > -1) {
                hotspotTopBoundStart = hotspotOffsetTop
                hotspotTopBoundEnd = hotspotOffsetTop + hotspotHeight
                hotspotBottomBoundStart = view.measuredHeight - hotspotHeight - hotspotOffsetBottom
                hotspotBottomBoundEnd = view.measuredHeight - hotspotOffsetBottom
                Timber.d("Hotspot top bound = $hotspotTopBoundStart to $hotspotTopBoundEnd")
                Timber.d("Hotspot bottom bound = $hotspotBottomBoundStart to $hotspotBottomBoundEnd")
            }
        }

        if (result && event.action == ACTION_UP) {
            onDragSelectionStop()
        }
        return result
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    override fun onTouchEvent(
        view: RecyclerView,
        event: MotionEvent
    ) {
        val action = event.action
        val itemPosition = view.getItemPosition(event)
        val y = event.y

        when (action) {
            ACTION_UP -> {
                onDragSelectionStop()
                return
            }

            ACTION_MOVE -> {
                if (hotspotHeight > -1) {
                    // Check for auto-scroll hotspot
                    if (y >= hotspotTopBoundStart && y <= hotspotTopBoundEnd) {
                        inBottomHotspot = false
                        if (!inTopHotspot) {
                            inTopHotspot = true
                            Timber.d("Now in TOP hotspot")
                            autoScrollHandler.removeCallbacks(autoScrollRunnable)
                            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY.toLong())
                            this.notifyAutoScrollListener(true)
                        }
                        val simulatedFactor = (hotspotTopBoundEnd - hotspotTopBoundStart).toFloat()
                        val simulatedY = y - hotspotTopBoundStart
                        autoScrollVelocity = (simulatedFactor - simulatedY).toInt() / 2
                        Timber.d("Auto scroll velocity = $autoScrollVelocity")
                    } else if (y >= hotspotBottomBoundStart && y <= hotspotBottomBoundEnd) {
                        inTopHotspot = false
                        if (!inBottomHotspot) {
                            inBottomHotspot = true
                            Timber.d("Now in BOTTOM hotspot")
                            autoScrollHandler.removeCallbacks(autoScrollRunnable)
                            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY.toLong())
                            this.notifyAutoScrollListener(true)
                        }
                        val simulatedY = y + hotspotBottomBoundEnd
                        val simulatedFactor = (hotspotBottomBoundStart + hotspotBottomBoundEnd).toFloat()
                        autoScrollVelocity = (simulatedY - simulatedFactor).toInt() / 2
                        Timber.d("Auto scroll velocity = $autoScrollVelocity")
                    } else if (inTopHotspot || inBottomHotspot) {
                        Timber.d("Left the hotspot")
                        autoScrollHandler.removeCallbacks(autoScrollRunnable)
                        this.notifyAutoScrollListener(false)
                        inTopHotspot = false
                        inBottomHotspot = false
                    }
                }

                // Drag selection logic
                if (mode == Mode.PATH && itemPosition != RecyclerView.NO_POSITION) {
                    // Non-default mode, we select exactly what the user touches over
                    if (lastDraggedIndex == itemPosition) return
                    lastDraggedIndex = itemPosition
                    receiver.setSelected(
                        index = lastDraggedIndex,
                        selected = !receiver.isSelected(lastDraggedIndex)
                    )
                    return
                }

                if (mode == Mode.RANGE &&
                    itemPosition != RecyclerView.NO_POSITION &&
                    lastDraggedIndex != itemPosition
                ) {
                    lastDraggedIndex = itemPosition
                    if (minReached == -1) minReached = lastDraggedIndex
                    if (maxReached == -1) maxReached = lastDraggedIndex
                    if (lastDraggedIndex > maxReached) maxReached = lastDraggedIndex
                    if (lastDraggedIndex < minReached) minReached = lastDraggedIndex
                    selectRange(
                        from = initialSelection,
                        to = lastDraggedIndex,
                        min = minReached,
                        max = maxReached
                    )
                    if (initialSelection == lastDraggedIndex) {
                        minReached = lastDraggedIndex
                        maxReached = lastDraggedIndex
                    }
                }
                return
            }
        }
    }

    private fun onDragSelectionStop() {
        dragSelectActive = false
        inTopHotspot = false
        inBottomHotspot = false
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        this.notifyAutoScrollListener(false)
    }

    @RestrictTo(Scope.LIBRARY_GROUP)
    override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) = Unit

    private fun selectRange(
        from: Int,
        to: Int,
        min: Int,
        max: Int
    ) = with(receiver) {
        if (from == to) {
            // Finger is back on the initial item, unselect everything else
            for (i in min..max) {
                if (i == from) {
                    continue
                }
                setSelected(i, false)
            }
            return
        }

        if (to < from) {
            // When selecting from one to previous items
            for (i in to..from) {
                setSelected(i, true)
            }
            if (min > -1 && min < to) {
                // Unselect items that were selected during this drag but no longer are
                for (i in min until to) {
                    setSelected(i, false)
                }
            }
            if (max > -1) {
                for (i in from + 1..max) {
                    setSelected(i, false)
                }
            }
        } else {
            // When selecting from one to next items
            for (i in from..to) {
                setSelected(i, true)
            }
            if (max > -1 && max > to) {
                // Unselect items that were selected during this drag but no longer are
                for (i in to + 1..max) {
                    setSelected(i, false)
                }
            }
            if (min > -1) {
                for (i in min until from) {
                    setSelected(i, false)
                }
            }
        }
    }

    fun disableAutoScroll() {
        hotspotHeight = -1
        hotspotOffsetTop = -1
        hotspotOffsetBottom = -1
    }

    fun clearResource() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        recyclerView = null
    }

    companion object {
        private const val AUTO_SCROLL_DELAY = 25L

        fun create(
            context: Context,
            receiver: MultiSelectReceiver,
            config: (MultiSelectTouchListener.() -> Unit)? = null
        ): MultiSelectTouchListener {
            val listener = MultiSelectTouchListener(
                context = context,
                receiver = receiver
            )
            if (config != null) {
                listener.config()
            }
            return listener
        }
    }
}

private fun RecyclerView.getItemPosition(e: MotionEvent): Int {
    val v = findChildViewUnder(e.x, e.y) ?: return RecyclerView.NO_POSITION
    return getChildAdapterPosition(v)
}

enum class Mode {
    RANGE,
    PATH
}