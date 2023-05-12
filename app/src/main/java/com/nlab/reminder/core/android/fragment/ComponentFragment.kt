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

package com.nlab.reminder.core.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.*
import androidx.fragment.app.Fragment
import com.nlab.reminder.databinding.FragmentComponentBinding

/**
 * Fragment for Compose Composition
 * @author Doohyun
 */
abstract class ComponentFragment : Fragment() {
    private var _binding: FragmentComponentBinding? = null
    val composeView: ComposeView get() = checkNotNull(_binding).composeView

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentComponentBinding.inflate(inflater, container, false)
        .apply {
            with(composeView) {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                composeView.setContent {

                }
            }
        }
        .also { _binding = it }
        .root

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated(savedInstanceState)
    }

    final override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun onViewCreated(savedInstanceState: Bundle?)
}