package com.nlab.reminder.core.component.bottomappbar.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.ColorPressButton
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@Composable
fun NewPlanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ColorPressButton(
        contentColor = PlaneatTheme.colors.point1,
        modifier = modifier.fillMaxHeight().padding(horizontal = 10.dp),
        onClick = onClick
    ) { contentColor ->
        Icon(
            modifier = Modifier
                .width(35.73.dp)
                .height(20.69.dp),
            painter = painterResource(id = DrawableIds.ic_new_plan),
            contentDescription = null,
            tint = contentColor
        )
        Text(
            text = stringResource(StringIds.new_plan),
            style = PlaneatTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

@Previews
@Composable
private fun NewPlanButtonPreview() {
    PlaneatTheme {
        NewPlanButton(
            onClick = {}
        )
    }
}