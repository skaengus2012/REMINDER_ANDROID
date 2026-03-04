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

package com.nlab.reminder.core.designsystem.compose.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Thalys
 */
@Composable
fun PlaneatBottomSheet(
    title: PlaneatBottomSheetTitle,
    body: PlaneatBottomSheetBody,
    button: PlaneatBottomButton,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    BasePlaneatBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Content(
            title = title,
            body = body,
            button = button
        )
    }
}

@Composable
private fun Content(
    title: PlaneatBottomSheetTitle,
    body: PlaneatBottomSheetBody,
    button: PlaneatBottomButton,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Title(
            modifier = Modifier.fillMaxWidth(),
            title = title
        )
        Body(
            modifier = Modifier.fillMaxWidth(),
            body = body
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            button = button
        )
    }
}

@Composable
private fun Title(
    title: PlaneatBottomSheetTitle,
    modifier: Modifier = Modifier
) {
    when (title) {
        is PlaneatBottomSheetTitle.None -> {}
        is PlaneatBottomSheetTitle.Text -> {
            Text(
                modifier = modifier.padding(bottom = 6.dp),
                text = title.text,
                style = PlaneatTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PlaneatTheme.colors.content1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Body(
    body: PlaneatBottomSheetBody,
    modifier: Modifier = Modifier
) {
    when (body) {
        is PlaneatBottomSheetBody.None -> {}
        is PlaneatBottomSheetBody.Text -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(
                        start = 30.dp,
                        top = 0.dp,
                        end = 30.dp,
                        bottom = 22.dp
                    ),
                    style = PlaneatTheme.typography.bodySmall,
                    color = PlaneatTheme.colors.content1,
                    text = body.text
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = PlaneatTheme.colors.bgLine1,
                )
            }
        }
    }
}

@Composable
private fun Button(
    button: PlaneatBottomButton,
    modifier: Modifier = Modifier
) {
    when (button) {
        is PlaneatBottomButton.TwoButton -> {
            Column(modifier = modifier) {
                BaseButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = button.primaryButtonText,
                    fontColor = PlaneatTheme.colors.red1,
                    onClick = button.onPrimaryButtonClicked
                )
                BaseButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = button.secondaryButtonText,
                    fontColor = PlaneatTheme.colors.content2,
                    onClick = button.onSecondaryButtonClicked
                )
            }
        }
    }
}

@Composable
private fun BaseButton(
    text: String,
    fontColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable(
                onClick = onClick,
                onClickLabel = text,
                role = Role.Button
            ),
        color = Color.Transparent
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            text = text,
            textAlign = TextAlign.Center,
            color = fontColor,
            style = PlaneatTheme.typography.bodyLarge,
        )
    }
}

@Immutable
sealed class PlaneatBottomSheetTitle {
    data object None : PlaneatBottomSheetTitle()
    data class Text(val text: String) : PlaneatBottomSheetTitle()
}

@Immutable
sealed class PlaneatBottomSheetBody {
    data object None : PlaneatBottomSheetBody()
    data class Text(val text: String) : PlaneatBottomSheetBody()
}

@Immutable
sealed class PlaneatBottomButton {
    data class TwoButton(
        val primaryButtonText: String,
        val onPrimaryButtonClicked: () -> Unit,
        val secondaryButtonText: String,
        val onSecondaryButtonClicked: () -> Unit
    ) : PlaneatBottomButton()
}

@Preview(
    name = "LightNoneTitlePreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkNoneTitlePreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NoneTitlePreview() {
    PlaneatTheme {
        Content(
            modifier = Modifier.background(color = PlaneatTheme.colors.bgDialogSurface),
            title = PlaneatBottomSheetTitle.None,
            body = PlaneatBottomSheetBody.Text(text = "This is sample body"),
            button = PlaneatBottomButton.TwoButton(
                primaryButtonText = "Primary",
                onPrimaryButtonClicked = {},
                secondaryButtonText = "Secondary",
                onSecondaryButtonClicked = {}
            )
        )
    }
}

@Preview(
    name = "LightTextTitlePreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkTextTitlePreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TextTitlePreview() {
    PlaneatTheme {
        Content(
            modifier = Modifier.background(color = PlaneatTheme.colors.bgDialogSurface),
            title = PlaneatBottomSheetTitle.Text(text = "Title"),
            body = PlaneatBottomSheetBody.Text(text = "This is sample body"),
            button = PlaneatBottomButton.TwoButton(
                primaryButtonText = "Primary",
                onPrimaryButtonClicked = {},
                secondaryButtonText = "Secondary",
                onSecondaryButtonClicked = {}
            )
        )
    }
}

@Preview(
    name = "LightOnlyButtonsPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkOnlyButtonsPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OnlyButtonsPreview() {
    PlaneatTheme {
        Content(
            modifier = Modifier.background(color = PlaneatTheme.colors.bgDialogSurface),
            title = PlaneatBottomSheetTitle.None,
            body = PlaneatBottomSheetBody.None,
            button = PlaneatBottomButton.TwoButton(
                primaryButtonText = "Primary Button",
                onPrimaryButtonClicked = {},
                secondaryButtonText = "Secondary",
                onSecondaryButtonClicked = {}
            )
        )
    }
}