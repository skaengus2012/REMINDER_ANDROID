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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.schedule.view.DefaultSchedulePagingAdapter
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemAnimator
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemTouchMediator
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleState
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleViewModel
import com.nlab.reminder.domain.feature.schedule.all.onScheduleCompleteModifyClicked
import com.nlab.reminder.domain.feature.schedule.all.onToggleCompletedScheduleShownClicked
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class AllScheduleFragment : Fragment() {
    private val viewModel: AllScheduleViewModel by viewModels()

    private var _binding: FragmentAllScheduleBinding? = null
    private val binding: FragmentAllScheduleBinding get() = checkNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentAllScheduleBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scheduleAdapter = DefaultSchedulePagingAdapter(
            onCompleteClicked = { scheduleUiState ->
                viewModel.onScheduleCompleteModifyClicked(
                    scheduleId = scheduleUiState.schedule.id(),
                    isComplete = scheduleUiState.isCompleteMarked.not()
                )
            }
        )
        val scheduleItemTouchMediator = ScheduleItemTouchMediator(viewLifecycleOwner, scheduleAdapter)

        binding.contentRecyclerview
            .apply { scheduleItemTouchMediator.attachToRecyclerView(recyclerView = this) }
            .apply { itemAnimator = ScheduleItemAnimator() }
            .apply { adapter = scheduleAdapter }

        scheduleItemTouchMediator.dragEndedFlow
            .onEach {
                // TODO make order save.
                println("TODO make order save.")
            }
            .launchIn(viewLifecycleScope)

        binding.buttonCompletedScheduleShownToggle
            .throttleClicks()
            .onEach { viewModel.onToggleCompletedScheduleShownClicked() }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.snapshot }
            .map { it.isDoneScheduleShown }
            .distinctUntilChanged()
            .onEach { isDoneScheduleShown ->
                binding.buttonCompletedScheduleShownToggle.setText(
                    if (isDoneScheduleShown) R.string.completed_schedule_hidden
                    else R.string.completed_schedule_shown
                )
            }
            .launchIn(viewLifecycleScope)

        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow
                    .filterIsInstance<AllScheduleState.Loaded>()
                    .map { it.snapshot.pagingScheduled }
                    .distinctUntilChanged()
                    .collectLatest(scheduleAdapter::submitData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}