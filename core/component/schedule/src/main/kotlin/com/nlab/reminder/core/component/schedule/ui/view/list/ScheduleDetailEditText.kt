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

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import kotlin.math.max

/**
 * @author Doohyun
 */
class ScheduleDetailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var dateText: CharSequence? = null
    private var isExpired: Boolean? = null

    private var myText: String = "Hello"

    init {
        isHapticFeedbackEnabled = false
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        try {
            if (selStart < myText.length) {
                setSelection(myText.length, max(myText.length, selEnd))
            } else {
                super.onSelectionChanged(selStart, selEnd)
            }
        } catch (t: Throwable) {
            // OnSelectionChanged can be called before class creation.
            super.onSelectionChanged(selStart, selEnd)
        }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return when (id) {
            android.R.id.selectAll -> {
                // 사용자가 전체 선택했을 때, 우리가 원하는 범위만 선택
                val textLength = text?.length ?: 0
                if (textLength >  myText.length) {
                    setSelection( myText.length, textLength)
                }
                true // 기본 처리 막기
            }
            else -> super.onTextContextMenuItem(id)
        }
    }
}