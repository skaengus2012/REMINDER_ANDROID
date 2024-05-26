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

package com.nlab.reminder.core.schedule.ui

/**
 * @author Doohyun
 */

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nlab.reminder.R
import com.nlab.reminder.core.android.animation.animatorListenerOf
import com.nlab.reminder.core.android.content.getDimension
import com.nlab.reminder.databinding.ViewItemScheduleElementBinding
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of Drag N drop, swipe policy for ScheduleItem.
 * Please check below reference document docs1, docs2.
 *
 * @see <a href="https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo">docs1</a>
 * @see <a href="https://min-wachya.tistory.com/171">docs2</a>
 * @author thalys
 */
class ScheduleItemTouchCallback(
    private val clampWidth: Float,
    private val draggedWhenLinkImageVisibleHeight: Float,
    private val itemMoveListener: ItemMoveListener
) : ItemTouchHelper.SimpleCallback(
    /* dragDirs=*/ ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    /* swipeDirs=*/ ItemTouchHelper.START or ItemTouchHelper.END
) {
    private val disposeSwipeClearedAnimations: MutableSet<ViewPropertyAnimator> = HashSet()

    private var disposeScaleAnimation: ViewPropertyAnimator? = null
    private var isItemViewSwipeEnabled: Boolean = false
    private var isLongPressDragEnabled: Boolean = false
    private var curDX: Float = 0f
    private var curContainerX: Float = 0f
    private var curSelectedPosition: Int? = null
    private var prevSelectedPosition: Int? = null

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnabled
    override fun isLongPressDragEnabled(): Boolean = isLongPressDragEnabled

    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
        return itemMoveListener.onItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
    }

    // nothing.
    override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 20

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
        viewHolder.isClamped = curDX <= -clampWidth
        return 2f
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: ViewHolder): Int =
        if (viewHolder.isSupportType) super.getDragDirs(recyclerView, viewHolder)
        else ItemTouchHelper.ACTION_STATE_IDLE

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: ViewHolder): Int =
        if (viewHolder.isSupportType) super.getSwipeDirs(recyclerView, viewHolder)
        else ItemTouchHelper.ACTION_STATE_IDLE

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) = withSupportType(viewHolder) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            removePreviousSwipeClamp(recyclerView)
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                binding.swipeView,
                clampViewPositionHorizontal(dX, isCurrentlyActive, viewHolder.isClamped).also { curDX = it },
                dY,
                actionState,
                isCurrentlyActive
            )
        } else {
            removeSwipeClamp(recyclerView)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) with(binding) {
            root.alpha = if (isCurrentlyActive) 0.7f else 1f
            viewLine.alpha = if (isCurrentlyActive) 0f else 1f

            if (imageviewBgLinkThumbnail.visibility == View.VISIBLE) {
                root.translationX = if (isCurrentlyActive.not()) 0f else curContainerX - (root.width / 2f)

                val scaleP: Float =
                    if (isCurrentlyActive.not()) 1f
                    else (draggedWhenLinkImageVisibleHeight / root.height).coerceAtMost(1f)
                if (scaleP != root.requestedScale) {
                    disposeScaleAnimation?.cancel()
                    root.requestedScale = scaleP
                    root.animate().scaleX(scaleP).scaleY(scaleP)
                        .setDuration(SCALE_ANIMATE_DURATION)
                        .setListener(animatorListenerOf(doOnCancel = {
                            root.clearScaleAnim = null
                            root.scaleX = 1f
                            root.scaleY = 1f
                        }))
                        .also { disposeScaleAnimation = it }
                        .start()
                }
            }
        }
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder
    ) = withSupportType(viewHolder) {
        curDX = 0f
        prevSelectedPosition = viewHolder.absoluteAdapterPosition
        getDefaultUIUtil().clearView(binding.swipeView)
        itemMoveListener.onItemMoveEnded()
    }

    override fun onSelectedChanged(
        viewHolder: ViewHolder?,
        actionState: Int
    ) = withSupportType(viewHolder) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            curSelectedPosition = absoluteAdapterPosition
        }
        getDefaultUIUtil().onSelected(/* view=*/ binding.swipeView)
    }

    private fun clampViewPositionHorizontal(
        dX: Float,
        isCurrentlyActive: Boolean,
        isClamped: Boolean
    ): Float = min(
        0f,
        if (isClamped)
            if (isCurrentlyActive)
                if (dX < 0) max(dX / 3 - clampWidth, -clampWidth)
                else dX - clampWidth
            else -clampWidth
        else max(dX / 2, -clampWidth)
    )

    private fun removeSwipeClampInternal(viewHolder: ViewHolder) = withSupportType(viewHolder) {
        viewHolder.isClamped = false

        val anim = viewHolder.clearClampAnim
        if (anim != null) return

        binding.swipeView.let { view ->
            view.animate()
                .x(0f)
                .setDuration(SWIPE_ANIMATE_DURATION)
                .setListener(animatorListenerOf(doOnCancel = {
                    view.x = 0f
                    viewHolder.clearClampAnim = null
                }))
                .also { disposeSwipeClearedAnimations += it }
                .start()
        }
    }

    private fun removeSwipeClampInternal(recyclerView: RecyclerView, position: Int?) {
        position
            ?.let(recyclerView::findViewHolderForAdapterPosition)
            ?.let(::removeSwipeClampInternal)
    }

    private fun removePreviousSwipeClamp(recyclerView: RecyclerView) {
        if (curSelectedPosition == prevSelectedPosition) return

        removeSwipeClampInternal(recyclerView, prevSelectedPosition)
        prevSelectedPosition = null
    }

    fun removeSwipeClamp(recyclerView: RecyclerView) {
        removeSwipeClampInternal(recyclerView, prevSelectedPosition)
        removeSwipeClampInternal(recyclerView, curSelectedPosition)
        prevSelectedPosition = null
        curSelectedPosition = null
    }

    fun clearResource() {
        disposeSwipeClearedAnimations.forEach { it.cancel() }
        disposeSwipeClearedAnimations.clear()
        disposeScaleAnimation?.cancel()
        disposeScaleAnimation = null
        prevSelectedPosition = null
        curSelectedPosition = null
        curContainerX = 0f
        curDX = 0f
    }

    fun setLongPressDragEnabled(isEnable: Boolean) {
        isLongPressDragEnabled = isEnable
    }

    fun setItemViewSwipeEnabled(isEnable: Boolean) {
        isItemViewSwipeEnabled = isEnable
    }

    fun setContainerX(containerX: Float) {
        curContainerX = containerX
    }

    companion object {
        private const val SWIPE_ANIMATE_DURATION: Long = 100L
        private const val SCALE_ANIMATE_DURATION: Long = 100L

        private val ViewHolder.isSupportType: Boolean get() = this is ScheduleElementViewHolder

        private inline fun withSupportType(
            viewHolder: ViewHolder?,
            block: ScheduleElementViewHolder.() -> Unit
        ) {
            if (viewHolder is ScheduleElementViewHolder) viewHolder.block()
        }

        private val ViewItemScheduleElementBinding.swipeView: View get() = layoutContent

        private var ViewHolder.isClamped: Boolean
            get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_swipe_is_clamp) as? Boolean ?: false
            set(value) {
                itemView.setTag(R.id.tag_schedule_item_touch_callback_swipe_is_clamp, value)
            }

        private var ViewHolder.clearClampAnim: ViewPropertyAnimator?
            get() = itemView
                .getTag(R.id.tag_schedule_item_touch_callback_swipe_clear_clamp_anim) as? ViewPropertyAnimator
            set(value) {
                itemView.setTag(R.id.tag_schedule_item_touch_callback_swipe_clear_clamp_anim, value)
            }

        private var View.requestedScale: Float?
            get() = getTag(R.id.tag_schedule_item_touch_callback_scale_id) as? Float
            set(value) {
                setTag(R.id.tag_schedule_item_touch_callback_scale_id, value)
            }

        private var View.clearScaleAnim: ViewPropertyAnimator?
            get() = getTag(R.id.tag_schedule_item_touch_callback_scale_clear_anim) as? ViewPropertyAnimator
            set(value) {
                setTag(R.id.tag_schedule_item_touch_callback_scale_clear_anim, value)
            }
    }

    interface ItemMoveListener {
        fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean
        fun onItemMoveEnded()
    }
}

fun ScheduleItemTouchCallback(
    context: Context,
    itemMoveListener: ScheduleItemTouchCallback.ItemMoveListener
): ScheduleItemTouchCallback = ScheduleItemTouchCallback(
    clampWidth = context.getDimension(R.dimen.schedule_clamp_width),
    draggedWhenLinkImageVisibleHeight = context.getDimension(R.dimen.schedule_dragged_when_link_thumbnail_height),
    itemMoveListener = itemMoveListener
)