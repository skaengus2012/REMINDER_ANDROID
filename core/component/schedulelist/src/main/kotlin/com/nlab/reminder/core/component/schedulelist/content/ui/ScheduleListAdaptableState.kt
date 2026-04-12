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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.nlab.reminder.core.component.schedulelist.content.ScheduleListElement
import com.nlab.reminder.core.kotlin.collections.IdentityList
import com.nlab.reminder.core.kotlin.collections.toIdentityList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Doohyun
 */
@Immutable
sealed class ScheduleListItemsAdaptation {
    internal data object Absent : ScheduleListItemsAdaptation()
    internal data class Exist(
        val items: IdentityList<ScheduleListItem>,
        val replayStamp: Long
    ) : ScheduleListItemsAdaptation()
}

@Composable
fun <T : ScheduleListElement> rememberScheduleListItemsAdaptationState(
    headline: String,
    elements: List<T>,
    elementsReplayStamp: Long,
    buildBodyItems: (List<T>) -> List<ScheduleListItem>,
): State<ScheduleListItemsAdaptation> {
    // Stage 1: Track the latest inputs as State to avoid O(N) equals() on Main thread.
    val currentElements by rememberUpdatedState(elements)
    val currentStamp by rememberUpdatedState(elementsReplayStamp)
    val currentHeadline by rememberUpdatedState(headline)

    // Stage 2: Heavy transformation in background.
    // Keyed by data content only, allowing it to skip when only the stamp changes (Undo case).
    val snapshotState = produceState<AdaptationSnapshot?>(
        initialValue = null,
        headline,
        elements,
        buildBodyItems
    ) {
        value = withContext(Dispatchers.Default) {
            val bodyItems = buildBodyItems(elements)
            // capacity = bodyItems.size + 3 (headline, headlinePadding, bottomAppbarPadding)
            val totalItems = buildList(capacity = bodyItems.size + 3) {
                add(ScheduleListItem.Headline(text = headline))
                add(ScheduleListItem.HeadlinePadding)
                addAll(bodyItems)
                add(ScheduleListItem.BottomAppbarPadding)
            }.toIdentityList()

            AdaptationSnapshot(totalItems, elements, headline, elementsReplayStamp)
        }
    }

    // Stage 3: Adaptive wrapper for perfect sync and fast-track updates.
    return remember {
        derivedStateOf {
            val snapshot =
                snapshotState.value
                ?: return@derivedStateOf ScheduleListItemsAdaptation.Absent
            // If data content hasn't changed, sync with the latest stamp immediately (Fast-track for Undo).
            val isSync =
                snapshot.elements === currentElements && snapshot.headline == currentHeadline
            ScheduleListItemsAdaptation.Exist(
                snapshot.items,
                replayStamp = if (isSync) currentStamp else snapshot.replayStamp
            )
        }
    }
}

/**
 * Internal snapshot of the adaptation result paired with the inputs that produced it.
 */
private data class AdaptationSnapshot(
    val items: IdentityList<ScheduleListItem>,
    val elements: List<*>,
    val headline: String,
    val replayStamp: Long
)