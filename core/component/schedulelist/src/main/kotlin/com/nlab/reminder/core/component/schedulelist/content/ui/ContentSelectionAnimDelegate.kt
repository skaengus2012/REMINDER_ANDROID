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
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
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
            applyCompleteButtonTranslate(selectable)
            applyCompleteButtonAlpha(selectable)
            applySelectionButtonTranslateX(selectable)
            applySelectionButtonAlpha(selectable)
            applyDragButtonTranslateX(selectable)
            applyDragButtonAlpha(selectable)
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

    private fun createAnimators(selectable: Boolean): List<Animator> = listOf(
        createExtraContentWidthTransformAnimator(selectable),
        createCompleteButtonTranslateAnimator(selectable),
        createCompleteButtonAlphaAnimator(selectable),
        createSelectionButtonTranslateAnimator(selectable),
        createSelectionButtonAlphaAnimator(selectable),
        createDragButtonTranslateAnimator(selectable),
        createDragButtonAlphaAnimator(selectable),
    )

    // End of configurations

    // Start of extra content area
    private fun createExtraContentWidthTransformAnimator(selectable: Boolean): Animator {
        val currentMediaGuideEnd = binding.guidelineExtraContentEnd
            .constraintLayoutParams
            .guideEnd

        val titleParams = binding.edittextTitle.constraintLayoutParams
        titleParams.goneEndMargin = if (selectable) selectedExtraContentGuideEnd else 0
        binding.edittextTitle.layoutParams = titleParams

        return ValueAnimator.ofInt(currentMediaGuideEnd, getExtraContentGuideEnd(selectable)).apply {
            addUpdateListener { value ->
                val params = binding.guidelineExtraContentEnd.constraintLayoutParams
                params.guideEnd = value.animatedValue as Int
                binding.guidelineExtraContentEnd.layoutParams = params
            }
        }
    }

    private fun getExtraContentGuideEnd(selectable: Boolean): Int {
        return if (selectable) selectedExtraContentGuideEnd else unselectedExtraContentGuideEnd
    }
    // End of extra content area

    // Start of CompleteButton TranslateX area
    private fun createCompleteButtonTranslateAnimator(selectable: Boolean): Animator =
        binding.buttonComplete.translationXAnimator(getCompleteButtonTranslateX(selectable))

    private fun applyCompleteButtonTranslate(selectable: Boolean) {
        binding.buttonComplete.translationX = getCompleteButtonTranslateX(selectable)
    }

    private fun getCompleteButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedCompleteButtonTranslationX.toFloat() else 0f
    }
    // End of CompleteButton TranslateX area

    // Start of CompleteButton Alpha area
    private fun createCompleteButtonAlphaAnimator(selectable: Boolean): Animator =
        binding.buttonComplete.alphaAnimator(getCompleteButtonAlpha(selectable))

    private fun applyCompleteButtonAlpha(selectable: Boolean) {
        binding.buttonComplete.alpha = getCompleteButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getCompleteButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 0f else 1f
    }
    // End of CompleteButton Alpha area

    // Start of SelectionButton TranslateX area
    private fun createSelectionButtonTranslateAnimator(selectable: Boolean): Animator =
        binding.buttonSelection.translationXAnimator(getSelectionButtonTranslateX(selectable))

    private fun applySelectionButtonTranslateX(selectable: Boolean) {
        binding.buttonSelection.translationX = getSelectionButtonTranslateX(selectable)
    }

    private fun getSelectionButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedSelectionButtonTranslationX.toFloat() else 0f
    }
    // End of SelectionButton TranslateX area

    // Start of SelectionButton Alpha area
    private fun createSelectionButtonAlphaAnimator(selectable: Boolean): Animator =
        binding.buttonSelection.alphaAnimator(getSelectionButtonAlpha(selectable))

    private fun applySelectionButtonAlpha(selectable: Boolean) {
        binding.buttonSelection.alpha = getSelectionButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getSelectionButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }
    // End of SelectionButton Alpha area

    // Start of DragButton TranslateX area
    private fun createDragButtonTranslateAnimator(selectable: Boolean): Animator =
        binding.buttonDragHandle.translationXAnimator(getDragButtonTranslateX(selectable))

    private fun applyDragButtonTranslateX(selectable: Boolean) {
        binding.buttonDragHandle.translationX = getDragButtonTranslateX(selectable)
    }

    private fun getDragButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedDragButtonTransitionX.toFloat() else 0f
    }
    // End of DragButton TranslateX area

    // Start of DragButton Alpha area
    private fun createDragButtonAlphaAnimator(selectable: Boolean): Animator =
        binding.buttonDragHandle.alphaAnimator(getDragButtonAlpha(selectable))

    private fun applyDragButtonAlpha(selectable: Boolean) {
        binding.buttonDragHandle.alpha = getDragButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getDragButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }
    // End of DragButton Alpha area

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
    
    // Register a listener to restore the width on animation end or cancellation
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {
            this@setupFixedLayoutWidthDuringAnimation.layoutParams = 
                this@setupFixedLayoutWidthDuringAnimation.layoutParams.apply { width = restoreWidth }
        }
        override fun onAnimationEnd(animation: Animator) {
            this@setupFixedLayoutWidthDuringAnimation.layoutParams = 
                this@setupFixedLayoutWidthDuringAnimation.layoutParams.apply { width = restoreWidth }
        }
    })
}