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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nlab.reminder.core.androidx.compose.ui.DelayedContent
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.content.ui.FormBottomLine
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListContent
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListItem
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListTheme
import com.nlab.reminder.core.component.schedulelist.content.ui.SimpleAdd
import com.nlab.reminder.core.component.schedulelist.content.ui.SimpleEdit
import com.nlab.reminder.core.component.schedulelist.content.ui.rememberScheduleListItemsAdaptationState
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbar
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarState
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.rememberScheduleListToolbarState
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.collections.IdentityList
import com.nlab.reminder.core.androidx.compose.runtime.rememberAccumulatedStateStream
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.collections.toIdentityList
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.all.AllAction
import com.nlab.reminder.feature.all.AllEnvironment
import com.nlab.reminder.feature.all.AllReduce
import com.nlab.reminder.feature.all.AllUiState
import com.nlab.reminder.feature.all.AllUiStateSyncedFlow
import com.nlab.statekit.androidx.lifecycle.store.compose.retained
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap
import com.nlab.statekit.foundation.store.createStore
import kotlin.time.Instant

/**
 * @author Doohyun
 */
@Composable
internal fun AllScreen(
    enterTransitionTimeInMillis: Long,
    onBackClicked: () -> Unit,
    showAppToast: (String) -> Unit,
    modifier: Modifier = Modifier,
    environment: AllEnvironment = hiltViewModel()
) {
    val store = retained {
        createStore(
            initState = AllUiState.Loading,
            reduce = AllReduce(environment),
            bootstrap = AllUiStateSyncedFlow(environment).collectAsBootstrap(
                started = DeliveryStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
            )
        )
    }
    val uiState by store.state
        .rememberAccumulatedStateStream(
            accumulator = { prev, next ->
                if (prev !is AllUiState.Success || next !is AllUiState.Success) convertToOptimizedState(uiState = next)
                else reuseScheduleListIfUnchanged(prev = prev, next = next)
            },
            initialValueTransform = ::convertToOptimizedState
        )
        .collectAsStateWithLifecycle()
    AllScreen(
        modifier = modifier,
        uiState = uiState,
        contentDelayTimeMillis = enterTransitionTimeInMillis,
        onBackClicked = onBackClicked,
        onMoreClicked = { store.dispatch(AllAction.OnSelectionModeToggled) },
        onCompleteClicked = {
            // TODO implements
        },
        onItemSelectionChanged = { selectedIds ->
            store.dispatch(AllAction.OnItemSelectionChanged(selectedIds))
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

private fun convertToOptimizedState(uiState: AllUiState): AllUiState =
    if (uiState !is AllUiState.Success) uiState
    else uiState.copy(scheduleListResources = uiState.scheduleListResources.toIdentityList())

private fun reuseScheduleListIfUnchanged(prev: AllUiState.Success, next: AllUiState.Success): AllUiState {
    if (prev.scheduleListResources !is IdentityList
        || prev.scheduleListResources.value != next.scheduleListResources
    ) {
        return convertToOptimizedState(uiState = next)
    }
    return next.copy(scheduleListResources = prev.scheduleListResources)
}

@Composable
private fun AllScreen(
    uiState: AllUiState,
    contentDelayTimeMillis: Long,
    onBackClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    onItemSelectionChanged: (Set<ScheduleId>) -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = PlaneatTheme.colors.bg2)
            .fillMaxSize()
    ) {
        val title = stringResource(StringIds.label_all)
        val toolbarState = rememberScheduleListToolbarState()
        ScheduleListToolbar(
            modifier = modifier,
            title = title,
            toolbarState = toolbarState,
            isMoreVisible = true,
            isCompleteVisible = true,
            onBackClicked = onBackClicked,
            onMenuClicked = onMoreClicked,
            onCompleteClicked = onCompleteClicked
        )
        DelayedContent(delayTimeMillis = contentDelayTimeMillis) {
            when (uiState) {
                is AllUiState.Loading -> {
                    // todo make loading.
                }

                is AllUiState.Success -> {
                    AllScheduleListContent(
                        headline = title,
                        entryAt = uiState.entryAt,
                        scheduleListResources = uiState.scheduleListResources,
                        multiSelectionEnabled = uiState.multiSelectionEnabled,
                        toolbarState = toolbarState,
                        onItemSelectionChanged = onItemSelectionChanged,
                        onSimpleAdd = onSimpleAdd,
                        onSimpleEdit = onSimpleEdit
                    )
                }
            }
        }
    }
}

@Composable
private fun AllScheduleListContent(
    headline: String,
    entryAt: Instant,
    scheduleListResources: List<UserScheduleListResource>,
    multiSelectionEnabled: Boolean,
    toolbarState: ScheduleListToolbarState,
    onItemSelectionChanged: (Set<ScheduleId>) -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarsPaddings = WindowInsets.navigationBars.asPaddingValues()
    val footerForm = remember { ScheduleListItem.FooterForm(formBottomLine = FormBottomLine.Type1) }
    val scheduleListItemsAdaptation by rememberScheduleListItemsAdaptationState(
        headline = headline,
        elements = scheduleListResources,
        buildBodyItemsIfNotEmpty = { elements ->
            buildList {
                elements.forEach { resource ->
                    this += ScheduleListItem.Content(
                        resource = resource,
                        isLineVisible = true
                    )
                }
                this += footerForm
            }
        }
    )
    ScheduleListContent(
        modifier = modifier,
        scheduleListItemsAdaptation = scheduleListItemsAdaptation,
        entryAt = entryAt,
        multiSelectionEnabled = multiSelectionEnabled,
        triggerAtFormatPatterns = remember { AllScheduleTriggerAtFormatPatterns() },
        theme = ScheduleListTheme.Point3,
        listBottomScrollPadding = remember(navBarsPaddings) {
            navBarsPaddings.calculateBottomPadding()
        },
        toolbarState = toolbarState,
        onItemSelectionChanged = onItemSelectionChanged,
        onSimpleAdd = onSimpleAdd,
        onSimpleEdit = onSimpleEdit,
    )
}

@Previews
@Composable
private fun AllScreenPreview() {
    PlaneatTheme {
        AllScreen(
            contentDelayTimeMillis = 0,
            uiState = AllUiState.Loading,
            onBackClicked = {},
            onMoreClicked = {},
            onCompleteClicked = {},
            onItemSelectionChanged = {},
            onSimpleAdd = {},
            onSimpleEdit = {}
        )
    }
}