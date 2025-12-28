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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
    internal data class Exist(val items: IdentityList<ScheduleListItem>) : ScheduleListItemsAdaptation()
}

@Composable
fun <T : ScheduleListElement> rememberScheduleListItemsAdaptationState(
    headline: String,
    elements: List<T>,
    buildBodyItemsIfNotEmpty: (List<T>) -> List<ScheduleListItem>,
): State<ScheduleListItemsAdaptation> {
    val headlineItem by produceState<ScheduleListItem.Headline?>(
        initialValue = null,
        key1 = headline
    ) { value = ScheduleListItem.Headline(text = headline) }
    val bodyItems by produceState<List<ScheduleListItem>?>(
        initialValue = null,
        key1 = elements,
        key2 = buildBodyItemsIfNotEmpty
    ) {
        value = if (elements.isEmpty()) {
            emptyList()
        } else {
            withContext(Dispatchers.Default) {
                buildBodyItemsIfNotEmpty(elements).toIdentityList()
            }
        }
    }
    return produceState<ScheduleListItemsAdaptation>(
        initialValue = ScheduleListItemsAdaptation.Absent,
        key1 = headlineItem,
        key2 = bodyItems,
    ) {
        val currentHeadlineItem = headlineItem
        val currentBodyItems = bodyItems
        if (currentHeadlineItem == null || currentBodyItems == null) {
            value = ScheduleListItemsAdaptation.Absent
            return@produceState
        }
        value = withContext(Dispatchers.Default) {
            val totalItems = buildList {
                // add headline
                add(currentHeadlineItem)
                add(ScheduleListItem.HeadlinePadding)

                if (currentBodyItems.isEmpty()) {
                    // TODO add empty ui
                } else {
                    addAll(currentBodyItems)
                }
            }
            ScheduleListItemsAdaptation.Exist(items = totalItems.toIdentityList())
        }
    }
}