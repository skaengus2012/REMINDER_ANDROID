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

package com.nlab.reminder.core.component.schedulelist.ui.content

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.nlab.reminder.core.android.view.awaitUntilLaidOut
import com.nlab.reminder.core.component.schedulelist.R
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentBinding
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentMirrorBinding
import kotlinx.coroutines.Runnable

/**
 * @author Doohyun
 */
internal class ContentSelectionAnimDelegate(private val binding: LayoutScheduleAdapterItemContentBinding) {
    private var selectedExtraContentGuideEnd = 0
    private var unselectedExtraContentGuideEnd = 0

    private var selectedCompleteButtonTranslationX = 0
    private var selectedSelectionButtonTranslationX = 0
    private var selectedDragButtonTransitionX = 0

    private val disposableContentActions = mutableListOf<Runnable>()
    private var disposableAnimator: Animator? = null

    private var isReady = false
    private var isFirstAnimate = true
    private var latestSelectable = false

    suspend fun awaitReady() {
        // Check whether the settings have already been initialized
        if (isReady) {
            return
        }

        // await layout
        binding.layoutContent.awaitUntilLaidOut()
        // initialized
        isReady = true

        val isLtr = binding.root.layoutDirection.let { direction -> direction == View.LAYOUT_DIRECTION_LTR }
        val bodyStartGuideBegin = binding.guidelineBodyStart
            .constraintLayoutParams
            .guideBegin
        val checkboxTouchableWidth = binding.root.resources.getDimensionPixelSize(
            /* id = */ R.dimen.schedule_checkbox_touchable_size
        )

        selectedExtraContentGuideEnd = checkboxTouchableWidth
        unselectedExtraContentGuideEnd = binding.guidelineExtraContentEnd
            .constraintLayoutParams
            .guideEnd
        selectedCompleteButtonTranslationX = with(binding) {
            val completionSelectedGuideBegin = binding.guidelineCompletionSelectedStart
                .constraintLayoutParams
                .guideBegin
            val diff = (completionSelectedGuideBegin - bodyStartGuideBegin).coerceAtLeast(minimumValue = 0)
            if (isLtr) diff
            else -diff
        }
        selectedSelectionButtonTranslationX = bodyStartGuideBegin.let { absValue ->
            if (isLtr) absValue
            else -absValue
        }
        selectedDragButtonTransitionX =
            if (isLtr) -checkboxTouchableWidth
            else checkboxTouchableWidth
    }

    private fun postAnimator(animator: Animator) {
        disposableAnimator = animator
        animator.start()
    }

    private fun disposeLatestAnimator() {
        disposableAnimator?.cancel()
        disposableAnimator = null
    }

    private fun postActionToContentLayout(action: Runnable) {
        disposableContentActions += action
        binding.layoutContent.post(action)
    }

    private fun disposeLatestContentLayoutAction() {
        disposableContentActions.forEach { binding.layoutContent.removeCallbacks(/* action = */ it) }
        disposableContentActions.clear()
    }

    private fun canAnimate(): Boolean {
        return binding.layoutContent.isAttachedToWindow
    }

    fun clearResources() {
        disposeLatestAnimator()
        disposeLatestContentLayoutAction()
    }

