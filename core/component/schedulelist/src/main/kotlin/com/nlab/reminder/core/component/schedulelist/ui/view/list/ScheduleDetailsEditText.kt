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

package com.nlab.reminder.core.component.schedulelist.ui.view.list

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.doOnDetach
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import kotlinx.coroutines.Runnable
import kotlinx.datetime.TimeZone
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Instant

/**
 * @author Doohyun
 */
internal class ScheduleDetailsEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private val tagSelectionAdjustRunnable: Runnable

    private lateinit var scheduleTimingDisplayFormatter: ScheduleTimingDisplayFormatter
    private lateinit var tagsDisplayFormatter: TagsDisplayFormatter

    private var scheduleTiming: ScheduleTiming? = null
    private var scheduleCompleted: Boolean = false
    private var scheduleTimingText: CharSequence = ""
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                this.start = start
                this.count = count
                this.after = after
                this.partialTagDeletionCapture.reset()

                if (isUpdatingDetailsText.not() && count > 0) {
                    partialTagDeletionCapture.check(
                        text = s,
                        start = start,
                        end = start + count
                    )
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingDetailsText) return
                if (scheduleTimingText.isNotEmpty() && s?.startsWith(scheduleTimingText) != true) {
                    Timber.w(message = "Unexpected input encountered in ScheduleDetailsEditText.")
                    runWithDetailsTextUpdate { setText(scheduleTimingText) }
                    setSelection(scheduleTimingText.length)
                    return
                }

                if (partialTagDeletionCapture.isTried) {
                    val targetStart = partialTagDeletionCapture.deletingTagStart
                    val targetEnd = partialTagDeletionCapture.deletingTagEnd
                    runWithDetailsTextUpdate { setText(partialTagDeletionCapture.textForRecovery) }
                    setSelection(targetStart, targetEnd)
                    return
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
                        s as Editable
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

    internal fun initialize(
        scheduleTimingDisplayFormatter: ScheduleTimingDisplayFormatter,
        tagsDisplayFormatter: TagsDisplayFormatter
    ) {
        this.scheduleTimingDisplayFormatter = scheduleTimingDisplayFormatter
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

    fun bindTimeZone(timeZone: TimeZone) {
        if (this.timeZone == timeZone) return

        val currentExtraText = findExtraText()

        this.timeZone = timeZone
        this.scheduleTimingText = scheduleTimingDisplayFormatter.format(
            context = context,
            scheduleTiming = scheduleTiming,
            timeZone = timeZone,
            entryAt = entryAt,
            completed = scheduleCompleted
        )
        setDetailsTextAndSelection(extraText = currentExtraText ?: "")
    }

    fun bindEntryAt(entryAt: Instant) {
        if (this.entryAt == entryAt) return

        val currentExtraText = findExtraText()

        this.entryAt = entryAt
        this.scheduleTimingText = scheduleTimingDisplayFormatter.format(
            context = context,
            scheduleTiming = scheduleTiming,
            timeZone = timeZone,
            entryAt = entryAt,
            completed = scheduleCompleted
        )
        setDetailsTextAndSelection(extraText = currentExtraText ?: "")
    }

    fun bindScheduleData(scheduleTiming: ScheduleTiming?, scheduleCompleted: Boolean, tags: List<Tag>) {
        /**
         * The formatter cache checks for object identity, so it performs an object comparison.
         *
         * @see ScheduleTimingDisplayFormatter
         * @see TagsDisplayFormatter
         */
        val needScheduleTimingDisplayTextUpdate =
            this.scheduleTiming !== scheduleTiming || this.scheduleCompleted != scheduleCompleted
        val needTagsDisplayTextUpdate = this.tags !== tags
        if (needScheduleTimingDisplayTextUpdate.not() && needTagsDisplayTextUpdate.not()) {
            return
        }

        if (needScheduleTimingDisplayTextUpdate) {
            this.scheduleCompleted = scheduleCompleted
            this.scheduleTiming = scheduleTiming
            this.scheduleTimingText = scheduleTimingDisplayFormatter.format(
                context = context,
                scheduleTiming = scheduleTiming,
                timeZone = timeZone,
                entryAt = entryAt,
                completed = scheduleCompleted
            )
        }

        if (needTagsDisplayTextUpdate) {
            this.tags = tags
            setInputAvailable(available = tags.isNotEmpty())
        }

        setDetailsTextAndSelection(extraText = tagsDisplayFormatter.format(context, tags = tags))
        setVisible(isVisible = scheduleTiming != null || tags.isNotEmpty())
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