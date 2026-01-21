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
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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
    private val visibilityStateDecorator = VisibilityStateDecorator(binding)

    private lateinit var unselectedContentConstraintSet: ConstraintSet
    private lateinit var selectedContentConstraintSet: ConstraintSet

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
        unselectedContentConstraintSet = ConstraintSet().apply {
            clone(/* constraintLayout = */ binding.layoutContent)
            setVisibility(
                /* viewId = */ binding.buttonInfo.id,
                /* visibility = */ ConstraintSet.GONE
            )
        }
        selectedContentConstraintSet = ConstraintSet().apply {
            clone(unselectedContentConstraintSet)
            setGoneMargin(
                /* viewId = */ binding.edittextTitle.id,
                /* anchor = */ ConstraintSet.END,
                /* value = */ checkboxTouchableWidth
            )
            setGuidelineEnd(
                /* guidelineID = */ binding.guidelineExtraContentEnd.id,
                /* margin = */ selectedExtraContentGuideEnd
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
        ConstraintSet()
            .apply {
                clone(/* set = */  getContentConstraintSet(selectable))
                visibilityStateDecorator.decorateTo(constraintSet = this)
            }
            .applyTo(/* constraintLayout = */ binding.layoutContent)
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
        createExtraContentWidthTransformAnimator(selectable),
        createCompleteButtonTranslateAnimator(selectable),
        createCompleteButtonAlphaAnimator(selectable),
        createSelectionButtonTranslateAnimator(selectable),
        createSelectionButtonAlphaAnimator(selectable),
        createDragButtonTranslateAnimator(selectable),
        createDragButtonAlphaAnimator(selectable),
    )

    private fun getContentConstraintSet(selectable: Boolean): ConstraintSet {
        return if (selectable) selectedContentConstraintSet else unselectedContentConstraintSet
    }

    // End of configurations

    // Start of extra content area
    private fun createExtraContentWidthTransformAnimator(selectable: Boolean): Animator {
        val constraintSet = ConstraintSet().apply { clone(/* set = */ getContentConstraintSet(selectable)) }
        val currentMediaGuideEnd = binding.guidelineExtraContentEnd
            .constraintLayoutParams
            .guideEnd
        return ValueAnimator.ofInt(currentMediaGuideEnd, getExtraContentGuideEnd(selectable)).apply {
            addUpdateListener { value ->
                visibilityStateDecorator.decorateTo(constraintSet)
                constraintSet.setGuidelineEnd(
                    /* guidelineID = */ binding.guidelineExtraContentEnd.id,
                    /* margin = */ value.animatedValue as Int
                )
                constraintSet.applyTo(/* constraintLayout = */ binding.layoutContent)
            }
        }
    }

    private fun getExtraContentGuideEnd(selectable: Boolean): Int {
        return if (selectable) selectedExtraContentGuideEnd else unselectedExtraContentGuideEnd
    }
    // End of extra content area

    // Start of CompleteButton TranslateX area
    private fun createCompleteButtonTranslateAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonComplete
        return ValueAnimator
            .ofFloat(targetView.translationX, getCompleteButtonTranslateX(selectable))
            .apply {
                addUpdateListener { targetView.translationX = it.animatedValue as Float }
            }
    }

    private fun applyCompleteButtonTranslate(selectable: Boolean) {
        binding.buttonComplete.translationX = getCompleteButtonTranslateX(selectable)
    }

    private fun getCompleteButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedCompleteButtonTranslationX.toFloat() else 0f
    }
    // End of CompleteButton TranslateX area

    // Start of CompleteButton Alpha area
    private fun createCompleteButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonComplete
        return ValueAnimator
            .ofFloat(targetView.alpha, getCompleteButtonAlpha(selectable))
            .apply {
                addUpdateListener { targetView.alpha = it.animatedValue as Float }
            }
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
        return ValueAnimator
            .ofFloat(targetView.translationX, getSelectionButtonTranslateX(selectable))
            .apply {
                addUpdateListener { targetView.translationX = it.animatedValue as Float }
            }
    }

    private fun applySelectionButtonTranslateX(selectable: Boolean) {
        binding.buttonSelection.translationX = getSelectionButtonTranslateX(selectable)
    }

    private fun getSelectionButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedSelectionButtonTranslationX.toFloat() else 0f
    }
    // End of SelectionButton TranslateX area

    // Start of SelectionButton Alpha area
    private fun createSelectionButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonSelection
        return ValueAnimator
            .ofFloat(targetView.alpha, getSelectionButtonAlpha(selectable))
            .apply {
                addUpdateListener { targetView.alpha = it.animatedValue as Float }
            }
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
        return ValueAnimator
            .ofFloat(targetView.translationX, getDragButtonTranslateX(selectable))
            .apply {
                addUpdateListener { targetView.translationX = it.animatedValue as Float }
            }
    }

    private fun applyDragButtonTranslateX(selectable: Boolean) {
        binding.buttonDragHandle.translationX = getDragButtonTranslateX(selectable)
    }

    private fun getDragButtonTranslateX(selectable: Boolean): Float {
        return if (selectable) selectedDragButtonTransitionX.toFloat() else 0f
    }
    // End of DragButton TranslateX area

    // Start of DragButton Alpha area
    private fun createDragButtonAlphaAnimator(selectable: Boolean): Animator {
        val targetView = binding.buttonDragHandle
        return ValueAnimator
            .ofFloat(targetView.alpha, getDragButtonAlpha(selectable))
            .apply {
                addUpdateListener { targetView.alpha = it.animatedValue as Float }
            }
    }

    private fun applyDragButtonAlpha(selectable: Boolean) {
        binding.buttonDragHandle.alpha = getDragButtonAlpha(selectable)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun getDragButtonAlpha(selectable: Boolean): Float {
        return if (selectable) 1f else 0f
    }
    // End of DragButton Alpha area

    fun applyStateToMirror(mirrorBinding: LayoutScheduleAdapterItemContentMirrorBinding) {
        getContentConstraintSet(
            selectable = binding.buttonDragHandle.translationX == getDragButtonTranslateX(selectable = true)
        ).apply { visibilityStateDecorator.decorateTo(constraintSet = this) }
            .applyTo(/* constraintLayout = */ mirrorBinding.layoutContent)

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

private class VisibilityStateDecorator(binding: LayoutScheduleAdapterItemContentBinding) {
    private val targets = listOf(
        binding.edittextNote,
        binding.edittextDetail,
        binding.cardLink
    )

    fun decorateTo(constraintSet: ConstraintSet) {
        targets.forEach { v ->
            constraintSet.setVisibility(/* viewId = */ v.id, /* visibility = */ v.visibility)
        }
    }
}

private val View.constraintLayoutParams: ConstraintLayout.LayoutParams
    get() = layoutParams as ConstraintLayout.LayoutParams