    fun startAnimation(selectable: Boolean) {
        latestSelectable = selectable

        val currentIsFirstAnimate = isFirstAnimate
        if (currentIsFirstAnimate) {
            isFirstAnimate = false
            if (selectable.not()) {
                return
            }
        }

        clearResources()

        if (canAnimate() && currentIsFirstAnimate.not()) {
            val animatorSet = AnimatorSet().setDuration(250).apply {
                playTogether(/* items = */ createAnimators(selectable))
                interpolator = FastOutSlowInInterpolator()
            }

            // Lock text view widths beforehand and register restore on animation end to optimize text measurement overhead
            if (binding.cardLink.isVisible) {
                val deltaWidth = selectedExtraContentGuideEnd - unselectedExtraContentGuideEnd
                val targetCardWidth = if (selectable) {
                    binding.cardLink.width - deltaWidth
                } else {
                    binding.cardLink.width + deltaWidth
                }
                // 46dp represents the non-text horizontal space occupied by margins and the browser icon in the XML layout:
                // - 10dp: textviewLink start margin
                // - 6dp: textviewLink end margin (spacing between link text and browser icon)
                // - 20dp: imageviewBrowser width
                // - 10dp: imageviewBrowser end margin
                val marginAndBrowserWidth = dpToPx(
                    46f,
                    binding.root.context.resources.displayMetrics
                ).toInt()
                val targetTextWidth = (targetCardWidth - marginAndBrowserWidth).coerceAtLeast(0)

                binding.textviewLink.setupFixedLayoutWidthDuringAnimation(targetTextWidth, animatorSet)
                binding.textviewTitleLink.setupFixedLayoutWidthDuringAnimation(targetTextWidth, animatorSet)
            }

            postAnimator(animator = animatorSet)
        } else {
            postActionToContentLayout { applyLayout(selectable) }
        }
    }

    // Start of configurations
    private fun applyLayout(selectable: Boolean) {
        restoreTextViewWidth()

        val titleParams = binding.edittextTitle.constraintLayoutParams
        titleParams.goneEndMargin = if (selectable) selectedExtraContentGuideEnd else 0
        binding.edittextTitle.layoutParams = titleParams

        val guideParams = binding.guidelineExtraContentEnd.constraintLayoutParams
        guideParams.guideEnd = getExtraContentGuideEnd(selectable)
        binding.guidelineExtraContentEnd.layoutParams = guideParams

        postActionToContentLayout {
            binding.buttonComplete.applyOrAnimate(getCompleteButtonTranslateX(selectable), getCompleteButtonAlpha(selectable))
            binding.buttonSelection.applyOrAnimate(getSelectionButtonTranslateX(selectable), getSelectionButtonAlpha(selectable))
            binding.buttonDragHandle.applyOrAnimate(getDragButtonTranslateX(selectable), getDragButtonAlpha(selectable))
        }
    }

    private fun restoreTextViewWidth() {
        if (binding.cardLink.isVisible) {
            binding.textviewLink.layoutParams = binding.textviewLink.layoutParams.apply {
                width = 0
            }
            binding.textviewTitleLink.layoutParams = binding.textviewTitleLink.layoutParams.apply {
                width = 0
            }
        }
    }

    private fun createAnimators(selectable: Boolean): List<Animator> = buildList {
        val titleParams = binding.edittextTitle.constraintLayoutParams
        titleParams.goneEndMargin = if (selectable) selectedExtraContentGuideEnd else 0
        binding.edittextTitle.layoutParams = titleParams

        add(binding.guidelineExtraContentEnd.guideEndAnimator(getExtraContentGuideEnd(selectable)))
        binding.buttonComplete.applyOrAnimate(getCompleteButtonTranslateX(selectable), getCompleteButtonAlpha(selectable), this)
        binding.buttonSelection.applyOrAnimate(getSelectionButtonTranslateX(selectable), getSelectionButtonAlpha(selectable), this)
        binding.buttonDragHandle.applyOrAnimate(getDragButtonTranslateX(selectable), getDragButtonAlpha(selectable), this)
    }

    // End of configurations

    // Start of extra content area
    private fun createExtraContentWidthTransformAnimator(selectable: Boolean): Animator {
        val titleParams = binding.edittextTitle.constraintLayoutParams
        titleParams.goneEndMargin = if (selectable) selectedExtraContentGuideEnd else 0
        binding.edittextTitle.layoutParams = titleParams

        return binding.guidelineExtraContentEnd.guideEndAnimator(getExtraContentGuideEnd(selectable))
    }

    private fun getExtraContentGuideEnd(selectable: Boolean): Int {
        return if (selectable) selectedExtraContentGuideEnd else unselectedExtraContentGuideEnd
    }
    // End of extra content area

