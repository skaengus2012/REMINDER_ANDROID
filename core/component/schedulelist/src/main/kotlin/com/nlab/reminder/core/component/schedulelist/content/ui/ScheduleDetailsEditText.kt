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

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.doOnDetach
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.kotlin.NonBlankString
import kotlinx.coroutines.Runnable
import kotlinx.datetime.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Instant

/**
 * @author Doohyun
 */
internal class ScheduleDetailsEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private val tagSelectionAdjustRunnable: Runnable

    private val tagsDisplayParser = TagsDisplayParser
    private lateinit var tagsDisplayFormatter: TagsDisplayFormatter

    private var scheduleTimingText: CharSequence = ""

    // Material for ScheduleTimingText
    private var scheduleTimingDisplayFormatter: ScheduleTimingDisplayFormatter? = null
    private var scheduleTiming: ScheduleTiming? = null
    private var completed: Boolean = false
    private var timeZone: TimeZone? = null
    private var entryAt: Instant? = null
    private var tags: List<Tag>? = null

    private var isUpdatingDetailsText: Boolean = false

    private var isFullyInitialized = false

    init {
        post {
            isFullyInitialized = true
        }

        tagSelectionAdjustRunnable = object : Runnable {
            private val tagSelectionAdjustHelper = TagSelectionAdjustHelper()

            override fun run() {
                // FIXME dragging, processing modification required
                // When holding a drag, the cursor is always positioned in front of the tag text.
                // The movements are not awkward, so I keep the present for now.
                tagSelectionAdjustHelper.adjustSelectionToTagBounds(
                    text,
                    selectionStart,
                    selectionEnd,
                    invokeWhenNewSelectionNeeded = { newSelStart, newSelEnd -> setSelection(newSelStart, newSelEnd) }
                )
            }
        }
        doOnDetach {
            // If the view is removed from the window, the reserved runner is removed.
            removeCallbacks(tagSelectionAdjustRunnable)
        }

        addTextChangedListener(object : TextWatcher {
            private val partialTagDeletionCapture = PartialTagDeletionCapture()
            private val tagStyleRemover = TagStyleRemover()

            private var start = -1
            private var count = -1
            private var after = -1
            private var spacePressedPos = -1

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (isUpdatingDetailsText) return

                this.start = start
                this.count = count
                this.after = after
                this.spacePressedPos = -1
                this.partialTagDeletionCapture.reset()

                if (count > 0 && scheduleTimingText.isNotEmpty() && start < scheduleTimingText.length) {
                    // Prevent scheduleTimingText from being removed.
                    runWithDetailsTextUpdate { setText(s) }
                    setSelection(s.length)
                    return
                }

                if (count > 0) {
                    partialTagDeletionCapture.check(
                        text = s,
                        start = start,
                        end = start + count
                    )
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isUpdatingDetailsText) return

                if (count > 0) {
                    val newChars = s.subSequence(start, start + count)
                    if (newChars.toString() == " ") {
                        spacePressedPos = start
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (isUpdatingDetailsText) return

                if (partialTagDeletionCapture.isTried) {
                    val targetStart = partialTagDeletionCapture.deletingTagStart
                    val targetEnd = partialTagDeletionCapture.deletingTagEnd
                    runWithDetailsTextUpdate { setText(partialTagDeletionCapture.textForRecovery) }
                    setSelection(targetStart, targetEnd)
                    return
                }

                if (spacePressedPos > 0 && s.getOrNull(spacePressedPos - 1) != ' ') {
                    val currentExtraText = findExtraText()
                    if (currentExtraText is Spanned) {
                        val tagNames = tagsDisplayParser.parse(currentExtraText)
                        if (tagNames.isNotEmpty()) {
                            val fakeTagId = TagId(0)
                            val fakeTags = tagNames.map { tagName ->
                                Tag(id = fakeTagId, name = tagName)
                            }
                            tags = fakeTags
                            val detailsText = createDetailsText(
                                extraText = tagsDisplayFormatter.format(context, fakeTags)
                            )
                            val selectionPos = detailsText.indexOf(
                                char = ' ',
                                startIndex = spacePressedPos
                            ).let { index ->
                                if (index == -1) detailsText.length
                                else index
                            }
                            runWithDetailsTextUpdate { setText(detailsText) }
                            setSelection(selectionPos)
                            return
                        }
                    }
                }

                if (start >= 0 && after > 0) {
                    val result = tagStyleRemover.remove(
                        text = s,
                        start = start,
                        addedSize = after
                    )
                    if (result is TagStyleRemover.Result.InvalidStyle) {
                        // https://github.com/skaengus2012/REMINDER_ANDROID/issues/556
                        // When input is performed while a tag is attached to the cursor,
                        // remove the tag and replace it with the new text.
                        val newText = s.subSequence(start, start + after)
                        s.delete(result.styleStart, result.styleEnd)
                        s.insert(result.styleStart, newText)
                        runWithDetailsTextUpdate { setText(s) }
                        val newSelEnd = result.styleStart + newText.length

                        // If the number of characters entered is one,
                        // the cursor is sent behind the input character.
                        setSelection(
                            /*start= */ if (newText.length > 1) result.styleStart else newSelEnd,
                            /*stop= */newSelEnd
                        )
                    }
                    return
                }
            }
        })

        setInputAvailable(available = false)
    }

    fun getCurrentTagTexts(): Set<NonBlankString> {
        if (tags.isNullOrEmpty()) return emptySet()

        val currentExtraText = findExtraText()
        if (currentExtraText !is Spanned) return emptySet()

        return tagsDisplayParser.parse(currentExtraText).toSet()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (scheduleTimingText.isNotEmpty()) {
                    val offset = getOffsetForPosition(event.x, event.y)
                    if (offset < scheduleTimingText.length) {
                        // If the touch occurs within the displayTimingText area, it is ignored.
                        return false
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        // https://github.com/skaengus2012/REMINDER_ANDROID/issues/554
        // When expanding the drag area without the cursor handle, selStart and selEnd are reversed.
        val safeSelStart = min(selStart, selEnd)
        val safeSelEnd = max(selStart, selEnd)

        super.onSelectionChanged(safeSelStart, safeSelEnd)

        if (isFullyInitialized.not()) {
            // Executes the default implementation when called from the parent constructor
            return
        }

        removeCallbacks(tagSelectionAdjustRunnable)
        postDelayed(tagSelectionAdjustRunnable, TAG_SELECTION_ADJUST_DEBOUNCE_MS)

        val currentText = text
        val textLength = currentText?.length ?: 0
        if (safeSelStart > textLength || safeSelEnd > textLength) {
            // check out of bounds
            return
        }

        val timingTextLength = scheduleTimingText.length
        val needHapticFeedback = safeSelStart > timingTextLength
        if (isHapticFeedbackEnabled != needHapticFeedback) {
            // Since selStart cannot be ahead of Timing, it also invalidates feedback.
            isHapticFeedbackEnabled = needHapticFeedback
        }

        if (safeSelStart < timingTextLength) {
            // Selected the part of the text entered after detailsText
            val newStart = min(timingTextLength, textLength)
            setSelection(newStart, max(newStart, safeSelEnd))
            return
        }
    }

    internal fun initialize(tagsDisplayFormatter: TagsDisplayFormatter) {
        this.tagsDisplayFormatter = tagsDisplayFormatter
    }

    private inline fun runWithDetailsTextUpdate(block: () -> Unit) {
        isUpdatingDetailsText = true
        block()
        isUpdatingDetailsText = false
    }

    private fun findExtraText(): CharSequence? {
        val currentText = text
        return if (currentText == null || scheduleTimingText.isEmpty()) currentText
        else currentText.subSequence(scheduleTimingText.length, currentText.length)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return when (id) {
            android.R.id.selectAll -> {
                // Prevent the displayTimingText prefix from being included when the user selectAll
                val textLength = text?.length ?: 0
                if (textLength > scheduleTimingText.length) {
                    setSelection(scheduleTimingText.length, textLength)
                }
                true // prevent default behavior
            }

            else -> super.onTextContextMenuItem(id)
        }
    }

    fun bindScheduleTimingDisplayFormatter(formatter: ScheduleTimingDisplayFormatter) {
        if (this.scheduleTimingDisplayFormatter === formatter) return
        this.scheduleTimingDisplayFormatter = formatter

        val currentExtraText = findExtraText()
        scheduleTimingText = formatter.format(
            context = context,
            scheduleTiming = scheduleTiming,
            timeZone = timeZone,
            entryAt = entryAt,
            completed = completed
        )
        setDetailsTextAndSelection(extraText = currentExtraText ?: "")
    }

    private fun refreshScheduleTimingText() {
        val currentFormatter = scheduleTimingDisplayFormatter
        if (currentFormatter != null) {
            val currentExtraText = findExtraText()

            scheduleTimingText = currentFormatter.format(
                context = context,
                scheduleTiming = scheduleTiming,
                timeZone = timeZone,
                entryAt = entryAt,
                completed = completed
            )
            setDetailsTextAndSelection(extraText = currentExtraText ?: "")
        }
    }

    fun bindTimeZone(timeZone: TimeZone) {
        if (this.timeZone == timeZone) return
        this.timeZone = timeZone
        refreshScheduleTimingText()
    }

    fun bindEntryAt(entryAt: Instant) {
        if (this.entryAt == entryAt) return
        this.entryAt = entryAt
        refreshScheduleTimingText()
    }

    fun bindCompleted(completed: Boolean) {
        if (this.completed == completed) return
        this.completed = completed
        refreshScheduleTimingText()
    }

    fun bindScheduleData(scheduleTiming: ScheduleTiming?, completed: Boolean, tags: List<Tag>) {
        /**
         * The formatter cache checks for object identity, so it performs an object comparison.
         *
         * @see ScheduleTimingDisplayFormatter
         * @see TagsDisplayFormatter
         */
        val needScheduleTimingDisplayTextUpdate =
            this.scheduleTiming !== scheduleTiming || this.completed != completed
        val needTagsDisplayTextUpdate = this.tags !== tags
        if (needScheduleTimingDisplayTextUpdate.not() && needTagsDisplayTextUpdate.not()) {
            return
        }

        if (needScheduleTimingDisplayTextUpdate) {
            this.completed = completed
            this.scheduleTiming = scheduleTiming

            val currentFormatter = scheduleTimingDisplayFormatter
            if (currentFormatter != null) {
                this.scheduleTimingText = currentFormatter.format(
                    context = context,
                    scheduleTiming = scheduleTiming,
                    timeZone = timeZone,
                    entryAt = entryAt,
                    completed = completed
                )
            }
        }

        if (needTagsDisplayTextUpdate) {
            this.tags = tags
            setInputAvailable(available = tags.isNotEmpty())
        }

        setVisible(isVisible = scheduleTiming != null || tags.isNotEmpty())

        // This needs to be called when either scheduleTimingText or Tags change.
        // For Tags, identity comparison is used, so if the tags are the same, tagsDisplayFormatter retrieves the data from the cache.
        setDetailsTextAndSelection(extraText = tagsDisplayFormatter.format(context, tags = tags))
    }

    private fun createDetailsText(extraText: CharSequence): CharSequence {
        val isTimingTextEmpty = scheduleTimingText.isEmpty()
        val isExtraTextEmpty = extraText.isEmpty()
        return when {
            isTimingTextEmpty && isExtraTextEmpty -> ""
            isTimingTextEmpty -> extraText
            isExtraTextEmpty -> scheduleTimingText
            else -> TextUtils.concat(scheduleTimingText, extraText)
        }
    }

    private fun setDetailsTextAndSelection(extraText: CharSequence) {
        val detailsText = createDetailsText(extraText)
        runWithDetailsTextUpdate { setText(detailsText) }
        setSelection(detailsText.length)
    }

    private fun setInputAvailable(available: Boolean) {
        isFocusable = available
        isFocusableInTouchMode = available
    }

    companion object {
        private const val TAG_SELECTION_ADJUST_DEBOUNCE_MS = 500L
    }
}