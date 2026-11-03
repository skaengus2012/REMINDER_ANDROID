/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.ui.content

import android.widget.FrameLayout
import androidx.recyclerview.widget.ItemTouchHelper
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

/**
 * @author Thalys
 */
class ScheduleListItemTouchCallbackTest {
    private val overlay = mockk<FrameLayout>(relaxed = true)
    private val callback = ScheduleListItemTouchCallback(
        scrollGuard = ScrollGuard(),
        scrollGuardMargin = 0f,
        dragAnchorOverlay = overlay,
        dragToScaleTargetHeight = 0f,
        dragScaleAnimateDuration = 0L,
        swipeCancelAnimateDuration = 0L,
        clampSwipeThreshold = 0.5f,
        maxClampSwipeWidthMultiplier = 1.75f,
        itemMoveListener = mockk(relaxed = true),
    )

    @Test
    fun `Given posted mirror view removal, When clearResource invoked, Then the same runnable is removed from callbacks`() {
        val postedRunnable = slot<Runnable>()
        callback.onSelectedChanged(null, ItemTouchHelper.ACTION_STATE_DRAG)
        callback.onSelectedChanged(null, ItemTouchHelper.ACTION_STATE_IDLE)
        verify { overlay.post(capture(postedRunnable)) }

        callback.clearResource()

        verify { overlay.removeCallbacks(postedRunnable.captured) }
    }

    @Test
    fun `Given posted mirror view removal, When runnable runs after clearResource, Then no view is removed`() {
        val postedRunnable = slot<Runnable>()
        callback.onSelectedChanged(null, ItemTouchHelper.ACTION_STATE_DRAG)
        callback.onSelectedChanged(null, ItemTouchHelper.ACTION_STATE_IDLE)
        verify { overlay.post(capture(postedRunnable)) }
        callback.clearResource()

        postedRunnable.captured.run()

        verify(exactly = 0) { overlay.removeView(allAny()) }
    }
}
