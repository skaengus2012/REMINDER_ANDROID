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

package com.nlab.reminder.domain.feature.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.component.ColorPressButton
import com.nlab.reminder.core.android.compose.ui.throttle
import com.nlab.reminder.core.designsystem.compose.theme.ReminderTheme

/**
 * @author thalys
 */
@Composable
internal fun NewPlanButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ColorPressButton(
        contentColor = ReminderTheme.colors.point1,
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxHeight(),
        onClick = onClick.throttle()
    ) { contentColor ->
        Icon(
            modifier = Modifier
                .width(35.73.dp)
                .height(20.69.dp),
            painter = painterResource(id = R.drawable.ic_new_plan),
            contentDescription = null,
            tint = contentColor
        )
        Text(
            text = LocalContext.current.getString(R.string.new_schedule_label),
            style = ReminderTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

@Composable
internal fun TimePushSwitchButton(
    isPushOn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ColorPressButton(
        contentColor = ReminderTheme.colors.point1,
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxHeight(),
        onClick = onClick
    ) { contentColor ->
        Icon(
            modifier = Modifier
                .width(19.35.dp)
                .height(18.9.dp),
            painter = painterResource(id = R.drawable.ic_time_push),
            contentDescription = null,
            tint = contentColor,
        )
        Spacer(modifier = Modifier.width(5.87.dp))
        Text(
            text = LocalContext
                .current
                .getString(if (isPushOn) R.string.home_push_off_label else R.string.home_push_on_label),
            style = ReminderTheme.typography.titleMedium,
            color = contentColor
        )
    }
}

@Preview(
    name = "LightBottomContainerButtonsPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkBottomContainerButtonsPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BottomContainerButtonsPreview() {
    ReminderTheme {
        Row(Modifier.height(56.dp)) {
            NewPlanButton()
            Spacer(Modifier.width(30.dp))
            TimePushSwitchButton(isPushOn = true)
        }
    }
}
