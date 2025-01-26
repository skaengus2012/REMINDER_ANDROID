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
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.component.schedule.ui.ScheduleListTimingFormatter
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max

/**
 * @author Doohyun
 */
class ScheduleDetailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var scheduleListTimingFormatter: ScheduleListTimingFormatter? = null
    private var scheduleTiming: ScheduleTiming? = null
    private var timeZone: TimeZone? = null
    private var entryAt: Instant? = null
    private var displayTimingText: CharSequence = ""

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

    fun bindScheduleListTimingFormatter(scheduleListTimingFormatter: ScheduleListTimingFormatter) {
        if (this.scheduleListTimingFormatter === scheduleListTimingFormatter) return
        // TODO implement
    }

    fun bindTimeZone(timeZone: TimeZone) {
        if (this.timeZone == timeZone) return
        // TODO implement
    }

    fun bindEntryAt(entryAt: Instant) {
        if (this.entryAt == entryAt) return
    }

    fun bindScheduleTiming(scheduleTiming: ScheduleTiming) {
        if (this.scheduleTiming == scheduleTiming) return
    }

    private fun invalidateDisplayTimingText() {

    }

    private fun getDisplayTriggerAt(): CharSequence {
        val curScheduleListTimingFormatter = scheduleListTimingFormatter
        val curScheduleTiming = scheduleTiming
        val curTimeZone = timeZone
        val curEntryAt = entryAt
        if (curScheduleListTimingFormatter == null
            || curScheduleTiming == null
            || curTimeZone == null
            || curEntryAt == null
        ) return ""

        val entryAtAsLocalDateTime = curEntryAt.toLocalDateTime(curTimeZone)
        val triggerAtAsLocalDateTime = curScheduleTiming.triggerAt.toLocalDateTime(curTimeZone)
        val formattedText: String
        val isTriggerAtPassed: Boolean
        if (curScheduleTiming.isTriggerAtDateOnly) {
            formattedText = curScheduleListTimingFormatter.format(
                entryAt = entryAtAsLocalDateTime,
                triggerAt = triggerAtAsLocalDateTime.date
            )
            isTriggerAtPassed = triggerAtAsLocalDateTime.date < entryAtAsLocalDateTime.date
        } else {
            formattedText = curScheduleListTimingFormatter.format(
                entryAt = entryAtAsLocalDateTime,
                triggerAt = triggerAtAsLocalDateTime
            )
            isTriggerAtPassed = triggerAtAsLocalDateTime < entryAtAsLocalDateTime
        }
        return if (isTriggerAtPassed.not()) formattedText
        else buildSpannedString {
            color(context.getThemeColor(attrRes = AttrIds.red1)) { append(formattedText) }
        }
    }


    fun bind(scheduleTiming: ScheduleTiming?, tags: List<Tag>) {
      /**
        if (scheduleTiming == boundScheduleTimingState?.timing) {
            boundScheduleTiming = scheduleTiming
            invalidateScheduleTiming()
        }
        */
    }
}