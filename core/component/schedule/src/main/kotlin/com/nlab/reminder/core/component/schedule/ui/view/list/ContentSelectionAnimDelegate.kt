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

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleAdapterItemContentBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Doohyun
 */
internal class ContentSelectionAnimDelegate(
    private val binding: LayoutScheduleAdapterItemContentBinding,
) {
    private var latestAnimators = emptySet<Animator>()
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

    private inline fun animatorTransaction(block: (duration: Long) -> Set<Animator>) {
        cancelAnimation()
        val duration = if (latestAnimators.isEmpty()) 0L else 250L
        val newAnimators = block(duration)
        newAnimators.forEach { it.start() }
        latestAnimators = newAnimators
    }

    fun startAnimation(selectable: Boolean) = animatorTransaction { duration ->
        setOf(
            createDataLayoutAnimator(selectable, duration),
            createCompleteButtonTranslateAnimator(selectable, duration),
            createCompleteButtonAlphaAnimator(selectable, duration),
            createSelectionButtonTranslateAnimator(selectable, duration),
            createSelectionButtonAlphaAnimator(selectable, duration),
            createDragButtonTranslateAnimator(selectable, duration),
            createDragButtonAlphaAnimator(selectable, duration)
        )
    }

    fun cancelAnimation() {
        latestAnimators.forEach { it.cancel() }
    }

    private fun createDataLayoutAnimator(selectable: Boolean, duration: Long): Animator {
        val view = binding.layoutData
        val animator = ValueAnimator.ofInt(
            view.width,
            if (selectable) selectedDataLayoutWidth
            else unselectedDataLayoutWidth
        )
        animator.duration = duration
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { value ->
            view.updateLayoutParams<LayoutParams> { width = value.animatedValue as Int }
        }
        return animator
    }

    private fun createCompleteButtonTranslateAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonComplete
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            if (selectable) selectedCompleteButtonTranslationX
            else 0f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun createCompleteButtonAlphaAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonComplete
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            if (selectable) 0f else 1f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }

    private fun createSelectionButtonTranslateAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonSelection
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            if (selectable) selectedSelectionButtonTranslationX
            else 0f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun createSelectionButtonAlphaAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonSelection
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            if (selectable) 1f
            else 0f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }

    private fun createDragButtonTranslateAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonDragHandle
        val animator = ValueAnimator.ofFloat(
            targetView.translationX,
            if (selectable) selectedDragButtonTransitionX
            else 0f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.translationX = value.animatedValue as Float
        }
        return animator
    }

    private fun createDragButtonAlphaAnimator(selectable: Boolean, duration: Long): Animator {
        val targetView = binding.buttonDragHandle
        val animator = ValueAnimator.ofFloat(
            targetView.alpha,
            if (selectable) 1f
            else 0f
        )
        animator.duration = duration
        animator.addUpdateListener { value ->
            targetView.alpha = value.animatedValue as Float
        }
        return animator
    }
}