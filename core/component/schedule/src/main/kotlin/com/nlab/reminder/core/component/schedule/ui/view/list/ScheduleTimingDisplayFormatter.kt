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
import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.component.displayformat.ScheduleTimingDisplayResource
import com.nlab.reminder.core.component.displayformat.ui.repeatDisplayText
import com.nlab.reminder.core.component.schedule.R
import com.nlab.reminder.core.component.schedule.ui.TriggerAtFormatPatterns
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import kotlinx.datetime.TimeZone
import java.util.IdentityHashMap
import kotlin.time.Instant

/**
 * @author Doohyun
 */
internal class ScheduleTimingDisplayFormatter(
    private val triggerAtFormatPatterns: TriggerAtFormatPatterns,
    private val dateTimeFormatPool: DateTimeFormatPool
) {
    private val cache = IdentityHashMap<Any, CachedValue>()

    fun format(
        context: Context,
        scheduleTiming: ScheduleTiming,
        timeZone: TimeZone,
        entryAt: Instant,
        completed: Boolean,
    ): CharSequence {
        val cachedValue = cache[scheduleTiming]
        if (cachedValue == null) {
            val scheduleTimingResource = ScheduleTimingDisplayResource(scheduleTiming, timeZone, entryAt)
            val originDisplayText = getDisplayText(
                context.resources,
                scheduleTimingResource
            )
            val finalDisplayText = decorateWithCompleted(
                context,
                originDisplayText,
                completed,
                provideScheduleTimingDisplayResource = { scheduleTimingResource }
            )
            cache[scheduleTiming] = CachedValue(
                timeZone = timeZone,
                entryAt = entryAt,
                originDisplayText = originDisplayText,
                completed = completed,
                finalDisplayText = finalDisplayText
            )
            return finalDisplayText
        }

        val isOriginTextEquals = cachedValue.timeZone == timeZone && entryAt == cachedValue.entryAt
        val isCompletedEquals = completed == cachedValue.completed
        return when {
            isOriginTextEquals && isCompletedEquals -> {
                cachedValue.finalDisplayText
            }

            isOriginTextEquals.not() -> {
                val scheduleTimingResource = ScheduleTimingDisplayResource(scheduleTiming, timeZone, entryAt)
                val originDisplayText = getDisplayText(context.resources, scheduleTimingResource)
                val finalDisplayText = decorateWithCompleted(
                    context,
                    originDisplayText,
                    completed,
                    provideScheduleTimingDisplayResource = { scheduleTimingResource }
                )
                cachedValue.timeZone = timeZone
                cachedValue.entryAt = entryAt
                cachedValue.completed = completed
                cachedValue.originDisplayText = originDisplayText
                cachedValue.finalDisplayText = finalDisplayText

                finalDisplayText
            }

            // If only schedule completed has changed,
            else -> {
                val finalDisplayText = decorateWithCompleted(
                    context,
                    cachedValue.originDisplayText,
                    completed,
                    provideScheduleTimingDisplayResource = {
                        ScheduleTimingDisplayResource(scheduleTiming, timeZone, entryAt)
                    }
                )
                cachedValue.completed = completed
                cachedValue.finalDisplayText = finalDisplayText

                finalDisplayText
            }
        }
    }

    private fun getDisplayText(
        resources: Resources,
        scheduleTimingDisplayResource: ScheduleTimingDisplayResource
    ): CharSequence {
        val dateTimeDisplayText: String
        val repeatDisplayText: String
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
            }
        }
        val repeatDisplayTextWithSymbol: CharSequence =
            if (repeatDisplayText.isEmpty()) ""
            else attachRepeatSymbol(resources, repeatDisplayText)
        return if (dateTimeDisplayText.isEmpty()) repeatDisplayTextWithSymbol
        else TextUtils.concat(dateTimeDisplayText, " ", repeatDisplayTextWithSymbol)
    }

    private fun decorateWithCompleted(
        context: Context,
        originText: CharSequence,
        completed: Boolean,
        provideScheduleTimingDisplayResource: () -> ScheduleTimingDisplayResource
    ): CharSequence {
        if (completed) return originText

        val isTriggerAtAfterEntry = when (val displayResource = provideScheduleTimingDisplayResource()) {
            is ScheduleTimingDisplayResource.DateTime -> {
                displayResource.triggerAt >= displayResource.entryAt
            }
            is ScheduleTimingDisplayResource.DateOnly -> {
                displayResource.triggerAt >= displayResource.entryAt.date
            }
        }
        if (isTriggerAtAfterEntry) return originText

        return buildSpannedString {
            inSpans(ForegroundColorSpan(context.getThemeColor(AttrIds.red1))) { append(originText) }
        }
    }

    private fun attachRepeatSymbol(
        resources: Resources,
        repeatDetailText: String
    ): CharSequence = buildSpannedString {
        append(" ")
        inSpans(
            StyleSpan(Typeface.BOLD),
            RepeatSymbolBaselineShiftSpan(resources)
        ) { append("↩") }
        append(" ")
        append(repeatDetailText)
        append("  ")
    }

    fun releaseCache() {
        cache.clear()
    }

    private class CachedValue(
        var timeZone: TimeZone,
        var entryAt: Instant,
        var originDisplayText: CharSequence,
        var completed: Boolean,
        var finalDisplayText: CharSequence
    )
}

internal fun ScheduleTimingDisplayFormatter.format(
    context: Context,
    scheduleTiming: ScheduleTiming?,
    timeZone: TimeZone?,
    entryAt: Instant?,
    completed: Boolean,
): CharSequence =
    if (scheduleTiming == null || timeZone == null || entryAt == null) ""
    else format(context, scheduleTiming, timeZone, entryAt, completed)

private class RepeatSymbolBaselineShiftSpan(resources: Resources) : MetricAffectingSpan() {
    private val shiftPx = resources.getDimensionPixelSize(R.dimen.schedule_repeat_symbol_baseline_adjust)

    override fun updateDrawState(tp: TextPaint) {
        tp.baselineShift += shiftPx
    }

    override fun updateMeasureState(tp: TextPaint) {
        tp.baselineShift += shiftPx
    }
}