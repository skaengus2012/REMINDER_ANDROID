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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.fragment.compose.AndroidFragment
import com.nlab.reminder.core.androidx.compose.ui.LocalTimeZone
import com.nlab.reminder.core.kotlin.collections.NonEmptyList
import kotlin.time.Instant

/**
 * @author Doohyun
 */
@Composable
fun ScheduleListContent(
    items: NonEmptyList<ScheduleListItem>,
    entryAt: Instant,
    itemSelectionEnabled: Boolean,
    triggerAtFormatPatterns: TriggerAtFormatPatterns,
    theme: ScheduleListTheme,
    onToolbarVisibleChanged: (Boolean) -> Unit,
    onToolbarBackgroundAlphaChanged: (Float) -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier
) {
    var fragmentRef by remember { mutableStateOf<ScheduleListFragment?>(null) }

    val layoutDirection = LocalLayoutDirection.current
    val displayCutoutPaddings = WindowInsets.displayCutout.asPaddingValues()
    AndroidFragment<ScheduleListFragment>(
        modifier =  modifier
            .fillMaxSize()
            .padding(
                start = displayCutoutPaddings.calculateStartPadding(layoutDirection),
                end = displayCutoutPaddings.calculateEndPadding(layoutDirection),
            )
            .navigationBarsPadding()
            .imePadding()
    ) { fragment -> fragmentRef = fragment }

    fragmentRef?.let { fragment ->
        val timeZone = LocalTimeZone.current
        SideEffect {
            fragment.onScheduleListItemUpdated(items)
            fragment.onItemSelectionEnabledChanged(itemSelectionEnabled)
            fragment.onTriggerAtFormatPatternsUpdated(triggerAtFormatPatterns)
            fragment.onThemeUpdated(theme)
            fragment.onTimeZoneUpdated(timeZone)
            fragment.onEntryAtUpdated(entryAt)
            fragment.onToolbarVisibleChangedObserverChanged(observer = onToolbarVisibleChanged)
            fragment.onToolbarBackgroundAlphaChangedObserverChanged(observer = onToolbarBackgroundAlphaChanged)
            fragment.onSimpleAddCommandObserverChanged(observer = onSimpleAdd)
            fragment.onSimpleEditCommandObserverChanged(observer = onSimpleEdit)
        }
    }
}