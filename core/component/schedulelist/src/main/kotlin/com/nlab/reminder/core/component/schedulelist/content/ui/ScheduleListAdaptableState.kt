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
    // Stage 1: Track the latest inputs as State to ensure they are correctly captured 
    // by the Stage 3 closure without restarting the entire pipeline.
    val currentElements by rememberUpdatedState(elements)
    val currentStamp by rememberUpdatedState(elementsReplayStamp)
    val currentHeadline by rememberUpdatedState(headline)
    val currentBuildBodyItems by rememberUpdatedState(buildBodyItems)

    // Stage 2: Heavy transformation in background.
    // Note: Keys (headline, elements) are compared using equals() on the Main thread.
    // To achieve O(1) performance, callers should provide an IdentityList.
    val snapshotState = produceState<AdaptationSnapshot?>(
        initialValue = null,
        headline,
        elements,
        currentBuildBodyItems
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
    // Since produceState runs asynchronously, there is a delay between input changes and snapshot delivery.
    // This derivedStateOf checks if the current snapshot is already in sync with the latest inputs.
    return remember {
        derivedStateOf {
            val snapshot =
                snapshotState.value
                ?: return@derivedStateOf ScheduleListItemsAdaptation.Absent

            // Check if the snapshot's data matches the current inputs.
            // Using '==' for elements is O(1) here because elements are expected to be an IdentityList,
            // which overrides equals() to use referential equality of its internal value.
            // This allows us to sync with produceState's key comparison logic (which uses equals()).
            val isSync =
                snapshot.elements == currentElements && snapshot.headline == currentHeadline

            ScheduleListItemsAdaptation.Exist(
                snapshot.items,
                // If data is in sync, we immediately adopt the latest stamp (Fast-track for Undo).
                // Otherwise, we must stick with the snapshot's stamp to avoid inconsistent UI states.
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