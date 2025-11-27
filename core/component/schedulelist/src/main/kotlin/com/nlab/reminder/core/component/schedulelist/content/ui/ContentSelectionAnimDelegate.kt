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
import androidx.core.view.doOnDetach
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
internal class ContentSelectionAnimDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) {
    private var canAnimate: Boolean = false
    private var latestAnimator: Animator? = null
    private var selectedDataLayoutWidth: Int = 0
    private var unselectedDataLayoutWidth: Int = 0
    private var selectedCompleteButtonTranslationX: Float = 0f
    private var selectedSelectionButtonTranslationX: Float = 0f
    private var selectedDragButtonTransitionX: Float = 0f

    suspend fun awaitReady() {
        suspendCancellableCoroutine { cons ->
            binding.layoutContent.doOnLayout {
                cons.resume(Unit)
            }
        }
        val isLtr = binding.root.layoutDirection.let { direction -> direction == View.LAYOUT_DIRECTION_LTR }

        selectedDataLayoutWidth =
            (binding.layoutData.width - binding.viewGuideLayoutDataSelectedEnd.width)
                .coerceAtLeast(minimumValue = 0)
        unselectedDataLayoutWidth = binding.layoutData.width
        selectedCompleteButtonTranslationX =
            (binding.viewGuideCompletionSelectedStart.width - binding.buttonComplete.width)
                .coerceAtLeast(minimumValue = 0)
                .toFloat()
                .let { absValue ->
                    if (isLtr) absValue
                    else -absValue
                }
        selectedSelectionButtonTranslationX =
            binding.viewGuideBodyStart.width
                .toFloat()
                .let { absValue ->
                    if (isLtr) absValue
                    else -absValue
                }
        selectedDragButtonTransitionX =
            -binding.viewGuideDragSelectedStart.width
                .toFloat()
                .let { absValue ->
                    if (isLtr) absValue
                    else -absValue
                }
    }

    fun startAnimation(selectable: Boolean) {
        if (canAnimate.not()) {
            canAnimate = true
            applyDataLayout(selectable)
            applyCompleteButtonTranslate(selectable)
            applyCompleteButtonAlpha(selectable)
            applySelectionButtonTranslateX(selectable)
            applySelectionButtonAlpha(selectable)
            applyDragButtonTranslateX(selectable)
            applyDragButtonAlpha(selectable)
        } else {
            cancelAnimation()
            val animatorItems = listOf(
                createDataLayoutAnimator(selectable),
                createCompleteButtonTranslateAnimator(selectable),
                createCompleteButtonAlphaAnimator(selectable),
                createSelectionButtonTranslateAnimator(selectable),
                createSelectionButtonAlphaAnimator(selectable),
                createDragButtonTranslateAnimator(selectable),
                createDragButtonAlphaAnimator(selectable),
            )
            AnimatorSet()
                .setDuration(250)
                .apply {
                    playTogether(animatorItems)
                    interpolator = FastOutSlowInInterpolator()
                }
                .also { latestAnimator = it }
                .start()
        }
    }

    fun cancelAnimation() {
        latestAnimator?.cancel()
    }

    // Start of DataLayout area
    private fun createDataLayoutAnimator(selectable: Boolean): Animator {
        val view = binding.layoutData
        val animator = ValueAnimator.ofInt(view.width, getDataLayoutWidth(selectable))
        animator.addUpdateListener { value ->
            val newWidth = value.animatedValue as Int
            val currentWidth = view.layoutParams.width
            if (currentWidth != newWidth) {
                view.updateLayoutParams { width = newWidth }
            }
        }
        return animator
    }

    private fun applyDataLayout(selectable: Boolean) {
        val view = binding.layoutData
        val currentWidth = view.width
        val newWidth = getDataLayoutWidth(selectable)
        val needUpdate = abs(currentWidth - newWidth) > 1
        if (needUpdate) {
            val callback = Runnable {
                view.updateLayoutParams { width = newWidth }
            }
            view.post(/* action = */ callback)
            view.doOnDetach { v -> v.removeCallbacks(/* action = */ callback) }
        }
    }

    private fun getDataLayoutWidth(selectable: Boolean): Int {
        return if (selectable) selectedDataLayoutWidth else unselectedDataLayoutWidth
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
        layoutData.updateLayoutParams { width = binding.layoutData.width }
    }
}