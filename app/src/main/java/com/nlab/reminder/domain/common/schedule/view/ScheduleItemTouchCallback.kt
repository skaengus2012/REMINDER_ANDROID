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

package com.nlab.reminder.domain.common.schedule.view

import android.graphics.Canvas
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nlab.reminder.R
import com.nlab.reminder.core.android.animation.animatorListenerOf
import com.nlab.reminder.databinding.ViewItemScheduleBinding
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
    private val onItemMoved: (fromPosition: Int, toPosition: Int) -> Boolean,
    private val onItemMoveEnded: () -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.START or ItemTouchHelper.END
) {
    private val disposeSwipeClearedAnimations: MutableSet<ViewPropertyAnimator> = HashSet()

    private var disposeScaleAnimation: ViewPropertyAnimator? = null
    private var isItemViewSwipeEnabled: Boolean = true
    private var curDX: Float = 0f
    private var curContainerX: Float = 0f
    private var curPosition: Int? = null
    private var prevPosition: Int? = null

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnabled
    override fun isLongPressDragEnabled(): Boolean = true

    override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean {
        return onItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
    }

    // nothing.
    override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
        viewHolder.isClamped = curDX <= -clampWidth
        return 2f
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val binding: ViewItemScheduleBinding = viewHolder.binding
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

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            with(binding) {
                viewLine.visibility = if (isCurrentlyActive) View.INVISIBLE else View.VISIBLE

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
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        val binding: ViewItemScheduleBinding = viewHolder.binding
        curDX = 0f
        prevPosition = viewHolder.bindingAdapterPosition
        getDefaultUIUtil().clearView(binding.swipeView)
        onItemMoveEnded()
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            curPosition = viewHolder?.bindingAdapterPosition ?: return
        }

        getDefaultUIUtil().onSelected(viewHolder?.binding?.swipeView ?: return)
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

    private fun removeSwipeClampInternal(viewHolder: ViewHolder) {
        viewHolder.isClamped = false

        val anim = viewHolder.clearClampAnim
        if (anim != null) return
        viewHolder.binding.swipeView.let { view ->
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
        if (curPosition == prevPosition) return

        removeSwipeClampInternal(recyclerView, prevPosition)
        prevPosition = null
    }

    fun removeSwipeClamp(recyclerView: RecyclerView) {
        removeSwipeClampInternal(recyclerView, prevPosition)
        removeSwipeClampInternal(recyclerView, curPosition)
        prevPosition = null
        curPosition = null
    }

    fun clearResource() {
        disposeSwipeClearedAnimations.forEach { it.cancel() }
        disposeSwipeClearedAnimations.clear()
        disposeScaleAnimation?.cancel()
        disposeScaleAnimation = null
        prevPosition = null
        curPosition = null
        curContainerX = 0f
        curDX = 0f
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

        private val ViewItemScheduleBinding.swipeView: View get() = layoutContent
        private val ViewHolder.binding: ViewItemScheduleBinding get() = ViewItemScheduleBinding.bind(itemView)
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
}