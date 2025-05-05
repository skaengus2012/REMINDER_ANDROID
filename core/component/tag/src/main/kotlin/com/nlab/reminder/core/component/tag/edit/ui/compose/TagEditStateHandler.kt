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

package com.nlab.reminder.core.component.tag.edit.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.nlab.reminder.core.component.tag.edit.TagEditState

/**
 * @author Doohyun
 */
@Composable
fun TagEditStateHandler(
    state: TagEditState,
    onCompleted: () -> Unit,
    onRenameRequestClicked: () -> Unit,
    onDeleteRequestClicked: () -> Unit,
    onRenameInputReady: () -> Unit,
    onRenameInputted: (String) -> Unit,
    onRenameConfirmClicked: () -> Unit,
    onMergeCancelClicked: () -> Unit,
    onMergeConfirmClicked: () -> Unit,
    onDeleteConfirmClicked: () -> Unit,
) {
    when (state) {
        is TagEditState.None -> Unit

        is TagEditState.AwaitTaskSelection -> {
            TagEditIntroDialog(
                tagName = state.tag.name,
                onDismissRequest = onCompleted,
                onRenameRequestClicked = onRenameRequestClicked,
                onDeleteRequestClicked = onDeleteRequestClicked
            )
        }

        is TagEditState.Rename -> {
            TagRenameDialog(
                value = state.renameText,
                tagName = state.tag.name,
                usageCount = state.usageCount,
                shouldKeyboardShown = state.shouldUserInputReady,
                onTextChanged = onRenameInputted,
                onCancel = onCompleted,
                onConfirm = onRenameConfirmClicked
            )
            if (state.shouldUserInputReady) {
                SideEffect { onRenameInputReady() }
            }
        }

        is TagEditState.Merge -> {
            TagMergeDialog(
                fromTagName = state.from.name,
                toTagName = state.to.name,
                onDismissRequested = onCompleted,
                onCancel = onMergeCancelClicked,
                onConfirm = onMergeConfirmClicked
            )
        }

        is TagEditState.Delete -> {
            TagDeleteBottomSheet(
                tagNames = remember(state.tag) { listOf(state.tag.name) },
                usageCount = state.usageCount,
                onCancel = onCompleted,
                onConfirm = onDeleteConfirmClicked
            )
        }

        is TagEditState.Processing -> {
            // TODO If necessary, let's work on it.
        }
    }
}