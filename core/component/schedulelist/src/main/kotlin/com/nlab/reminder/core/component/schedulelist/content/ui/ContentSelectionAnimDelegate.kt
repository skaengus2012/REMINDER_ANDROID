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
import android.widget.ImageButton
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentBinding
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Doohyun
 */
internal class ContentSelectionAnimDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) {
    private lateinit var selectedContentConstraintSet: ConstraintSet
    private lateinit var unselectedContentConstraintSet: ConstraintSet

    private var selectedCardLinkWidth: Int = 0
    private var unselectedCardLinkWidth: Int = 0

    private var selectedCompleteButtonTranslationX: Float = 0f
    private var selectedSelectionButtonTranslationX: Float = 0f
    private var selectedDragButtonTransitionX: Float = 0f

    private val disposableContentActions = mutableListOf<Runnable>()
    private var disposableAnimator: Animator? = null

    private var isFirstAnimate: Boolean = true

    suspend fun awaitReady() {
        if (binding.layoutContent.isLaidOut) return

        // await layout
        suspendCancellableCoroutine { cons ->
            binding.layoutContent.doOnLayout {
                cons.resume(Unit)
            }
        }

        val isLtr = binding.root.layoutDirection.let { direction -> direction == View.LAYOUT_DIRECTION_LTR }
        val bodyStartGuideBegin = binding.guidelineBodyStart
            .constraintLayoutParams
            .guideBegin
            .toFloat()
        val dataSelectedGuideEnd = binding.guidelineDataSelectedEnd
            .constraintLayoutParams
            .guideEnd

        selectedCardLinkWidth = binding.cardLink.width - dataSelectedGuideEnd
        unselectedCardLinkWidth = binding.cardLink.width // match constraint
        selectedCompleteButtonTranslationX = with(binding) {
            val completionSelectedGuideBegin = binding.guidelineCompletionSelectedStart
                .constraintLayoutParams
                .guideBegin
            val diff = (completionSelectedGuideBegin - bodyStartGuideBegin).coerceAtLeast(minimumValue = 0f)
            if (isLtr) diff
            else -diff
        }
        selectedSelectionButtonTranslationX = bodyStartGuideBegin.let { absValue ->
            if (isLtr) absValue
            else -absValue
        }
        selectedDragButtonTransitionX = binding.guidelineDragSelectedStart.constraintLayoutParams
            .guideEnd
            .toFloat()
            .let { absValue ->
                if (isLtr) -absValue
                else absValue
            }

        unselectedContentConstraintSet = ConstraintSet().apply { clone(/* constraintLayout = */ binding.layoutContent) }
        selectedContentConstraintSet = ConstraintSet().apply {
            clone(unselectedContentConstraintSet)

            constrainWidth(
                /* viewId = */ binding.edittextTitle.id,
                /* width = */  binding.edittextTitle.width - dataSelectedGuideEnd
            )
            constrainWidth(
                /* viewId = */ binding.cardLink.id,
                /* width = */ selectedCardLinkWidth
            )
        }
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
        val currentIsFirstAnimate = isFirstAnimate
        if (currentIsFirstAnimate) {
            isFirstAnimate = false
            if (selectable.not()) {
                return
            }
        }

        clearResources()

        if (canAnimate() && currentIsFirstAnimate.not()) {
            postAnimator(
                animator = AnimatorSet().setDuration(250).apply {
                    playTogether(/* items = */ createAnimators(selectable))
                    interpolator = FastOutSlowInInterpolator()
                }
            )
        } else {
            postActionToContentLayout { applyLayout(selectable) }
        }
    }

    // Start of configurations
    private fun applyLayout(selectable: Boolean) {
        val constraintSet = if (selectable) selectedContentConstraintSet else unselectedContentConstraintSet
        constraintSet.applyTo(/* constraintLayout = */ binding.layoutContent)
        postActionToContentLayout {
            applyCompleteButtonTranslate(selectable)
            applyCompleteButtonAlpha(selectable)
            applySelectionButtonTranslateX(selectable)
            applySelectionButtonAlpha(selectable)
            applyDragButtonTranslateX(selectable)
            applyDragButtonAlpha(selectable)
        }
    }

