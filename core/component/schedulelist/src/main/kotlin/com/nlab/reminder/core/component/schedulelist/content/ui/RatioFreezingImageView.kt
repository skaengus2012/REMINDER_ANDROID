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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.roundToInt

/**
 * @author Thalys
 */
class RatioFreezingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    var myId: Int = 0

    // 높이 동결 상태 및 고정 높이 저장 변수
    private var fixedHeight: Int? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val currentWidth = MeasureSpec.getSize(widthMeasureSpec)

        if (fixedHeight == null && currentWidth > 0) {
            // 최초 계산: width의 0.4 비율로 fixedHeight를 설정
            fixedHeight = (currentWidth * HARDCODED_RATIO).toInt()
        }
        println("\"RatioFreezingView#${myId}\", \"Current Width (onMeasure): $currentWidth, Fixed Height: $fixedHeight ${layoutParams.width}")

        val heightToUse = fixedHeight ?: 0

        val finalHeightSpec = MeasureSpec.makeMeasureSpec(
            heightToUse,
            MeasureSpec.EXACTLY
        )

        super.onMeasure(widthMeasureSpec, finalHeightSpec)
    }

    companion object {

        // 하드코딩된 비율 (예: 1:0.4)
        private const val HARDCODED_RATIO = 0.4f
    }
}