    private fun getCompleteButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedCompleteButtonTranslationX.toFloat() else 0f
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getCompleteButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 0f else 1f
    }

    private fun getSelectionButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedSelectionButtonTranslationX.toFloat() else 0f
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getSelectionButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }

    private fun getDragButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedDragButtonTransitionX.toFloat() else 0f
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getDragButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }

    fun applyStateToMirror(mirrorBinding: LayoutScheduleAdapterItemContentMirrorBinding) {
        val selectable = binding.buttonDragHandle.translationX == getDragButtonTranslateX(selectable = true)

        val titleParams = mirrorBinding.edittextTitle.constraintLayoutParams
        titleParams.goneEndMargin = if (selectable) selectedExtraContentGuideEnd else 0
        mirrorBinding.edittextTitle.layoutParams = titleParams

        val guideParams = mirrorBinding.guidelineExtraContentEnd.constraintLayoutParams
        guideParams.guideEnd = getExtraContentGuideEnd(selectable)
        mirrorBinding.guidelineExtraContentEnd.layoutParams = guideParams

        mirrorBinding.buttonComplete.apply {
            alpha = binding.buttonComplete.alpha
            translationX = binding.buttonComplete.translationX
        }
        mirrorBinding.buttonSelection.apply {
            alpha = binding.buttonSelection.alpha
            translationX = binding.buttonSelection.translationX
        }
        mirrorBinding.buttonDragHandle.apply {
            alpha = binding.buttonDragHandle.alpha
            translationX = binding.buttonDragHandle.translationX
        }
    }
}

private val View.constraintLayoutParams: ConstraintLayout.LayoutParams
    get() = layoutParams as ConstraintLayout.LayoutParams

private fun Guideline.guideEndAnimator(targetGuideEnd: Int): ValueAnimator =
    ValueAnimator.ofInt(constraintLayoutParams.guideEnd, targetGuideEnd).apply {
        addUpdateListener {
            val params = constraintLayoutParams
            params.guideEnd = it.animatedValue as Int
            layoutParams = params
        }
    }

/**
 * Extension utilities to simplify View numeric animations
 */
private fun View.alphaAnimator(targetAlpha: Float): ValueAnimator =
    ValueAnimator.ofFloat(alpha, targetAlpha).apply {
        addUpdateListener { alpha = it.animatedValue as Float }
    }

private fun View.translationXAnimator(targetTranslationX: Float): ValueAnimator =
    ValueAnimator.ofFloat(translationX, targetTranslationX).apply {
        addUpdateListener { translationX = it.animatedValue as Float }
    }

private fun View.applyOrAnimate(
    targetTranslationX: Float,
    targetAlpha: Float,
    animators: MutableList<Animator>? = null
) {
    if (animators != null) {
        animators += translationXAnimator(targetTranslationX)
        animators += alphaAnimator(targetAlpha)
    } else {
        this.translationX = targetTranslationX
        this.alpha = targetAlpha
    }
}

/**
 * Lock the width of TextView (and its subclasses like EditText) to [targetWidth] during the animation.
 * Once the animation ends or is cancelled, restore its layout width back to [restoreWidth] (defaults to MATCH_CONSTRAINT).
 */
private fun TextView.setupFixedLayoutWidthDuringAnimation(
    targetWidth: Int,
    animator: Animator,
    restoreWidth: Int = 0 // MATCH_CONSTRAINT
) {
    if (targetWidth <= 0) return
    
    // Lock the width immediately at the start
    this.layoutParams = this.layoutParams.apply { width = targetWidth }
    
    // Use Android KTX extensions to restore layout width on animation end or cancellation
    animator.doOnEnd { layoutParams = layoutParams.apply { width = restoreWidth } }
    animator.doOnCancel { layoutParams = layoutParams.apply { width = restoreWidth } }
}