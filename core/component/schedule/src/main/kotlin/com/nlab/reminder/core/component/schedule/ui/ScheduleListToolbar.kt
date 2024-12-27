/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedule.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import com.nlab.reminder.core.component.schedule.databinding.LayoutScheduleListToolbarBinding
import com.nlab.reminder.core.component.schedule.R


/**
 * @author Doohyun
 */
class ScheduleListToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding = LayoutScheduleListToolbarBinding.inflate(LayoutInflater.from(context), /*parent=*/ this)

    var titleText: CharSequence?
        get() = binding.textviewToolbarTitle.text
        set(value) {
            binding.textviewToolbarTitle.text = value
        }

    @get:FloatRange(from = 0.0, to = 1.0)
    var titleAlpha: Float
        get() = binding.textviewToolbarTitle.alpha
        set(value) {
            binding.textviewToolbarTitle.alpha = value
        }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ScheduleListToolbar)
            titleText = typedArray.getString(R.styleable.ScheduleListToolbar_titleText)

            typedArray.recycle()
        }
    }

    fun setTitle(@StringRes titleRes: Int) {
        binding.textviewToolbarTitle.setText(titleRes)
    }

    fun setTitleAlpha() {
        binding.textviewToolbarTitle.alpha
    }

    fun setOnBackClickListener(listener: OnClickListener) {
        binding.buttonBack.setOnClickListener(listener)
    }

    fun setOnMoreClickListener(listener: OnClickListener) {
        binding.buttonMore.setOnClickListener(listener)
    }
}