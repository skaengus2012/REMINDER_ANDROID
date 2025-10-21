/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.nlab.reminder.core.androidx.fragment.viewLifecycle
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.component.schedulelist.databinding.FragmentScheduleListBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

/**
 * @author Thalys
 */
internal class ScheduleListFragment : Fragment() {
    private var _binding: FragmentScheduleListBinding? = null
    private val binding: FragmentScheduleListBinding get() = checkNotNull(_binding)

    private var uiState = MutableStateFlow<ScheduleListContentUiState?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentScheduleListBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireContext())



        uiState.mapNotNull { it?.theme }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { theme ->
            }
            .launchIn(viewLifecycleScope)
    }

    fun onUiStateUpdated(uiState: ScheduleListContentUiState) {
        this.uiState.value = uiState
    }
}