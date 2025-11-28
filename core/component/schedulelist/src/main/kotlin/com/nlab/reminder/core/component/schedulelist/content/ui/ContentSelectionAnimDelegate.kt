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
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemContentBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

/**
 * @author Doohyun
 */
private var key: Int = 1

internal class ContentSelectionAnimDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) {
    private val constraintSetApplyAttachListener = object : View.OnAttachStateChangeListener {
        private val callback = Runnable {
            applyCompleteButtonTranslate(true)
            applyCompleteButtonAlpha(true)
            applySelectionButtonTranslateX(true)
            applySelectionButtonAlpha(true)
            applyDragButtonTranslateX(true)
            applyDragButtonAlpha(true)
        }

        override fun onViewAttachedToWindow(view: View) {
            selectedContentConstraintSet.applyTo(/* constraintLayout = */ binding.layoutContent)
            binding.layoutContent.post(callback)
        }

        override fun onViewDetachedFromWindow(view: View) {
            binding.root.removeOnAttachStateChangeListener(/* listener = */ this)
            binding.layoutContent.removeCallbacks(callback)
        }
    }
    private val animatorDisposeAttachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit
        override fun onViewDetachedFromWindow(view: View) {
            latestAnimator?.cancel()
        }
    }

    private lateinit var selectedContentConstraintSet: ConstraintSet
    private lateinit var unselectedContentConstraintSet: ConstraintSet

    private var selectedCardLinkWidth: Int = 0
    private var unselectedCardLinkWidth: Int = 0

    private var latestAnimator: Animator? = null

    private var selectedCompleteButtonTranslationX: Float = 0f
    private var selectedSelectionButtonTranslationX: Float = 0f
    private var selectedDragButtonTransitionX: Float = 0f

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

        selectedContentConstraintSet = ConstraintSet().apply {
            clone(/* constraintLayout = */ binding.layoutContent)
            constrainWidth(
                /* viewId = */ binding.edittextTitle.id,
                /* width = */  binding.edittextTitle.width - dataSelectedGuideEnd
            )
            constrainWidth(
                /* viewId = */ binding.cardLink.id,
                /* width = */ selectedCardLinkWidth
            )
        }
        unselectedContentConstraintSet = ConstraintSet().apply { clone(/* constraintLayout = */ binding.layoutContent) }

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
    }

    fun startAnimation(selectable: Boolean) {
        val rootLayout = binding.root
        rootLayout.removeOnAttachStateChangeListener(/* listener = */ constraintSetApplyAttachListener)
        rootLayout.removeOnAttachStateChangeListener(/* listener = */ animatorDisposeAttachListener)

        if (binding.root.isAttachedToWindow.not()) {
            if (selectable) {
                rootLayout.addOnAttachStateChangeListener(/* listener = */ constraintSetApplyAttachListener)
            }
            return
        }

        val animatorItems = listOf(
            createDateContentWidthTransformAnimator(selectable),
            createCompleteButtonTranslateAnimator(selectable),
            createCompleteButtonAlphaAnimator(selectable),
            createSelectionButtonTranslateAnimator(selectable),
            createSelectionButtonAlphaAnimator(selectable),
            createDragButtonTranslateAnimator(selectable),
            createDragButtonAlphaAnimator(selectable),
        )
        val animator = AnimatorSet()
            .setDuration(250)
            .apply {
                playTogether(animatorItems)
                interpolator = FastOutSlowInInterpolator()
            }
        latestAnimator?.cancel()
        latestAnimator = animator
        rootLayout.addOnAttachStateChangeListener(/* listener = */ animatorDisposeAttachListener)
        animator.start()

        /**
        if (canAnimate.not()) {
            canAnimate = true
            applyLayout(selectable)
        } else {
            val animatorItems = listOf(
                createDateContentWidthTransformAnimator(selectable),
                createCompleteButtonTranslateAnimator(selectable),
                createCompleteButtonAlphaAnimator(selectable),
                createSelectionButtonTranslateAnimator(selectable),
                createSelectionButtonAlphaAnimator(selectable),
                createDragButtonTranslateAnimator(selectable),
                createDragButtonAlphaAnimator(selectable),
            )
            val animator = AnimatorSet()
                .setDuration(250)
                .apply {
                    playTogether(animatorItems)
                    interpolator = FastOutSlowInInterpolator()
                }
                .also { latestAnimator = it }
            val root = binding.root
            val attachStateChangeListener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = Unit
                override fun onViewDetachedFromWindow(v: View) {
                    animator.cancel()
                    root.removeOnAttachStateChangeListener(/*listener = */ this)
                }
            }
            root.addOnAttachStateChangeListener(attachStateChangeListener)
            animator.start()
        }*/
    }


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

private fun View.needWidthUpdate(newWidth: Int): Boolean {
    return abs(width - newWidth) > 1
}

private fun View.updateWidthIfChanged(newWidth: Int): Boolean {
    val needUpdate = abs(width - newWidth) > 1
    if (needUpdate) {
        updateLayoutParams { width = newWidth }
    }
    return needUpdate
}