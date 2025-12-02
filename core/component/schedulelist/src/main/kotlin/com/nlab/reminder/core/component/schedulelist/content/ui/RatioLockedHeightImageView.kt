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
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import com.nlab.reminder.core.component.schedulelist.R

/**
 * @author Thalys
 */
internal class RatioLockedHeightImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    @FloatRange(from = 0.0, to = 1.0)
    private var heightRatio: Float = 1f

    // Height freezing status and variable to store the fixed height
    private var fixedHeight: Int = -1

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                /* set = */ it,
                /* attrs = */ R.styleable.RatioLockedHeightImageView
            )
            heightRatio = typedArray.getFloat(
                /* index = */ R.styleable.RatioLockedHeightImageView_heightRatio,
                /* defValue = */ 1f
            )
            require(heightRatio in 0.0f..1.0f) {
                "The height ratio must be between 0.0 and 1.0, but was $heightRatio"
            }

            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val currentWidth = MeasureSpec.getSize(widthMeasureSpec)

        if (fixedHeight == -1 && currentWidth > 0) {
            // Initial calculation: Set fixedHeight based on heightRatio of the width
            fixedHeight = (currentWidth * heightRatio).toInt()
        }

        val modifiedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
            /* size = */ fixedHeight,
            /* mode = */ MeasureSpec.EXACTLY
        )
        super.onMeasure(widthMeasureSpec, modifiedHeightMeasureSpec)
    }
}