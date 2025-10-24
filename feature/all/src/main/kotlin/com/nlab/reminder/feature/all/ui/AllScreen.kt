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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.androidx.compose.ui.DelayedContent
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListContent
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListSnapshot
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListTheme
import com.nlab.reminder.core.component.schedulelist.content.ui.SimpleAdd
import com.nlab.reminder.core.component.schedulelist.content.ui.SimpleEdit
import com.nlab.reminder.core.component.schedulelist.content.ui.rememberScheduleListItemAdaptableState
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbar
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarRenderState
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.rememberScheduleListToolbarRenderState
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @author Doohyun
 */
@Composable
internal fun AllScreen(
    enterTransitionTimeInMillis: Long,
    onBackClicked: () -> Unit,
    showAppToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val entryAt = remember { Clock.System.now() }
    var itemSelectionEnabled by remember { mutableStateOf(false) }
    var items: List<UserScheduleListResource> by remember {
        mutableStateOf(emptyList())
    }

    if (items.isEmpty()) {
        LaunchedEffect(Unit) {
            val newItems = withContext(Dispatchers.Default) {
                FakeData.testItems
            }
            items = newItems
        }
    }

    AllScreen(
        modifier = modifier,
        scheduleListDisplayDelayTimeMillis = enterTransitionTimeInMillis,
        scheduleListSnapshot = ScheduleListSnapshot(items),
        itemSelectionEnabled = itemSelectionEnabled,
        entryAt = entryAt,
        onBackClicked = onBackClicked,
        onMoreClicked = {
            // TODO implements
            itemSelectionEnabled = itemSelectionEnabled.not()
        },
        onCompleteClicked = {
            // TODO implements
        },
        onSimpleAdd = { simpleAdd ->
            // TODO implements
            showAppToast("TODO Simple Add $simpleAdd")
        },
        onSimpleEdit = { simpleEdit ->
            // TODO implements
            showAppToast("TODO Simple Edit $simpleEdit")
        }
    )
}

@Composable
private fun AllScreen(
    scheduleListDisplayDelayTimeMillis: Long,
    scheduleListSnapshot: ScheduleListSnapshot<UserScheduleListResource>,
    itemSelectionEnabled: Boolean,
    entryAt: Instant,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color = PlaneatTheme.colors.bg2)
            .fillMaxSize()
    ) {
        val toolbarRenderState = rememberScheduleListToolbarRenderState()
        AllScheduleListToolbar(
            toolbarRenderState = toolbarRenderState,
            onBackClicked = onBackClicked,
            onMoreClicked = onMoreClicked,
            onCompleteClicked = onCompleteClicked
        )
        DelayedContent(delayTimeMillis = scheduleListDisplayDelayTimeMillis) {
            AllScheduleListContent(
                toolbarRenderState = toolbarRenderState,
                scheduleListSnapshot = scheduleListSnapshot,
                entryAt = entryAt,
                itemSelectionEnabled = itemSelectionEnabled,
                onSimpleAdd = onSimpleAdd,
                onSimpleEdit = onSimpleEdit
            )
        }
    }
}

@Composable
private fun AllScheduleListToolbar(
    toolbarRenderState: ScheduleListToolbarRenderState,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScheduleListToolbar(
        modifier = modifier,
        renderState = toolbarRenderState,
        title = stringResource(StringIds.label_all),
        isMoreVisible = true,
        isCompleteVisible = true,
        onBackClicked = onBackClicked,
        onMenuClicked = onMoreClicked,
        onCompleteClicked = onCompleteClicked
    )
}

@Composable
private fun AllScheduleListContent(
    toolbarRenderState: ScheduleListToolbarRenderState,
    scheduleListSnapshot: ScheduleListSnapshot<UserScheduleListResource>,
    entryAt: Instant,
    itemSelectionEnabled: Boolean,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier
) {
    ScheduleListContent(
        modifier = modifier,
        itemState = rememberScheduleListItemAdaptableState(
            headline = stringResource(StringIds.label_all),
            snapshot = scheduleListSnapshot,
            buildBodyItems = { elements ->
                TODO()
            }
        ),
        toolbarRenderState = toolbarRenderState,
        entryAt = entryAt,
        itemSelectionEnabled = itemSelectionEnabled,
        triggerAtFormatPatterns = remember { AllScheduleTriggerAtFormatPatterns() },
        theme = ScheduleListTheme.Point3,
        onSimpleAdd = onSimpleAdd,
        onSimpleEdit = onSimpleEdit,
    )
}

@Previews
@Composable
private fun AllScreenPreview() {
    PlaneatTheme {
        AllScreen(
            scheduleListDisplayDelayTimeMillis = 0,
            scheduleListSnapshot = ScheduleListSnapshot(emptyList()),
            itemSelectionEnabled = true,
            entryAt = Clock.System.now(),
            onBackClicked = {},
            onMoreClicked = {},
            onCompleteClicked = {},
            onSimpleAdd = {},
            onSimpleEdit = {}
        )
    }
}