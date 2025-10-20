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
import android.graphics.Typeface
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.component.displayformat.ui.tagDisplayText
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds
import java.util.IdentityHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * @author Thalys
 */
internal class TagStyleSpan(
    context: Context
) : CharacterStyle(), UpdateAppearance {
    private val color: Int = context.getThemeColor(AttrIds.point_1_sub)
    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = Typeface.create(tp.typeface, Typeface.BOLD)
        tp.color = color
    }
}

internal object TagStyleParser {
    fun findAppliedTagStyles(
        text: Spanned,
        start: Int,
        end: Int,
    ): Array<TagStyleSpan> = text.getSpans(start, end, TagStyleSpan::class.java)

    /**
     * @param output An IntArray with two sizes is required.
     */
    fun findAppliedTagStyleRange(
        text: Spanned,
        start: Int,
        end: Int,
        output: IntArray
    ) {
        val appliedSpans = text.getSpans(start, end, TagStyleSpan::class.java)
        if (appliedSpans.isEmpty()) {
            output[0] = -1
            output[1] = -1
        } else {
            output[0] = text.getSpanStart(appliedSpans.first())
            output[1] = text.getSpanEnd(appliedSpans.last())
        }
    }
}

internal class TagsDisplayFormatter {
    private val cache = IdentityHashMap<List<Tag>, CharSequence>()

    fun format(context: Context, tags: List<Tag>): CharSequence = cache.getOrPut(tags) {
        val spans = Array(tags.size) { index ->
            buildSpannedString {
                inSpans(TagStyleSpan(context)) { append(tagDisplayText(tags[index])) }
                append(" ")
            }
        }
        TextUtils.concat(*spans)
    }

    fun releaseCache() {
        cache.clear()
    }
}

internal class PartialTagDeletionCapture {
    private val tagStyleParser = TagStyleParser
    private val appliedTagStyleRange = intArrayOf(-1, -1)

    var deletingTagStart: Int = -1
        private set
    var deletingTagEnd: Int = -1
        private set
    var textForRecovery: CharSequence? = null
        private set

    val isTried: Boolean get() = textForRecovery != null

    fun reset() {
        deletingTagStart = -1
        deletingTagEnd = -1
        textForRecovery = null
    }

    fun check(text: CharSequence?, start: Int, end: Int) {
        reset()
        if (text !is Spanned) return

        tagStyleParser.findAppliedTagStyleRange(
            text,
            start,
            end,
            output = appliedTagStyleRange
        )
        if (appliedTagStyleRange[0] == -1
            || appliedTagStyleRange[1] == -1
            || (start <= appliedTagStyleRange[0] && appliedTagStyleRange[1] <= end)
        ) return

        deletingTagStart = appliedTagStyleRange[0]
        deletingTagEnd = appliedTagStyleRange[1]
        textForRecovery = SpannableString(text)
    }
}

internal class TagStyleRemover {
    private val tagStyleParser = TagStyleParser

    fun remove(text: CharSequence?, start: Int, addedSize: Int): Result {
        if (text !is Editable) return Result.NotFound

        val end = start + addedSize
        for (style in tagStyleParser.findAppliedTagStyles(text, start, end)) {
            val spanStart = text.getSpanStart(style)
            val spanEnd = text.getSpanEnd(style)
            text.removeSpan(style)
            if (spanStart + addedSize != end) {
                return Result.InvalidStyle(
                    styleStart = spanStart,
                    styleEnd = spanEnd
                )
            }
            if (spanEnd > end) {
                text.setSpan(style, end, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                return Result.StyleRemoved
            }
        }
        return Result.NotFound
    }

    sealed class Result {
        data object StyleRemoved : Result()
        data object NotFound : Result()
        data class InvalidStyle(val styleStart: Int, val styleEnd: Int) : Result()
    }
}

internal class TagSelectionAdjustHelper {
    private val tagStyleParser = TagStyleParser
    private val appliedTagStyleRange = intArrayOf(-1, -1)

    inline fun adjustSelectionToTagBounds(
        text: CharSequence?,
        selStart: Int,
        selEnd: Int,
        invokeWhenNewSelectionNeeded: (newSelStart: Int, newSelEnd: Int) -> Unit
    ) {
        if (text !is Spanned) return

        tagStyleParser.findAppliedTagStyleRange(
            text,
            selStart,
            selEnd,
            output = appliedTagStyleRange
        )
        val tagStart = appliedTagStyleRange[0]
        val tagEnd = appliedTagStyleRange[1]
        if (tagStart == -1
            || tagEnd == -1
            || (selStart == tagStart && selEnd == tagEnd)
            || ((tagStart >= selStart || selStart >= tagEnd) && (tagStart >= selEnd || selEnd >= tagEnd))
        ) return

        invokeWhenNewSelectionNeeded(min(selStart, tagStart), max(selEnd, tagEnd))
    }
}