/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.android.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter

/**
 * This used Material2 ModalBottomSheetLayout.
 * The Material3's ModalBottomSheet scrim could not cover the status bar.
 * @author thalys
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReminderThemeBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    onHide: () -> Unit = {},
    content: @Composable () -> Unit
) {
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .dropWhile { it == ModalBottomSheetValue.Hidden }
            .filter { value -> value == ModalBottomSheetValue.Hidden }
            .collect { onHide() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        modifier = modifier,
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        scrimColor = ReminderTheme.colors.bgDim,
        sheetContent = sheetContent,
        content = content
    )
}