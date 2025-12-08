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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.compose.AndroidFragment
import com.nlab.reminder.core.androidx.compose.ui.LocalTimeZone
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarState
import com.nlab.reminder.core.data.model.ScheduleId
import kotlin.time.Instant

/**
 * @author Doohyun
 */
@Composable
fun ScheduleListContent(
    scheduleListItemsAdaptation: ScheduleListItemsAdaptation,
    entryAt: Instant,
    multiSelectionEnabled: Boolean,
    triggerAtFormatPatterns: TriggerAtFormatPatterns,
    theme: ScheduleListTheme,
    onItemSelectionChanged: (Set<ScheduleId>) -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier,
    toolbarState: ScheduleListToolbarState? = null,
    listBottomPadding: Dp = 0.dp
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val displayCutoutPaddings = WindowInsets.displayCutout.asPaddingValues()
    // Save fragment references in Compose state
    var fragmentRef by remember { mutableStateOf<ScheduleListFragment?>(null) }
    AndroidFragment<ScheduleListFragment>(
        modifier = run {
            var ret = modifier
                .fillMaxSize()
                .padding(
                    start = displayCutoutPaddings.calculateStartPadding(layoutDirection),
                    end = displayCutoutPaddings.calculateEndPadding(layoutDirection),
                )
                .imePadding()
            if (listBottomPadding == 0.dp) {
                ret = ret.navigationBarsPadding()
            }
            ret
        }
    ) { fragment -> fragmentRef = fragment }

    fragmentRef?.let { fragment ->
        val timeZone = LocalTimeZone.current
        LaunchedEffect(fragment, scheduleListItemsAdaptation) {
            fragment.onScheduleListItemsAdaptationUpdated(scheduleListItemsAdaptation)
        }
        LaunchedEffect(fragment, entryAt) {
            fragment.onEntryAtUpdated(entryAt)
        }
        LaunchedEffect(fragment, density, listBottomPadding) {
            fragment.onListBottomPaddingUpdated(
                bottom = if (listBottomPadding == 0.dp) {
                    0
                } else with(density) {
                    listBottomPadding.toPx().toInt()
                }
            )
        }
        LaunchedEffect(fragment, multiSelectionEnabled) {
            fragment.onMultiSelectionEnabledChanged(multiSelectionEnabled)
        }
        LaunchedEffect(fragment, triggerAtFormatPatterns) {
            fragment.onTriggerAtFormatPatternsUpdated(triggerAtFormatPatterns)
        }
        LaunchedEffect(fragment, theme) {
            fragment.onThemeUpdated(theme)
        }
        LaunchedEffect(fragment, timeZone) {
            fragment.onTimeZoneUpdated(timeZone)
        }
        LaunchedEffect(fragment, toolbarState) {
            fragment.onToolbarStateUpdated(toolbarState)
        }
        LaunchedEffect(fragment, onItemSelectionChanged) {
            fragment.onItemSelectionChangedObserverChanged(observer = onItemSelectionChanged)
        }
        LaunchedEffect(fragment, onSimpleAdd) {
            fragment.onSimpleAddCommandObserverChanged(observer = onSimpleAdd)
        }
        LaunchedEffect(fragment, onSimpleEdit) {
            fragment.onSimpleEditCommandObserverChanged(observer = onSimpleEdit)
        }
    }
}