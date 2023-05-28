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

package com.nlab.reminder.core.android.designsystem.component

import android.annotation.SuppressLint
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@SuppressLint("ComposableNaming")
@Composable
inline fun BottomSheetShowEffect(
    sheetState: ModalBottomSheetState,
    vararg key: Any?,
    crossinline onDismiss: () -> Unit
) {
    LaunchedEffect(*key) {
        launch {
            snapshotFlow { sheetState.currentValue }
                .dropWhile { it == ModalBottomSheetValue.Hidden }
                .collect { value ->
                    if (value == ModalBottomSheetValue.Hidden) {
                        onDismiss()
                    }
                }
        }
        sheetState.show()
    }
}