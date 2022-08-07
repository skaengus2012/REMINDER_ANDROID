/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.schedule.all.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.recyclerview.suspendSubmitList
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.schedule.view.DefaultScheduleItemAdapter
import com.nlab.reminder.domain.common.schedule.view.ScheduleItem
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemAnimator
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleState
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleViewModel
import com.nlab.reminder.domain.feature.schedule.all.onScheduleCompleteUpdateClicked
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class AllScheduleFragment : Fragment() {
    private val viewModel: AllScheduleViewModel by viewModels()

    private var _binding: FragmentAllScheduleBinding? = null
    private val binding: FragmentAllScheduleBinding get() = checkNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentAllScheduleBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scheduleAdapter = DefaultScheduleItemAdapter()
        val renderWhenLoaded = renderWhenLoadedFunc(scheduleAdapter)

        binding.contentRecyclerview
            .apply { itemAnimator = ScheduleItemAnimator() }
            .apply { adapter = scheduleAdapter }

        viewModel.state
            .filterIsInstance<AllScheduleState.Loaded>()
            .flowWithLifecycle(viewLifecycle)
            .map { it.allSchedulesReport }
            .distinctUntilChanged()
            .map { report ->
                AllScheduleLoadedSnapshot(report, scheduleItemFactory = { uiState ->
                    ScheduleItem(
                        uiState,
                        onCompleteToggleClicked = {
                            viewModel.onScheduleCompleteUpdateClicked(
                                uiState.schedule.id(), isComplete = uiState.isCompleteMarked.not()
                            )
                        }
                    )
                })
            }
            .flowOn(Dispatchers.Default)
            .onEach(renderWhenLoaded)
            .launchIn(viewLifecycleScope)
    }

    private fun renderWhenLoadedFunc(
        doingScheduleAdapter: DefaultScheduleItemAdapter
    ): suspend (AllScheduleLoadedSnapshot) -> Unit = { snapshot ->
        doingScheduleAdapter.suspendSubmitList(snapshot.doingScheduleItems)
        startPostponedEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}