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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.nlab.reminder.core.component.schedulelist.content.ui.ClearableCompletedOption
import com.nlab.reminder.core.component.schedulelist.content.ui.CompletionUpdate
import com.nlab.reminder.core.component.schedulelist.content.ui.ScheduleListHeadlineOption
import com.nlab.reminder.core.component.schedulelist.content.ui.SelectionUpdate
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.MenuDropdown
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDropdownDivider
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDropdownIcon
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDropdownMenu
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDropdownMenuItem
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDropdownText
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.kotlin.collections.toIdentityList
import com.nlab.reminder.core.kotlin.toNonNegativeLong
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
                if (prev !is AllUiState.Success || next !is AllUiState.Success) {
                    convertToOptimizedState(uiState = next)
                } else {
                    reuseScheduleListIfUnchanged(prev = prev, next = next)
                }
            },
            initialValueTransform = ::convertToOptimizedState
        )
        .collectAsStateWithLifecycle()
    AllScreen(
        modifier = modifier,
        uiState = uiState,
        contentDelayTimeMillis = enterTransitionTimeInMillis,
        onBackClicked = onBackClicked,
        onMenuClicked = { store.dispatch(AllAction.MenuClicked) },
        onMenuDropdownDismissed = { store.dispatch(AllAction.MenuDropdownDismissed) },
        onSelectionStartClicked = {
            store.dispatch(AllAction.SelectionModeClicked(enabled = true))
        },
        onSelectionCompleteClicked = {
            store.dispatch(AllAction.SelectionModeClicked(enabled = false))
        },
        onCompletedScheduleVisibilityChangeClicked = {
            store.dispatch(AllAction.CompletedScheduleVisibilityChangeClicked(it))
        },
        onItemSelectionChanged = { selectionUpdate ->
            store.dispatch(AllAction.ItemSelectionUpdated(selectionUpdate.selectedIds))
        },
        onCompletionUpdated = { completionUpdate ->
            store.dispatch(
                AllAction.ItemCompletionUpdated(
                    scheduleId = completionUpdate.id,
                    targetCompleted = completionUpdate.targetCompleted
                )
            )
        },
        onItemPositionUpdated = { userScheduleListResources ->
            store.dispatch(AllAction.ItemPositionUpdated(userScheduleListResources))
        },
        onSimpleAdd = { simpleAdd ->
            store.dispatch(AllAction.AddSchedule(title = simpleAdd.title, note = simpleAdd.note))
        },
        onSimpleEdit = { simpleEdit ->
            store.dispatch(
                AllAction.EditSchedule(
                    id = simpleEdit.id,
                    title = simpleEdit.title,
                    note = simpleEdit.note,
                    tagNames = simpleEdit.tagNames
                )
            )
        }
    )
}

private fun convertToOptimizedState(uiState: AllUiState): AllUiState =
    if (uiState !is AllUiState.Success) uiState
    else uiState.copy(scheduleResources = uiState.scheduleResources.toIdentityList())

private fun reuseScheduleListIfUnchanged(
    prev: AllUiState.Success,
    next: AllUiState.Success
): AllUiState {
    if (prev.scheduleResources !is IdentityList
        || prev.scheduleResources.value != next.scheduleResources
    ) {
        return convertToOptimizedState(uiState = next)
    }
    return next.copy(scheduleResources = prev.scheduleResources)
}

