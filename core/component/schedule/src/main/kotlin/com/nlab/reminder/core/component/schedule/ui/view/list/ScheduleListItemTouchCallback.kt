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

import android.content.Context
import android.graphics.Canvas
import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.content.getDimension
import com.nlab.reminder.core.android.view.setListener
import com.nlab.reminder.core.component.schedule.R
import kotlin.math.max
import kotlin.math.min

private const val DEFAULT_ANIMATE_DURATION = 250L

/**
 * Implementation of Drag-n-drop, swipe policy for [ScheduleAdapterItem].
 * Please check below reference document docs1, docs2.
 *
 * @see <a href="https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo">docs1</a>
 * @see <a href="https://min-wachya.tistory.com/171">docs2</a>
 * @author Thalys
 */
class ScheduleListItemTouchCallback(
    private val draggedWhenScaleViewHeight: Float,
    private val itemMoveListener: ItemMoveListener
) : ItemTouchHelper.SimpleCallback(
    /* dragDirs=*/ ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    /* swipeDirs=*/ ItemTouchHelper.START or ItemTouchHelper.END
) {
    private val disposeSwipeClearedAnimators = mutableSetOf<ViewPropertyAnimator>()
    private var disposeDragScaleAnimator: ViewPropertyAnimator? = null
    private var isItemViewSwipeEnabled: Boolean = false
    private var isLongPressDragEnabled: Boolean = false
    private var curAdjustDX: Float = 0f
    private var curContainerTouchX: Float = 0f
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
        return if (viewHolder is DraggingSupportable && target is DraggingSupportable) {
            itemMoveListener.onMove(viewHolder, target)
        } else {
            false
        }
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (viewHolder is DraggingSupportable && viewHolder.draggingDelegate.userDraggable) {
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
        if (viewHolder is DraggingSupportable) {
            onChildDrawIfDraggingSupportable(viewHolder, actionState, isCurrentlyActive)
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
                    viewHolder.swipeDelegate.onSwipe(isActive = isCurrentlyActive, it)
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

    private fun <T> onChildDrawIfDraggingSupportable(
        viewHolder: T,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) where T : RecyclerView.ViewHolder, T : DraggingSupportable {
        if (actionState != ItemTouchHelper.ACTION_STATE_DRAG) return
        viewHolder.draggingDelegate.onDragging(isActive = isCurrentlyActive)

        if (viewHolder.draggingDelegate.isScaleOnDraggingNeeded()) {
            viewHolder.itemView.apply {
                translationX =
                    if (isCurrentlyActive) curContainerTouchX - (width / 2f)
                    else 0f
            }
            val scaleP: Float =
                if (isCurrentlyActive.not()) 1f
                else (draggedWhenScaleViewHeight / viewHolder.itemView.height).coerceAtMost(1f)
            if (scaleP != viewHolder.scaleOnDragging) {
                disposeDragScaleAnimator?.cancel()
                viewHolder.scaleOnDragging = scaleP
                viewHolder.itemView.animate()
                    .scaleX(scaleP)
                    .scaleY(scaleP)
                    .setDuration(DEFAULT_ANIMATE_DURATION)
                    .setListener(doOnCancel = {
                        disposeDragScaleAnimator = null
                        viewHolder.itemView.apply { scaleX = 1f; scaleY = 1f }
                    })
                    .also { disposeDragScaleAnimator = it }
                    .start()
            }
        }
    }

    private fun <T> removeSwipeClampInternal(viewHolder: T) where T : RecyclerView.ViewHolder, T : SwipeSupportable {
        viewHolder.isClamped = false

        if (viewHolder.clampHideAnimator != null) {
            return
        }

        viewHolder.swipeDelegate.swipeView.let { view ->
            view.animate()
                .x(0f)
                .setDuration(DEFAULT_ANIMATE_DURATION)
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

        itemMoveListener.onMoveEnded()
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        if (viewHolder is SwipeSupportable) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                curSelectedAbsolutePosition = viewHolder.absoluteAdapterPosition
            }
            getDefaultUIUtil().onSelected(/* view=*/ viewHolder.swipeDelegate.swipeView)
        }
    }

    fun clearResource() {
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
        fun onMove(fromViewHolder: RecyclerView.ViewHolder, toViewHolder: RecyclerView.ViewHolder): Boolean
        fun onMoveEnded()
    }
}

fun ScheduleListItemTouchCallback(
    context: Context,
    itemMoveListener: ScheduleListItemTouchCallback.ItemMoveListener
): ScheduleListItemTouchCallback = ScheduleListItemTouchCallback(
    draggedWhenScaleViewHeight = context.getDimension(R.dimen.schedule_dragging_scale_item_height),
    itemMoveListener = itemMoveListener
)

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

private var RecyclerView.ViewHolder.scaleOnDragging: Float?
    get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging) as? Float
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging, value)
    }