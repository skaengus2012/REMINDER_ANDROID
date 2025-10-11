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

package com.nlab.reminder.core.component.schedule.ui.view.list

import android.graphics.Canvas
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.setListener
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.component.schedule.R
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of Drag-n-drop, swipe policy for [ScheduleAdapterItem].
 * Please check below reference document docs1, docs2.
 *
 * @see <a href="https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo">docs1</a>
 * @see <a href="https://min-wachya.tistory.com/171">docs2</a>
 * @author Thalys
 */
class ScheduleListItemTouchCallback(
    private val scrollGuard: ScrollGuard,
    /**
     * If the user progresses to that critical point, it blocks scrolling.
     */
    private val scrollGuardMargin: Float,
    private val dragAnchorOverlay: FrameLayout,
    /**
     * When dragging, if the item needs to be retracted, the height value
     */
    private val dragToScaleTargetHeight: Float,
    private val animateDuration: Long,
    private val itemMoveListener: ItemMoveListener,
) : ItemTouchHelper.SimpleCallback(
    /* dragDirs=*/ ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    /* swipeDirs=*/ ItemTouchHelper.START or ItemTouchHelper.END
) {
    private var selectedActionState: Int = ItemTouchHelper.ACTION_STATE_IDLE

    // Drag item properties
    private val outLocation = IntArray(2)
    private val mirrorViewBindingPool = DraggingMirrorViewPool()
    private var draggingViewHolder: RecyclerView.ViewHolder? = null
    private var dragAnchorMirrorView: View? = null
    private var dragXOffset = -1f
    private var curContainerTouchX: Float = 0f

    // Swipe item properties
    private val disposeSwipeClearedAnimators = mutableSetOf<ViewPropertyAnimator>()
    private var disposeDragScaleAnimator: ViewPropertyAnimator? = null
    private var isItemViewSwipeEnabled: Boolean = false
    private var isLongPressDragEnabled: Boolean = false
    private var curAdjustDX: Float = 0f

    // TODO: Refactor all usages of absolute adapter position (e.g., curSelectedAbsolutePosition, prevSelectedAbsolutePosition)
    // to use bindingAdapterPosition instead, since RecyclerView currently only has a single adapter.
    // Perform this refactor when updating position handling logic or if adapter structure changes.
    private var curSelectedAbsolutePosition: Int? = null
    private var prevSelectedAbsolutePosition: Int? = null

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnabled
    override fun isLongPressDragEnabled(): Boolean = isLongPressDragEnabled

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // do nothing.
        // If you need swipe delete, check the document below.
        // https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        if (viewHolder !is SwipeSupportable) return getSwipeThreshold(viewHolder)

        viewHolder.isClamped = curAdjustDX <= -viewHolder.swipeDelegate.clampWidth
        return 2f // Define 2f to prevent swipe delete
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (viewHolder !is MovableViewHolder || target !is MovableViewHolder) return false
        val fromBindingAdapterPosition = viewHolder.bindingAdapterPosition
        val toBindingAdapterPosition = target.bindingAdapterPosition
        if (fromBindingAdapterPosition == RecyclerView.NO_POSITION || toBindingAdapterPosition == RecyclerView.NO_POSITION) {
            return false
        }

        return itemMoveListener.onMove(fromBindingAdapterPosition, toBindingAdapterPosition)
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (viewHolder is DraggableViewHolder && viewHolder.draggingDelegate.userDraggable()) {
            super.getDragDirs(recyclerView, viewHolder)
        } else {
            ItemTouchHelper.ACTION_STATE_IDLE
        }
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (viewHolder is SwipeSupportable && viewHolder.swipeDelegate.userSwipeable) {
            super.getSwipeDirs(recyclerView, viewHolder)
        } else {
            ItemTouchHelper.ACTION_STATE_IDLE
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder is SwipeSupportable) {
            onChildDrawIfSwipeSupportable(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder is DraggableViewHolder) {
            onChildDrawIfDraggableViewHolder(recyclerView, viewHolder)
        }
    }

    private fun <T> onChildDrawIfSwipeSupportable(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: T,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) where T : RecyclerView.ViewHolder, T : SwipeSupportable {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            removePreviousSwipeClamp(recyclerView)
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                viewHolder.swipeDelegate.swipeView,
                clampViewPositionHorizontal(viewHolder, dX, isCurrentlyActive).also {
                    curAdjustDX = it
                    viewHolder.swipeDelegate.onSwipe(dx = it)
                },
                dY,
                actionState,
                isCurrentlyActive
            )
        } else {
            removeSwipeClamp(recyclerView)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun <T> onChildDrawIfDraggableViewHolder(
        recyclerView: RecyclerView,
        viewHolder: T,
    ) where T : RecyclerView.ViewHolder, T : DraggableViewHolder {
        val mirrorView = dragAnchorMirrorView ?: return
        if (mirrorView.isAttachedToWindow.not() || mirrorView.parent !== dragAnchorOverlay) {
            // Parental status check
            return
        }

        if (dragXOffset == -1f) with(viewHolder.itemView) {
            val scaledWidth = width * scaleX
            val scaledLeft = curContainerTouchX - scaledWidth / 2f
            dragXOffset = if (scaledLeft < 0) {
                (width / 2f) + scaledLeft
            } else {
                val scaledRight = scaledLeft + scaledWidth
                (width / 2f) + (scaledRight - width).coerceAtLeast(minimumValue = 0f)
            }
        }
        viewHolder.itemView.getLocationInWindow(outLocation)
        mirrorView.apply {
            translationX = curContainerTouchX - dragXOffset
            translationY = if (viewHolder.draggingDelegate.isScaleOnDraggingNeeded()) {
                outLocation[1].toFloat() - height / 4f
            } else {
                outLocation[1].toFloat()
            }
        }

        // Scroll guard judgment: If the pointer (=mirror-centered Y) is outside the RV boundary + offset, scroll 'freezes'
        val pointerY = mirrorView.translationY + mirrorView.height / 2f
        recyclerView.getLocationInWindow(outLocation)
        val rvTop = outLocation[1].toFloat()
        val rvBottom = rvTop + recyclerView.height
        val outside = (pointerY < rvTop - scrollGuardMargin) || (pointerY > rvBottom + scrollGuardMargin)
        scrollGuard.setBlocked(outside)
    }

    private fun <T> removeSwipeClampInternal(viewHolder: T) where T : RecyclerView.ViewHolder, T : SwipeSupportable {
        viewHolder.isClamped = false

        if (viewHolder.clampHideAnimator != null) {
            return
        }

        viewHolder.swipeDelegate.swipeView.let { view ->
            view.animate()
                .x(0f)
                .setDuration(animateDuration)
                .setListener(doOnCancel = {
                    // todo check if doOnEnd needed
                    view.x = 0f
                    viewHolder.clampHideAnimator = null
                })
                .also { animator ->
                    disposeSwipeClearedAnimators += animator
                }
                .start()
        }
    }

    private fun removeSwipeClampInternal(recyclerView: RecyclerView, position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return
        if (viewHolder !is SwipeSupportable) return

        removeSwipeClampInternal(viewHolder)
    }

    private fun removePreviousSwipeClamp(recyclerView: RecyclerView) {
        if (curSelectedAbsolutePosition == prevSelectedAbsolutePosition) return
        prevSelectedAbsolutePosition?.let { removeSwipeClampInternal(recyclerView, it) }
        prevSelectedAbsolutePosition = null
    }

    fun removeSwipeClamp(recyclerView: RecyclerView) {
        prevSelectedAbsolutePosition?.let { removeSwipeClampInternal(recyclerView, it) }
        curSelectedAbsolutePosition?.let { removeSwipeClampInternal(recyclerView, it) }
        prevSelectedAbsolutePosition = null
        curSelectedAbsolutePosition = null
    }

    private fun <T> clampViewPositionHorizontal(
        viewHolder: T,
        dx: Float,
        isCurrentlyActive: Boolean
    ): Float where T : RecyclerView.ViewHolder, T : SwipeSupportable {
        val isClamped = viewHolder.isClamped
        val clampWidth = viewHolder.swipeDelegate.clampWidth
        return min(
            0f,
            if (isClamped)
                if (isCurrentlyActive)
                    if (dx < 0) max(dx / 2 - clampWidth, -clampWidth)
                    else dx - clampWidth
                else -clampWidth
            else max(dx / 2, -clampWidth)
        )
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        if (viewHolder is SwipeSupportable) {
            curAdjustDX = 0f
            prevSelectedAbsolutePosition = viewHolder.absoluteAdapterPosition
            getDefaultUIUtil().clearView(viewHolder.swipeDelegate.swipeView)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val oldActionState = selectedActionState
        selectedActionState = actionState

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if (viewHolder !is SwipeSupportable) return

                curSelectedAbsolutePosition = viewHolder.absoluteAdapterPosition
                getDefaultUIUtil().onSelected(/* view=*/ viewHolder.swipeDelegate.swipeView)
            }

            ItemTouchHelper.ACTION_STATE_DRAG -> {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder !is DraggableViewHolder) return

                viewHolder.setIsRecyclable(false)
                viewHolder.draggingDelegate.onDragStateChanged(isActive = true)

                // Original Pipeline Participation
                // If it contains a ViewHolder image, it cannot be processed as alpha.
                // Therefore, treat it as an invisible state
                viewHolder.itemView.setVisible(isVisible = false, goneIfNotVisible = false)

                // Mirror Creation & Binding
                val mirrorView = viewHolder.draggingDelegate
                    .mirrorView(parent = dragAnchorOverlay, viewPool = mirrorViewBindingPool)
                    .also { v ->
                        if (v.parent == null) {
                            dragAnchorOverlay.addView(v)
                        }
                        if (dragAnchorOverlay.childCount > 1) {
                            dragAnchorOverlay.children
                                .filter { it !== v }
                                .forEach { dragAnchorOverlay.removeView(it) }
                        }
                    }
                    .apply {
                        scaleX = 1f
                        scaleY = 1f
                        scaleOnDragging = null
                    }
                if (viewHolder.draggingDelegate.isScaleOnDraggingNeeded()) {
                    val scaleP = (dragToScaleTargetHeight / viewHolder.itemView.height).coerceAtMost(1f)
                    viewHolder.itemView.apply {
                        scaleX = scaleP
                        scaleY = scaleP
                    }
                    if (scaleP != mirrorView.scaleOnDragging) {
                        mirrorView.scaleOnDragging = scaleP
                        disposeDragScaleAnimator?.cancel()
                        disposeDragScaleAnimator = postDragScaleAnimator(
                            view = mirrorView,
                            scale = scaleP,
                            doOnAnimComplete = { disposeDragScaleAnimator = null }
                        )
                    }
                }

                draggingViewHolder = viewHolder
                dragAnchorMirrorView = mirrorView
                dragXOffset = -1f

                // At the start of dragging, release guard
                scrollGuard.setBlocked(false)
                // FIXME Sticky update may be required. from GPT
            }

            ItemTouchHelper.ACTION_STATE_IDLE -> {
                super.onSelectedChanged(viewHolder, actionState)
                when (oldActionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        dragAnchorMirrorView?.takeIf { it.isAttachedToWindow }.let { v ->
                            // According to GPT's explanation, ItemTouchHelper's onSelectedChange and clearView
                            // can be triggered during the middle of a layout pass.
                            // If we remove it immediately at this moment, an IllegalStateException may occur.
                            dragAnchorOverlay.post {
                                if (selectedActionState == ItemTouchHelper.ACTION_STATE_DRAG
                                    && dragAnchorMirrorView === v
                                ) return@post
                                dragAnchorOverlay.removeView(v)
                            }
                        }
                        dragAnchorMirrorView = null

                        draggingViewHolder?.let { viewHolder ->
                            viewHolder.setIsRecyclable(true)
                            viewHolder.itemView.setVisible(isVisible = true)

                            (viewHolder as? DraggableViewHolder)
                                ?.draggingDelegate
                                ?.onDragStateChanged(isActive = false)

                            disposeDragScaleAnimator?.cancel()
                            disposeDragScaleAnimator = postDragScaleAnimator(
                                view = viewHolder.itemView,
                                scale = 1f,
                                doOnAnimComplete = { disposeDragScaleAnimator = null }
                            )
                        }
                        draggingViewHolder = null

                        scrollGuard.setBlocked(false)
                        // FIXME Sticky update may be required. from GPT
                    }
                }
            }
        }
    }

    private inline fun postDragScaleAnimator(
        view: View,
        scale: Float,
        crossinline doOnAnimComplete: () -> Unit
    ): ViewPropertyAnimator = view.animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(animateDuration)
        .setListener(
            doOnEnd = { doOnAnimComplete() },
            doOnCancel = { doOnAnimComplete() }
        )
        .also { it.start() }

    fun clearResource() {
        mirrorViewBindingPool.clear()
        dragAnchorOverlay.removeAllViews()
        disposeSwipeClearedAnimators.forEach { it.cancel() }
        disposeSwipeClearedAnimators.clear()
        disposeDragScaleAnimator?.cancel()
        disposeDragScaleAnimator = null
        prevSelectedAbsolutePosition = null
        curSelectedAbsolutePosition = null
        curContainerTouchX = 0f
        curAdjustDX = 0f
    }

    fun setLongPressDragEnabled(isEnable: Boolean) {
        isLongPressDragEnabled = isEnable
    }

    fun setItemViewSwipeEnabled(isEnable: Boolean) {
        isItemViewSwipeEnabled = isEnable
    }

    fun setContainerTouchX(containerX: Float) {
        curContainerTouchX = containerX
    }

    interface ItemMoveListener {
        fun onMove(fromBindingAdapterPosition: Int, toBindingAdapterPosition: Int): Boolean
        fun onMoveEnded()
    }
}

private var RecyclerView.ViewHolder.isClamped: Boolean
    get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_is_clamp) as? Boolean ?: false
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_is_clamp, value)
    }

private var RecyclerView.ViewHolder.clampHideAnimator: ViewPropertyAnimator?
    get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_clamp_hide_animator) as? ViewPropertyAnimator
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_clamp_hide_animator, value)
    }

private var View.scaleOnDragging: Float?
    get() = getTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging) as? Float
    set(value) {
        setTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging, value)
    }