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
import android.graphics.Typeface
import android.text.Editable
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.component.displayformat.ScheduleTimingDisplayResource
import com.nlab.reminder.core.component.displayformat.ui.repeatDisplayText
import com.nlab.reminder.core.component.displayformat.ui.tagDisplayText
import com.nlab.reminder.core.component.schedule.ui.TriggerAtFormatPatterns
import com.nlab.reminder.core.component.schedule.R
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.math.max

/**
 * @author Doohyun
 */
class ScheduleDetailEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private val repeatSymbolBaselineShiftSpan = object : MetricAffectingSpan() {
        private val shiftPx = resources.getDimensionPixelSize(R.dimen.schedule_repeat_symbol_baseline_adjust)

        override fun updateDrawState(tp: TextPaint) {
            tp.baselineShift += shiftPx
        }

        override fun updateMeasureState(tp: TextPaint) {
            tp.baselineShift += shiftPx
        }
    }
    private val expiredColorSpan = ForegroundColorSpan(context.getThemeColor(AttrIds.red1))
    private val tagTextColorSpan = ForegroundColorSpan(context.getThemeColor(AttrIds.point_4))
    private val tagDisplayTextTransformer = ::tagDisplayText

    private lateinit var triggerAtFormatPatterns: TriggerAtFormatPatterns
    private lateinit var dateTimeFormatPool: DateTimeFormatPool
    private lateinit var displayTextPool: DisplayTextPool

    private var scheduleTiming: ScheduleTiming? = null
    private var scheduleCompleted: Boolean = false
    private var timeZone: TimeZone? = null
    private var entryAt: Instant? = null

    private var displayTimingText: CharSequence = ""
    private var displayTagsText: CharSequence = ""

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                if (s.startsWith(displayTimingText).not()) {
                    setText(displayTimingText)
                    setSelection(displayTimingText.length)
                }
            }
        })
    }

    internal fun initialize(
        triggerAtFormatPatterns: TriggerAtFormatPatterns,
        dateTimeFormatPool: DateTimeFormatPool,
        displayTextPool: DisplayTextPool
    ) {
        this.triggerAtFormatPatterns = triggerAtFormatPatterns
        this.dateTimeFormatPool = dateTimeFormatPool
        this.displayTextPool = displayTextPool
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        try {
            if (selStart < displayTimingText.length) {
                setSelection(displayTimingText.length, max(displayTimingText.length, selEnd))
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
                // Prevent the displayTimingText prefix from being included when the user selectAll
                val textLength = text?.length ?: 0
                if (textLength > displayTimingText.length) {
                    setSelection(displayTimingText.length, textLength)
                }
                true // prevent default behavior
            }
            else -> super.onTextContextMenuItem(id)
        }
    }

    fun bindTimeZone(timeZone: TimeZone) {
        if (this.timeZone == timeZone) return

        this.timeZone = timeZone
        invalidateScheduleTimingDisplayText()
    }

    fun bindEntryAt(entryAt: Instant) {
        if (this.entryAt == entryAt) return

        this.entryAt = entryAt
        invalidateScheduleTimingDisplayText()
    }

    fun bindScheduleData(
        scheduleTiming: ScheduleTiming?,
        scheduleCompleted: Boolean,
        tags: List<Tag>
    ) {
        if (this.scheduleCompleted != scheduleCompleted || this.scheduleTiming != scheduleTiming) {
            this.scheduleCompleted = scheduleCompleted
            this.scheduleTiming = scheduleTiming
            displayTimingText = getScheduleTimingDisplayText()
        }

        displayTagsText = getTagsDisplayText(tags)

        setTextAndSelection(getScheduleTimingAndTagsDisplayText())
    }

    private fun invalidateScheduleTimingDisplayText() {
        displayTimingText = getScheduleTimingDisplayText()
        setTextAndSelection(getScheduleTimingAndTagsDisplayText())
    }

    private fun getScheduleTimingDisplayText(): CharSequence {
        val curScheduleTiming = scheduleTiming
        val curTimeZone = timeZone
        val curEntryAt = entryAt
        if (curScheduleTiming == null || curTimeZone == null || curEntryAt == null) return ""

        return displayTextPool.getOrPut(curScheduleTiming) {
            getScheduleTimingDisplayText(curScheduleTiming, scheduleCompleted, curTimeZone, curEntryAt)
        }
    }

    private fun getScheduleTimingDisplayText(
        scheduleTiming: ScheduleTiming,
        scheduleCompleted: Boolean,
        timeZone: TimeZone,
        entryAt: Instant
    ): CharSequence {
        val scheduleTimingDisplayResource = ScheduleTimingDisplayResource(scheduleTiming, timeZone, entryAt)
        val dateTimeDisplayText: String
        val repeatDisplayText: String
        val isTriggerAtAfterEntry: Boolean
        when (scheduleTimingDisplayResource) {
            is ScheduleTimingDisplayResource.DateTime -> {
                dateTimeDisplayText = dateTimeFormatPool
                    .getOrPut(
                        resources,
                        pattern = triggerAtFormatPatterns.get(
                            resources,
                            triggerAt = scheduleTimingDisplayResource.triggerAt,
                            entryAt = scheduleTimingDisplayResource.entryAt
                        )
                    )
                    .format(scheduleTimingDisplayResource.triggerAt)
                repeatDisplayText = scheduleTimingDisplayResource.repeat
                    ?.let { repeat ->
                        repeatDisplayText(
                            resources = resources,
                            repeat = repeat,
                            triggerAt = scheduleTimingDisplayResource.triggerAt.date,
                        )
                    }
                    .orEmpty()
                isTriggerAtAfterEntry =
                    scheduleTimingDisplayResource.triggerAt >= scheduleTimingDisplayResource.entryAt
            }
            is ScheduleTimingDisplayResource.DateOnly -> {
                dateTimeDisplayText = dateTimeFormatPool
                    .getOrPut(
                        resources,
                        pattern = triggerAtFormatPatterns.get(
                            resources,
                            triggerAt = scheduleTimingDisplayResource.triggerAt,
                            entryAt = scheduleTimingDisplayResource.entryAt
                        )
                    )
                    .format(scheduleTimingDisplayResource.triggerAt)
                repeatDisplayText = scheduleTimingDisplayResource.repeat
                    ?.let { repeat ->
                        repeatDisplayText(
                            resources = resources,
                            repeat = repeat,
                            triggerAt = scheduleTimingDisplayResource.triggerAt,
                        )
                    }
                    .orEmpty()
                isTriggerAtAfterEntry =
                    scheduleTimingDisplayResource.triggerAt >= scheduleTimingDisplayResource.entryAt.date
            }
        }
        val repeatDisplayTextWithSymbol: CharSequence =
            if (repeatDisplayText.isEmpty()) ""
            else attachRepeatSymbol(repeatDisplayText)
        val dateTimeDisplayTextWithRepeatSymbol =
            if (dateTimeDisplayText.isEmpty()) repeatDisplayTextWithSymbol
            else TextUtils.concat(
                dateTimeDisplayText,
                " ",
                repeatDisplayTextWithSymbol,
            )
        return if (scheduleCompleted || isTriggerAtAfterEntry) dateTimeDisplayTextWithRepeatSymbol
        else buildSpannedString {
            inSpans(expiredColorSpan) { append(dateTimeDisplayTextWithRepeatSymbol) }
        }
    }

    private fun attachRepeatSymbol(repeatDetailText: String): CharSequence = buildSpannedString {
        append(" ")
        inSpans(StyleSpan(Typeface.BOLD), repeatSymbolBaselineShiftSpan) { append("↩") }
        append(" ")
        append(repeatDetailText)
        append(" ")
    }

    private fun getTagsDisplayText(tags: List<Tag>): CharSequence {
        return displayTextPool.getOrPut(tags) {
            buildSpannedString {
                inSpans(StyleSpan(Typeface.BOLD), tagTextColorSpan) {
                    append(tags.joinToString(separator = " ", transform = tagDisplayTextTransformer))
                }
            }
        }
    }

    private fun getScheduleTimingAndTagsDisplayText(): CharSequence {
        return TextUtils.concat(displayTimingText, " ", displayTagsText)
    }

    private fun setTextAndSelection(text: CharSequence) {
        setText(text)
        setSelection(text.length)
    }
}