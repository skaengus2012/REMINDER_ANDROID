package com.nlab.reminder.core.designsystem.compose.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Doohyun
 */
@Composable
fun PlaneatDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        shape = RoundedCornerShape(size = 8.dp),
        modifier = modifier.background(PlaneatTheme.colors.bg1Layer),
        scrollState = scrollState,
        properties = properties,
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = PlaneatTheme.colors.bgLine1
        ),
        content = content
    )
}

@Composable
fun PlaneatDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: MenuItemColors = MenuItemColors(
        textColor = PlaneatTheme.colors.content1,
        leadingIconColor = PlaneatTheme.colors.content1,
        trailingIconColor = PlaneatTheme.colors.content1,
        disabledTextColor = PlaneatTheme.colors.content1.copy(alpha = 0.5f),
        disabledLeadingIconColor = PlaneatTheme.colors.content1.copy(alpha = 0.5f),
        disabledTrailingIconColor = PlaneatTheme.colors.content1.copy(alpha = 0.5f)
    ),
) {
    DropdownMenuItem(
        modifier = modifier,
        text = text,
        onClick = onClick,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
    )
}

@Composable
fun PlaneatDropdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = text,
        style = PlaneatTheme.typography.bodyMedium,
    )
}

@Composable
fun PlaneatDropdownIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier.size(24.dp),
        painter = painter,
        contentDescription = null,
    )
}

@Composable
fun PlaneatDropdownDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 0.5.dp,
    color: Color = PlaneatTheme.colors.bgLine1
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

@Preview(
    name = "LightPlaneatDropdownMenuItemPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkPlaneatDropdownMenuItemPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PlaneatDropdownTrailingMenuItemPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = PlaneatTheme.colors.bg1)
                .height(300.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .background(color = PlaneatTheme.colors.bg1Layer)
            ) {
                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "Simple") },
                    onClick = {},
                )

                PlaneatDropdownDivider()

                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "Simple Disabled") },
                    enabled = false,
                    onClick = {},
                )

                PlaneatDropdownDivider()

                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "Info") },
                    onClick = {},
                    trailingIcon = {
                        PlaneatDropdownIcon(
                            painterResource(DrawableIds.ic_more)
                        )
                    }
                )

                PlaneatDropdownDivider()

                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "Info Disabled") },
                    onClick = {},
                    trailingIcon = {
                        PlaneatDropdownIcon(
                            painterResource(DrawableIds.ic_more)
                        )
                    },
                    enabled = false
                )

                PlaneatDropdownDivider()

                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "More") },
                    onClick = {},
                    leadingIcon = {
                        PlaneatDropdownIcon(
                            painterResource(DrawableIds.ic_more)
                        )
                    }
                )

                PlaneatDropdownDivider()

                PlaneatDropdownMenuItem(
                    text = { PlaneatDropdownText(text = "Info Disabled") },
                    onClick = {},
                    leadingIcon = {
                        PlaneatDropdownIcon(
                            painterResource(DrawableIds.ic_more)
                        )
                    },
                    enabled = false
                )
            }
        }
    }
}