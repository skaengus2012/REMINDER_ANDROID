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

package com.nlab.reminder.core.androidx.fragment.compose

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.FragmentState
import androidx.fragment.compose.rememberFragmentState
import androidx.fragment.compose.AndroidFragment as OriginAndroidFragment

/**
 * @author Thalys
 */
@Composable
inline fun <reified T : ComposableFragment> AndroidFragment(
    modifier: Modifier = Modifier,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    noinline onUpdate: (T) -> Unit = { }
) {
    AndroidFragment(
        clazz = T::class.java,
        modifier = modifier,
        fragmentState = fragmentState,
        arguments = arguments,
        onUpdate = onUpdate
    )
}

@Composable
fun <T : ComposableFragment> AndroidFragment(
    clazz: Class<T>,
    modifier: Modifier = Modifier,
    fragmentState: FragmentState = rememberFragmentState(),
    arguments: Bundle = Bundle.EMPTY,
    onUpdate: (T) -> Unit = { }
) {
    OriginAndroidFragment(
        clazz = clazz,
        modifier = modifier,
        fragmentState = fragmentState,
        arguments = arguments,
        onUpdate = { fragment ->
            onUpdate(fragment)
            fragment.completeComposition()
        }
    )
}