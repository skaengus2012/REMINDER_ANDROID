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

package com.nlab.reminder.feature.all.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.androidx.compose.ui.DelayedContent
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.androidx.fragment.compose.AndroidFragment
import com.nlab.reminder.core.component.schedule.ui.compose.ScheduleListToolbar
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
@Composable
internal fun AllScreen(
    enterTransitionTimeInMillis: Int,
    onBackClicked: () -> Unit,
    showAppToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val testFlow = remember { MutableStateFlow(false) }
    AllScreen(
        modifier = modifier,
        fragmentStateBridge = rememberAllFragmentStateBridge(
            testFlow = testFlow,
            isToolbarTitleVisible = false,
            toolbarBackgroundAlpha = 0.0f,
            onSimpleEdited = { simpleEdit ->
                // TODO implements
                showAppToast("TODO Simple Edit $simpleEdit")
            }
        ),
        scheduleListDisplayDelayTimeMillis = enterTransitionTimeInMillis.toLong(),
        onBackClicked = onBackClicked,
        onMoreClicked = {
            testFlow.update { it.not() }
        },
        onCompleteClicked = {
            // TODO implements
        }
    )
}

@Composable
private fun AllScreen(
    fragmentStateBridge: AllFragmentStateBridge,
    scheduleListDisplayDelayTimeMillis: Long,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(PlaneatTheme.colors.bg2)
            .fillMaxSize()
    ) {
        AllToolbar(
            fragmentStateBridge = fragmentStateBridge,
            onBackClicked = onBackClicked,
            onMoreClicked = onMoreClicked,
            onCompleteClicked = onCompleteClicked
        )
        DelayedContent(delayTimeMillis = scheduleListDisplayDelayTimeMillis) {
            AllScheduleList(fragmentStateBridge = fragmentStateBridge)
        }
    }
}

@Composable
private fun AllScheduleList(
    fragmentStateBridge: AllFragmentStateBridge,
    modifier: Modifier = Modifier,
) {
    val direction = LocalLayoutDirection.current
    val displayCutoutPaddings = WindowInsets.displayCutout.asPaddingValues()
    AndroidFragment<AllFragment>(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = displayCutoutPaddings.calculateStartPadding(direction),
                end = displayCutoutPaddings.calculateEndPadding(direction)
            )
            .navigationBarsPadding()
            .imePadding()
    ) { it.fragmentStateBridge = fragmentStateBridge }
}

@Composable
private fun AllToolbar(
    fragmentStateBridge: AllFragmentStateBridge,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    ScheduleListToolbar(
        modifier = modifier,
        title = stringResource(StringIds.label_all),
        isTitleVisible = fragmentStateBridge.isToolbarTitleVisible,
        isMoreVisible = true,
        isCompleteVisible = true,
        backgroundAlpha = fragmentStateBridge.toolbarBackgroundAlpha,
        onBackClicked = onBackClicked,
        onMenuClicked = onMoreClicked,
        onCompleteClicked = {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            onCompleteClicked()
        }
    )
}

@Previews
@Composable
private fun AllScreenPreview() {
    PlaneatTheme {
        AllScreen(
            scheduleListDisplayDelayTimeMillis = 0,
            fragmentStateBridge = rememberAllFragmentStateBridge(
                isToolbarTitleVisible = true,
                toolbarBackgroundAlpha = 1.0f,
                onSimpleEdited = {}
            ),
            onBackClicked = {},
            onMoreClicked = {},
            onCompleteClicked = {}
        )
    }
}