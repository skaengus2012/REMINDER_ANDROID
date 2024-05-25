/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.designsystem.compose.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
inline fun ReminderBottomSheet(
    noinline onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    scrimColor: Color = ReminderTheme.colors.black.copy(alpha = 0.5f),
    crossinline content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ReminderTheme.colors.bgDialogSurface,
        scrimColor = scrimColor,
        content = {
            content()
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    )
}

@Preview(
    name = "LightReminderBottomSheetPreview",
    showBackground = true,
    widthDp = 800,
    heightDp = 300,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkReminderBottomSheetPreview",
    showBackground = true,
    widthDp = 800,
    heightDp = 300,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ReminderDialogPreview() {
    ReminderTheme {
        ReminderBottomSheet(
            onDismissRequest = {},
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(150.dp)
            ) {
                Text(
                    text = "Hello, Dialogs",
                    color = ReminderTheme.colors.content1,
                )
            }
        }
    }
}