@Composable
private fun AllScreen(
    uiState: AllUiState,
    contentDelayTimeMillis: Long,
    onBackClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    onMenuDropdownDismissed: () -> Unit,
    onSelectionStartClicked: () -> Unit,
    onSelectionCompleteClicked: () -> Unit,
    onCompletedScheduleVisibilityChangeClicked: (Boolean) -> Unit,
    onItemSelectionChanged: (SelectionUpdate) -> Unit,
    onCompletionUpdated: (CompletionUpdate) -> Unit,
    onItemPositionUpdated: (List<UserScheduleListResource>) -> Unit,
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
        val successUiState = uiState as? AllUiState.Success
        ScheduleListToolbar(
            modifier = modifier,
            title = title,
            toolbarState = toolbarState,
            menuVisible = successUiState?.multiSelectionEnabled?.not() ?: true,
            menuDropdown = MenuDropdown(
                isVisible = successUiState?.menuDropdownVisible ?: false
            ) {
                MenuDropdown(
                    // safe: this lambda is invoked only when successUiState is a non-null AllUiState.Success
                    isCompletedScheduleShown = successUiState!!.completedScheduleVisible,
                    onSelectionStartClicked = onSelectionStartClicked,
                    onCompletedScheduleVisibilityChangeClicked = onCompletedScheduleVisibilityChangeClicked,
                    onDismissed = onMenuDropdownDismissed,
                )
            },
            onMenuClicked = onMenuClicked,
            selectionCompleteVisible = successUiState?.multiSelectionEnabled ?: false,
            onSelectionCompleteClicked = onSelectionCompleteClicked,
            onBackClicked = onBackClicked,
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
                        completedScheduleVisible = uiState.completedScheduleVisible,
                        scheduleResources = uiState.scheduleResources,
                        replayStamp = uiState.replayStamp,
                        multiSelectionEnabled = uiState.multiSelectionEnabled,
                        toolbarState = toolbarState,
                        onItemSelectionChanged = onItemSelectionChanged,
                        onCompletionUpdated = onCompletionUpdated,
                        onItemPositionUpdated = onItemPositionUpdated,
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
    completedScheduleVisible: Boolean,
    scheduleResources: List<UserScheduleListResource>,
    replayStamp: Long,
    multiSelectionEnabled: Boolean,
    toolbarState: ScheduleListToolbarState,
    onItemSelectionChanged: (SelectionUpdate) -> Unit,
    onCompletionUpdated: (CompletionUpdate) -> Unit,
    onItemPositionUpdated: (List<UserScheduleListResource>) -> Unit,
    onSimpleAdd: (SimpleAdd) -> Unit,
    onSimpleEdit: (SimpleEdit) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBarsPaddings = WindowInsets.navigationBars.asPaddingValues()
    val footerForm = remember { ScheduleListItem.FooterForm(formBottomLine = FormBottomLine.Type1) }
    val scheduleListItemsAdaptation by rememberScheduleListItemsAdaptationState(
        headline = ScheduleListHeadlineOption(
            title = headline,
            clearableCompletedOption = when (completedScheduleVisible) {
                true -> ClearableCompletedOption(
                    completedScheduleCount = scheduleResources.count { it.schedule.isComplete }.toNonNegativeLong()
                )

                false -> null
            }
        ),
        elements = scheduleResources,
        elementsReplayStamp = replayStamp,
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
        onSelectionUpdated = onItemSelectionChanged,
        onCompletionUpdated = onCompletionUpdated,
        onItemPositionUpdated = { itemPositionUpdated ->
            val userScheduleListResources = buildList {
                itemPositionUpdated.snapshot.forEach { item ->
                    if (item is ScheduleListItem.Content) {
                        this += item.resource
                    }
                }
            }
            onItemPositionUpdated(userScheduleListResources)
        },
        onSimpleAdd = onSimpleAdd,
        onSimpleEdit = onSimpleEdit,
    )
}

@Composable
private fun MenuDropdown(
    isCompletedScheduleShown: Boolean,
    onSelectionStartClicked: () -> Unit,
    onCompletedScheduleVisibilityChangeClicked: (Boolean) -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaneatDropdownMenu(
        modifier = modifier.width(250.dp),
        onDismissRequest = onDismissed,
        expanded = true
    ) {
        PlaneatDropdownMenuItem(
            text = {
                PlaneatDropdownText(text = stringResource(StringIds.content_select_plans))
            },
            leadingIcon = {
                PlaneatDropdownIcon(
                    painter = painterResource(DrawableIds.ic_round_check_stroke_24)
                )
            },
            onClick = {
                onSelectionStartClicked()
                onDismissed()
            }
        )

        PlaneatDropdownDivider()

        PlaneatDropdownMenuItem(
            text = {
                PlaneatDropdownText(
                    text = stringResource(
                        if (isCompletedScheduleShown) StringIds.content_completed_hidden
                        else StringIds.content_completed_shown
                    )
                )
            },
            leadingIcon = {
                PlaneatDropdownIcon(
                    painter = painterResource(
                        if (isCompletedScheduleShown) DrawableIds.ic_eye_invisible_24
                        else DrawableIds.ic_eye_visible_24
                    )
                )
            },
            onClick = {
                onCompletedScheduleVisibilityChangeClicked(isCompletedScheduleShown.not())
                onDismissed()
            }
        )
    }
}

@Previews
@Composable
private fun AllScreenPreview() {
    PlaneatTheme {
        AllScreen(
            contentDelayTimeMillis = 0,
            uiState = AllUiState.Loading,
            onBackClicked = {},
            onMenuClicked = {},
            onMenuDropdownDismissed = {},
            onSelectionStartClicked = {},
            onSelectionCompleteClicked = {},
            onCompletedScheduleVisibilityChangeClicked = {},
            onItemSelectionChanged = {},
            onCompletionUpdated = {},
            onItemPositionUpdated = {},
            onSimpleAdd = {},
            onSimpleEdit = {}
        )
    }
}