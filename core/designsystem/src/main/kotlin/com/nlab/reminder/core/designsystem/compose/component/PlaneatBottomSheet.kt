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
    title: PlanetBottomSheetTitle,
    body: PlanetBottomSheetBody,
    button: PlanetBottomButton,
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
    title: PlanetBottomSheetTitle,
    body: PlanetBottomSheetBody,
    button: PlanetBottomButton,
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
    title: PlanetBottomSheetTitle,
    modifier: Modifier = Modifier
) {
    when (title) {
        is PlanetBottomSheetTitle.None -> {}
        is PlanetBottomSheetTitle.Text -> {
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
    body: PlanetBottomSheetBody,
    modifier: Modifier = Modifier
) {
    when (body) {
        is PlanetBottomSheetBody.None -> {}
        is PlanetBottomSheetBody.Text -> {
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
    button: PlanetBottomButton,
    modifier: Modifier = Modifier
) {
    when (button) {
        is PlanetBottomButton.TwoButton -> {
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
sealed class PlanetBottomSheetTitle {
    data object None : PlanetBottomSheetTitle()
    data class Text(val text: String) : PlanetBottomSheetTitle()
}

@Immutable
sealed class PlanetBottomSheetBody {
    data object None : PlanetBottomSheetBody()
    data class Text(val text: String) : PlanetBottomSheetBody()
}

@Immutable
sealed class PlanetBottomButton {
    data class TwoButton(
        val primaryButtonText: String,
        val onPrimaryButtonClicked: () -> Unit,
        val secondaryButtonText: String,
        val onSecondaryButtonClicked: () -> Unit
    ) : PlanetBottomButton()
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
            title = PlanetBottomSheetTitle.None,
            body = PlanetBottomSheetBody.Text(text = "This is sample body"),
            button = PlanetBottomButton.TwoButton(
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
            title = PlanetBottomSheetTitle.Text(text = "Title"),
            body = PlanetBottomSheetBody.Text(text = "This is sample body"),
            button = PlanetBottomButton.TwoButton(
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
            title = PlanetBottomSheetTitle.None,
            body = PlanetBottomSheetBody.None,
            button = PlanetBottomButton.TwoButton(
                primaryButtonText = "Primary Button",
                onPrimaryButtonClicked = {},
                secondaryButtonText = "Secondary",
                onSecondaryButtonClicked = {}
            )
        )
    }
}