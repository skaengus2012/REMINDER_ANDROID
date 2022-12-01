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
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.*
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.lifecycle.event
import com.nlab.reminder.core.android.recyclerview.DragSnapshot
import com.nlab.reminder.core.android.recyclerview.scrollState
import com.nlab.reminder.core.android.recyclerview.suspendSubmitList
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.kotlin.coroutine.flow.withBefore
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.android.view.loadingFlow
import com.nlab.reminder.domain.common.schedule.view.DefaultScheduleUiStateAdapter
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemAnimator
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemTouchCallback
import com.nlab.reminder.domain.feature.schedule.all.*
import dagger.hilt.android.AndroidEntryPoint
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
        val scheduleAdapter = DefaultScheduleUiStateAdapter(
            onCompleteClicked = { scheduleUiState ->
                viewModel.onModifyScheduleCompleteClicked(
                    scheduleId = scheduleUiState.id,
                    isComplete = scheduleUiState.isCompleteMarked.not()
                )
            },
            onDeleteClicked = { scheduleUiState -> viewModel.onDeleteScheduleClicked(scheduleUiState.id) },
            onLinkClicked = {
                // TODO implements link.
            }
        )
        val itemTouchCallback = ScheduleItemTouchCallback(
            context = requireContext(),
            onItemMoved = scheduleAdapter::onMove,
            onItemMoveEnded = {
                val snapshot = scheduleAdapter.calculateDraggedSnapshot()
                if (snapshot is DragSnapshot.Success) {
                    viewModel.onDragScheduleEnded(snapshot.items)
                }
            }
        )
        val scheduleItemAnimator = ScheduleItemAnimator()

        binding.recyclerviewContent
            .apply { ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this) }
            .apply { itemAnimator = scheduleItemAnimator }
            .apply { adapter = scheduleAdapter }

        viewLifecycle.event()
            .filter { event -> event == Lifecycle.Event.ON_DESTROY }
            .onEach { itemTouchCallback.clearResource() }
            .launchIn(viewLifecycleScope)

        binding.buttonCompletedScheduleShownToggle
            .throttleClicks()
            .onEach { viewModel.onToggleCompletedScheduleShownClicked() }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.snapshot }
            .map { it.isCompletedScheduleShown }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { isDoneScheduleShown ->
                binding.buttonCompletedScheduleShownToggle.setText(
                    if (isDoneScheduleShown) R.string.completed_schedule_hidden
                    else R.string.completed_schedule_shown
                )
            }
            .launchIn(viewLifecycleScope)

        merge(
            viewModel.stateFlow.filterIsInstance<AllScheduleState.Loaded>(),
            viewModel.stateFlow.loadingFlow<AllScheduleState.Loading>()
        )
            .flowWithLifecycle(viewLifecycle)
            .take(count = 1)
            .onEach { startPostponedEnterTransition() }
            .launchIn(viewLifecycleScope)

        merge(
            binding.recyclerviewContent
                .scrollState()
                .distinctUntilChanged()
                .withBefore(SCROLL_STATE_IDLE)
                .filter { (prev, cur) -> prev == SCROLL_STATE_IDLE && cur == SCROLL_STATE_DRAGGING },
            viewModel.stateFlow
                .filterIsInstance<AllScheduleState.Loaded>()
                .map { it.snapshot.scheduleUiStates }
                .distinctUntilChanged()
                .flowWithLifecycle(viewLifecycle)
        )
            .onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewContent) }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.snapshot.scheduleUiStates }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { items ->
                scheduleAdapter.suspendSubmitList(items)
                scheduleAdapter.adjustRecentSwapPositions()
            }
            .launchIn(viewLifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}