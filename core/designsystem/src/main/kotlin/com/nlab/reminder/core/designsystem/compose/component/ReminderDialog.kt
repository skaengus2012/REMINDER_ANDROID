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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nlab.reminder.core.designsystem.compose.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
fun ReminderDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
        // TODO properties 적용.
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(8.94.dp),
            color = ReminderTheme.colors.bgDialogSurface,
            content = content
        )
    }
}

@Preview(
    name = "LightReminderDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkReminderDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ReminderDialogPreview() {
    ReminderTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            ReminderDialog(onDismissRequest = {}) {
                Box(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Hello, Dialogs",
                        color = ReminderTheme.colors.content1,
                    )
                }
            }
        }
    }
}