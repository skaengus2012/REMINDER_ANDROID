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

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.isLayoutRtl
import com.nlab.reminder.core.android.view.setListener
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.component.schedulelist.R
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of Drag-n-drop, swipe policy for [ScheduleListItem].
 * Please check below reference document docs1, docs2.
 *
 * @see <a href="https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo">docs1</a>
 * @see <a href="https://min-wachya.tistory.com/171">docs2</a>
 * @author Thalys
 */
internal class ScheduleListItemTouchCallback(
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
    @param:IntRange(from = 0) private val dragScaleAnimateDuration: Long,
    @param:IntRange(from = 0) private val swipeCancelAnimateDuration: Long,
    @param:FloatRange(from = 0.0, to = 1.0) private val clampSwipeThreshold: Float,
    @param:FloatRange(from = 1.0) private val maxClampSwipeWidthMultiplier: Float,
    private val itemMoveListener: ItemMoveListener,
) : ItemTouchHelper.SimpleCallback(
    /* dragDirs=*/ ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    /* swipeDirs=*/ ItemTouchHelper.START or ItemTouchHelper.END
) {
    private var selectedActionState: Int = ItemTouchHelper.ACTION_STATE_IDLE
    private var isItemViewSwipeEnabled: Boolean = false
    private var isLongPressDragEnabled: Boolean = false

    // Drag item properties
    private val outLocation = IntArray(2)
    private val mirrorViewBindingPool = DraggingMirrorViewPool()
    private val dragScaleInterpolator = OvershootInterpolator()
    private val dragEndCallbacks = mutableListOf<() -> Unit>()
    private var disposeDragScaleAnimator: ViewPropertyAnimator? = null
    private var draggingViewHolder: RecyclerView.ViewHolder? = null
    private var dragAnchorMirrorView: View? = null
    private var dragXOffset = -1f
    private var curContainerTouchX: Float = 0f

    // Swipe item properties
    private val swipingClampOpenAnimationInterpolator = DecelerateInterpolator()
    private val swipingClampCloseAnimationInterpolator = AccelerateInterpolator()
    private val disposeSwipeClampAnimators = mutableSetOf<Animator>()
    private var curSwipingPosition: Int? = null
    private var prevSwipingPosition: Int? = null

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnabled
    override fun isLongPressDragEnabled(): Boolean = isLongPressDragEnabled

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // do nothing.
        // If you need swipe delete, check the document below.
        // https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        if (viewHolder !is SwipeableViewHolder) return super.getSwipeThreshold(viewHolder)
        return 2f // Define 2f to prevent swipe delete
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (viewHolder !is MovableViewHolder || target !is MovableViewHolder) return false
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
            return false
        }

        return itemMoveListener.onMove(fromPosition, toPosition)
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (selectedActionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // Occasionally, when dragging, it may swipe.
            // force removal
            removeSwipeClamp(recyclerView)
        }

        return if (selectedActionState != ItemTouchHelper.ACTION_STATE_SWIPE
            && viewHolder is DraggableViewHolder
            && viewHolder.userDraggable()
        ) {
            super.getDragDirs(recyclerView, viewHolder)
        } else {
            ItemTouchHelper.ACTION_STATE_IDLE
        }
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (selectedActionState != ItemTouchHelper.ACTION_STATE_DRAG
            && viewHolder is SwipeableViewHolder
            && viewHolder.userSwipeable()
        ) {
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
        if (viewHolder is SwipeableViewHolder) {
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
    ) where T : RecyclerView.ViewHolder, T : SwipeableViewHolder {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            removePreviousSwipeClamp(recyclerView)
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                viewHolder.swipeView,
                /* dX= */ if (isCurrentlyActive) {
                    calculateAndSetSwipingClampX(viewHolder, dX).also { viewHolder.onSwipe(dx = it) }
                } else {
                    viewHolder.userSwipingDX ?: 0f
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
            translationY = if (viewHolder.isScaleOnDraggingNeeded()) {
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

    private fun <T> registerSwipeClampAnimator(
        viewHolder: T,
        @SwipingClampAnimationType animationType: Int
    ) where T : RecyclerView.ViewHolder, T : SwipeableViewHolder {
        val curAnimatorState = viewHolder.swipingClampAnimatorState
        if (curAnimatorState != null && curAnimatorState.animationType == animationType) {
            return
        }
        curAnimatorState?.animator?.cancel()

        val goalX = when (animationType) {
            SWIPING_CLAMP_ANIMATION_SHOW -> {
                val clampWidth = viewHolder.clampView.width.toFloat()
                if (viewHolder.itemView.isLayoutRtl) clampWidth
                else -clampWidth
            }
            SWIPING_CLAMP_ANIMATION_HIDE -> {
                0f
            }
            else -> {
                throw IllegalArgumentException("Invalid swiping animation Type -> $animationType")
            }
        }
        val latestSwiping = viewHolder.userSwipingDX ?: 0f
        ValueAnimator.ofFloat(latestSwiping, goalX)
            .setDuration(swipeCancelAnimateDuration)
            .apply {
                if (animationType == SWIPING_CLAMP_ANIMATION_SHOW) {
                    interpolator = swipingClampOpenAnimationInterpolator
                } else {
                    interpolator = swipingClampCloseAnimationInterpolator
                    // Added a 100ms delay to prevent
                    // the user from instantaneously shrinking when they touch to open Swipe again.
                    startDelay = 100
                }
                addUpdateListener { valueAnimator ->
                    val animateDx = valueAnimator.animatedValue as Float
                    viewHolder.userSwipingDX = animateDx
                    viewHolder.onSwipe(dx = animateDx)
                    viewHolder.swipeView.translationX = animateDx
                }
                var isAnimationCancelled = false
                doOnCancel { isAnimationCancelled = true }
                doOnEnd {
                    if (isAnimationCancelled) return@doOnEnd

                    viewHolder.swipingClampAnimatorState = null
                    viewHolder.onSwipe(dx = goalX)
                    viewHolder.userSwipingDX = if (animationType == SWIPING_CLAMP_ANIMATION_HIDE) {
                        null
                    } else {
                        goalX
                    }
                }
            }
            .also { animator ->
                viewHolder.swipingClampAnimatorState = SwipingClampAnimateState(
                    animationType = animationType,
                    animator = animator
                )
                disposeSwipeClampAnimators += animator
            }
            .start()
    }

    private fun removeSwipeClampInternal(recyclerView: RecyclerView, position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return
        if (viewHolder !is SwipeableViewHolder) return
        registerSwipeClampAnimator(
            viewHolder = viewHolder,
            animationType = SWIPING_CLAMP_ANIMATION_HIDE
        )
    }

    private fun removePreviousSwipeClamp(recyclerView: RecyclerView) {
        if (curSwipingPosition == prevSwipingPosition) return
        prevSwipingPosition?.let { removeSwipeClampInternal(recyclerView, it) }
        prevSwipingPosition = null
    }

    fun removeSwipeClamp(recyclerView: RecyclerView) {
        prevSwipingPosition?.let { removeSwipeClampInternal(recyclerView, it) }
        curSwipingPosition?.let { removeSwipeClampInternal(recyclerView, it) }
        prevSwipingPosition = null
        curSwipingPosition = null
    }

    private fun <T> calculateAndSetSwipingClampX(
        viewHolder: T,
        dX: Float
    ): Float where T : RecyclerView.ViewHolder, T : SwipeableViewHolder {
        val pivotX = viewHolder.swipingPivotX ?: 0f
        val prevDX = viewHolder.userSwipingDX
        val maxClampWidth = viewHolder.clampView.width * maxClampSwipeWidthMultiplier

        val adjustDX = dX / 2 + pivotX
        val newDX = if (viewHolder.itemView.isLayoutRtl) {
            max(0f, min(adjustDX, maxClampWidth))
        } else {
            min(0f, max(adjustDX, -maxClampWidth))
        }

        val notChanged = prevDX != null && prevDX.absoluteValue >= newDX.absoluteValue
        return if (notChanged) prevDX else newDX.also { viewHolder.userSwipingDX = it }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is SwipeableViewHolder) {
            val clampView = viewHolder.clampView
            val userSwipingDX = viewHolder.userSwipingDX
            if (userSwipingDX != null) {
                viewHolder.itemView.post {
                    registerSwipeClampAnimator(
                        viewHolder = viewHolder,
                        animationType = when (userSwipingDX.absoluteValue >= clampView.width * clampSwipeThreshold) {
                            true -> SWIPING_CLAMP_ANIMATION_SHOW
                            false -> SWIPING_CLAMP_ANIMATION_HIDE
                        }
                    )
                }
            }
            prevSwipingPosition = viewHolder.bindingAdapterPosition
            getDefaultUIUtil().clearView(viewHolder.swipeView)
        }
    }

    fun stopDragging(recyclerView: RecyclerView, commitCallback: () -> Unit) {
        if (selectedActionState != ItemTouchHelper.ACTION_STATE_DRAG) {
            commitCallback.invoke()
            return
        }
        val now = SystemClock.uptimeMillis()
        val cancelEvent = MotionEvent.obtain(
            now,
            now,
            MotionEvent.ACTION_CANCEL,
            0f,
            0f,
            0
        )
        recyclerView.dispatchTouchEvent(cancelEvent)
        cancelEvent.recycle()
        dragEndCallbacks += commitCallback
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val oldActionState = selectedActionState
        selectedActionState = actionState

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if (viewHolder !is SwipeableViewHolder) return

                curSwipingPosition = viewHolder.bindingAdapterPosition
                viewHolder.swipingPivotX = viewHolder.userSwipingDX
                viewHolder.swipingClampAnimatorState
                    ?.takeIf { it.animationType == SWIPING_CLAMP_ANIMATION_HIDE }
                    ?.run {
                        animator.cancel()
                        viewHolder.swipingClampAnimatorState = null
                    }
                getDefaultUIUtil().onSelected(/* view=*/ viewHolder.swipeView)
            }

            ItemTouchHelper.ACTION_STATE_DRAG -> {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder !is DraggableViewHolder) return

                viewHolder.setIsRecyclable(false)
                viewHolder.onDragStateChanged(isActive = true)

                // Original Pipeline Participation
                // If it contains a ViewHolder image, it cannot be processed as alpha.
                // Therefore, treat it as an invisible state
                viewHolder.itemView.setVisible(isVisible = false, goneIfNotVisible = false)

                // Mirror Creation & Binding
                val mirrorView = viewHolder
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
                if (viewHolder.isScaleOnDraggingNeeded()) {
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

                            (viewHolder as? DraggableViewHolder)?.onDragStateChanged(isActive = false)
                            disposeDragScaleAnimator?.cancel()
                            disposeDragScaleAnimator = postDragScaleAnimator(
                                view = viewHolder.itemView,
                                scale = 1f,
                                doOnAnimComplete = {
                                    dragEndCallbacks.forEach { it.invoke() }
                                    dragEndCallbacks.clear()
                                    disposeDragScaleAnimator = null
                                }
                            )
                        }
                        draggingViewHolder = null

                        itemMoveListener.onMoveEnded()
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
        .setDuration(dragScaleAnimateDuration)
        .setInterpolator(dragScaleInterpolator)
        .setListener(
            doOnEnd = { doOnAnimComplete() },
            doOnCancel = {
                // When canceled, the scale state is restored.
                // When drag is restored, the scale may not return.
                view.scaleX = scale
                view.scaleY = scale
                doOnAnimComplete()
            }
        )
        .also { it.start() }

    fun clearResource() {
        mirrorViewBindingPool.clear()
        dragAnchorOverlay.removeAllViews()
        disposeSwipeClampAnimators.forEach { it.cancel() }
        disposeSwipeClampAnimators.clear()
        disposeDragScaleAnimator?.cancel()
        disposeDragScaleAnimator = null
        prevSwipingPosition = null
        curSwipingPosition = null
        curContainerTouchX = 0f
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
        fun onMove(fromPosition: Int, toPosition: Int): Boolean
        fun onMoveEnded()
    }
}

private const val SWIPING_CLAMP_ANIMATION_SHOW = 1
private const val SWIPING_CLAMP_ANIMATION_HIDE = 2

@IntDef(
    value = [
        SWIPING_CLAMP_ANIMATION_SHOW,
        SWIPING_CLAMP_ANIMATION_HIDE
    ]
)
@Retention(AnnotationRetention.SOURCE)
private annotation class SwipingClampAnimationType

private class SwipingClampAnimateState(
    @SwipingClampAnimationType val animationType: Int,
    val animator: Animator
)

private var RecyclerView.ViewHolder.userSwipingDX: Float?
    get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_user_swiping_dx) as? Float
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_user_swiping_dx, value)
    }

private var RecyclerView.ViewHolder.swipingPivotX: Float?
    get() = itemView.getTag(R.id.tag_schedule_item_touch_callback_swiping_pivot_x) as? Float
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_swiping_pivot_x, value)
    }

private var RecyclerView.ViewHolder.swipingClampAnimatorState: SwipingClampAnimateState?
    get() {
        val value = itemView.getTag(R.id.tag_schedule_item_touch_callback_clamp_animator)
        return value as? SwipingClampAnimateState
    }
    set(value) {
        itemView.setTag(R.id.tag_schedule_item_touch_callback_clamp_animator, value)
    }

private var View.scaleOnDragging: Float?
    get() = getTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging) as? Float
    set(value) {
        setTag(R.id.tag_schedule_item_touch_callback_scale_on_dragging, value)
    }