    private fun createAnimators(selectable: Boolean): List<Animator> = listOf(
        createDateContentWidthTransformAnimator(selectable),
        createCompleteButtonTranslateAnimator(selectable),
        createCompleteButtonAlphaAnimator(selectable),
        createSelectionButtonTranslateAnimator(selectable),
        createSelectionButtonAlphaAnimator(selectable),
        createDragButtonTranslateAnimator(selectable),
        createDragButtonAlphaAnimator(selectable),
    )
    // End of configurations

    // Start of DataLayout area
    private fun createDateContentWidthTransformAnimator(selectable: Boolean): Animator {
        val constraintSet = ConstraintSet().apply {
            clone(if (selectable) selectedContentConstraintSet else unselectedContentConstraintSet)
        }
        return ValueAnimator.ofInt(binding.cardLink.width, getCardLinkWidth(selectable)).apply {
            addUpdateListener { value ->
                constraintSet.constrainWidth(
                    /* viewId = */ binding.cardLink.id,
                    /* width = */ value.animatedValue as Int
                )
                constraintSet.applyTo(/* constraintLayout = */ binding.layoutContent)
            }
        }
    }

    private fun getCardLinkWidth(selectable: Boolean): Int {
        return if (selectable) selectedCardLinkWidth else unselectedCardLinkWidth
    }
    // End of DataLayout area

    // Start of CompleteButton TranslateX area
    private fun createCompleteButtonTranslateAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonComplete
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            getCompleteButtonTranslateX(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun applyCompleteButtonTranslate(selectable: Boolean) {
        binding.buttonComplete.translationX = getCompleteButtonTranslateX(selectable)
    }

    private fun getCompleteButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedCompleteButtonTranslationX else 0f
    }
    // End of CompleteButton TranslateX area

    // Start of CompleteButton Alpha area
    private fun createCompleteButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonComplete
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            getCompleteButtonAlpha(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }

    private fun applyCompleteButtonAlpha(selectable: Boolean) {
        binding.buttonComplete.alpha = getCompleteButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getCompleteButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 0f else 1f
    }
    // End of CompleteButton Alpha area

    // Start of SelectionButton TranslateX area
    private fun createSelectionButtonTranslateAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonSelection
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            getSelectionButtonTranslateX(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun applySelectionButtonTranslateX(selectable: Boolean) {
        binding.buttonSelection.translationX = getSelectionButtonTranslateX(selectable)
    }

    private fun getSelectionButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedSelectionButtonTranslationX else 0f
    }
    // End of SelectionButton TranslateX area

    // Start of SelectionButton Alpha area
    private fun createSelectionButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonSelection
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            getSelectionButtonAlpha(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }

    private fun applySelectionButtonAlpha(selectable: Boolean) {
        binding.buttonSelection.alpha = getSelectionButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getSelectionButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }
    // End of SelectionButton Alpha area

    // Start of DragButton TranslateX area
    private fun createDragButtonTranslateAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonDragHandle
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            getDragButtonTranslateX(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun applyDragButtonTranslateX(selectable: Boolean) {
        binding.buttonDragHandle.translationX = getDragButtonTranslateX(selectable)
    }

    private fun getDragButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedDragButtonTransitionX else 0f
    }
    // End of DragButton TranslateX area

    // Start of DragButton Alpha area
    private fun createDragButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonDragHandle
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            getDragButtonAlpha(selectable)
        )
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }

    private fun applyDragButtonAlpha(selectable: Boolean) {
        binding.buttonDragHandle.alpha = getDragButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getDragButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }
    // End of DragButton Alpha area

    fun applyStateTo(
        layoutData: View,
        buttonComplete: ImageButton,
        buttonSelection: ImageButton,
        buttonDragHandle: ImageButton
    ) {
        buttonComplete.apply {
            isSelected = binding.buttonComplete.isSelected
            alpha = binding.buttonComplete.alpha
            translationX = binding.buttonComplete.translationX
        }
        buttonSelection.apply {
            isSelected = binding.buttonSelection.isSelected
            alpha = binding.buttonSelection.alpha
            translationX = binding.buttonSelection.translationX
        }
        buttonDragHandle.apply {
            alpha = binding.buttonDragHandle.alpha
            translationX = binding.buttonDragHandle.translationX
        }
     //   layoutData.updateLayoutParams { width = binding.layoutData.width }
    }
}

private val View.constraintLayoutParams: ConstraintLayout.LayoutParams
    get() = layoutParams as ConstraintLayout.LayoutParams