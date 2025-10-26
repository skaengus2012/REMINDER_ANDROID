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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.nlab.reminder.core.component.schedulelist.content.ScheduleListElement
import com.nlab.reminder.core.androidx.compose.runtime.IdentityList
import com.nlab.reminder.core.androidx.compose.runtime.toIdentityList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * @author Doohyun
 */
@Stable
class ScheduleListItemAdaptableState internal constructor() {
    internal var value: IdentityList<ScheduleListItem> by mutableStateOf(IdentityList())
}

@Composable
fun <T : ScheduleListElement> rememberScheduleListItemAdaptableState(
    headline: String,
    elements: IdentityList<T>,
    buildBodyItems: (List<T>) -> List<ScheduleListItem>,
    onEmpty: () -> List<ScheduleListItem> = { emptyList() },
): ScheduleListItemAdaptableState {
    val headlineState = rememberUpdatedState(headline)
    val elementsState = rememberUpdatedState(elements)
    val buildBodyItemsState = rememberUpdatedState(buildBodyItems)
    val result = remember { ScheduleListItemAdaptableState() }
    LaunchedEffect(Unit) {
        combine(
            snapshotFlow { headlineState.value }
                .distinctUntilChanged()
                .map(ScheduleListItem::Headline),
            snapshotFlow { elementsState.value }
                .distinctUntilChangedBy { it.value }
                .combine(snapshotFlow { buildBodyItemsState.value }) { elementsValue, buildBodyItemsValue ->
                    val isElementEmpty = elementsValue.value.isEmpty()
                    BodyItemTransformResult(
                        isElementEmpty = isElementEmpty,
                        items = if (isElementEmpty) onEmpty() else buildBodyItemsValue(elementsValue.value)
                    )
                }
        ) { headlineItem, bodyItemTransformResult ->
            val newItems = if (bodyItemTransformResult.isElementEmpty) {
                bodyItemTransformResult.items
            } else buildList {
                add(headlineItem)
                add(ScheduleListItem.HeadlinePadding)
                addAll(bodyItemTransformResult.items)
            }
            newItems.toIdentityList()
        }.flowOn(Dispatchers.Default).collect { result.value = it }
    }
    return result
}

private data class BodyItemTransformResult(
    val isElementEmpty: Boolean,
    val items: List<ScheduleListItem>
)