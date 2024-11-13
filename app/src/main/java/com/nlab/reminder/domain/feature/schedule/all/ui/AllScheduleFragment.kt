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

package com.nlab.reminder.domain.feature.schedule.all.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.nlab.reminder.R
import com.nlab.reminder.core.androidx.fragment.viewLifecycle
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androidx.lifecycle.event
import com.nlab.reminder.core.androidx.lifecycle.filterLifecycleEvent
import com.nlab.reminder.core.androix.recyclerview.SingleItemAdapter
import com.nlab.reminder.core.androix.recyclerview.scrollState
import com.nlab.reminder.core.androix.recyclerview.suspendSubmitList
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.kotlinx.coroutine.flow.withBefore
import com.nlab.reminder.core.schedule.ui.*
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.android.navigation.navigateOpenLink
import com.nlab.reminder.domain.common.android.view.loadingFlow
import com.nlab.reminder.domain.feature.schedule.all.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class AllScheduleFragment : Fragment() {


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
        /**
        val scheduleItemAdapter = ScheduleItemAdapter()
        val itemTouchCallback = ScheduleItemTouchCallback(
            context = requireContext(),
            itemMoveListener = scheduleItemAdapter
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        val dragSelectionHelper = ScheduleItemDragSelectionHelper(
            recyclerView = binding.recyclerviewContent,
            onSelectChanged = viewModel::onScheduleSelected
        )

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnCompleteClicked>()
            .onEach { (position, isComplete) -> viewModel.onScheduleCompleteClicked(position, isComplete) }
            .launchIn(viewLifecycleScope)

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnItemMoveEnded>()
            .onEach { (fromPosition, toPosition) -> viewModel.onScheduleItemMoved(fromPosition, toPosition) }
            .launchIn(viewLifecycleScope)

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnLinkClicked>()
            .onEach { viewModel.onScheduleLinkClicked(it.position) }
            .launchIn(viewLifecycleScope)

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnDeleteClicked>()
            .onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewContent) }
            .onEach { viewModel.onScheduleDeleteClicked(it.position) }
            .launchIn(viewLifecycleScope)

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnSelectTouched>()
            .onEach { (absolutePosition, isSelected) ->
                dragSelectionHelper.enableDragSelection(absolutePosition, isSelected)
            }
            .launchIn(viewLifecycleScope)

        scheduleItemAdapter.itemEvent
            .filterIsInstance<ScheduleItemAdapter.ItemEvent.OnDragHandleClicked>()
            .map { it.viewHolder }
            .onEach { itemTouchHelper.startDrag(it) }
            .launchIn(viewLifecycleScope)

        viewLifecycle.event()
            .filterLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            .onEach { itemTouchCallback.clearResource() }
            .launchIn(viewLifecycleScope)

        binding.recyclerviewContent.apply {
            itemAnimator = ScheduleItemAnimator()
            adapter = ConcatAdapter(
                SingleItemAdapter(R.layout.view_item_empty), // Removing the header causes scrolling problems when completing the first item.
                scheduleItemAdapter
            )
            itemTouchHelper.attachToRecyclerView(/* recyclerView=*/ this)
            addOnItemTouchListener(dragSelectionHelper.itemTouchListener)
        }

        binding.recyclerviewContent.touches()
            .map { it.x }
            .distinctUntilChanged()
            .onEach { itemTouchCallback.setContainerX(it) }
            .launchIn(viewLifecycleScope)

        merge(
            binding.recyclerviewContent
                .scrollState()
                .distinctUntilChanged()
                .withBefore(SCROLL_STATE_IDLE)
                .filter { (prev, cur) -> prev == SCROLL_STATE_IDLE && cur == SCROLL_STATE_DRAGGING },
            viewModel.loadedUiState
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .flowWithLifecycle(viewLifecycle)
        )
            .onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewContent) }
            .launchIn(viewLifecycleScope)

        binding.buttonCompletedScheduleShownToggle
            .throttleClicks()
            .onEach { viewModel.onCompletedScheduleVisibilityToggleClicked() }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectionModeOnOff
            .throttleClicks()
            .onEach { viewModel.onSelectionModeToggleClicked() }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemComplete
            .throttleClicks()
            .onEach { viewModel.onSelectedSchedulesCompleteClicked(isComplete = true) }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemIncomplete
            .throttleClicks()
            .onEach { viewModel.onSelectedSchedulesCompleteClicked(isComplete = false) }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemDeleted
            .throttleClicks()
            .onEach { viewModel.onSelectedSchedulesDeleteClicked() }
            .launchIn(viewLifecycleScope)

        viewModel.loadedUiState
            .distinctUntilChangedBy { it.scheduleElements }
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach { action ->
                if (action.isSelectedActionInvoked) {
                    viewModel.appliedSelectedActionWithSchedules()
                    /**
                     * Set a term not to allow the selection change animation
                     * and list update to be performed at the same time.
                     */
                    delay(timeMillis = 300)
                }
                scheduleItemAdapter.suspendSubmitList(action.scheduleElements)
            }
            .launchIn(viewLifecycleScope)

        viewModel.loadedUiState
            .mapNotNull { it.workflows.firstOrNull() }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { workflow ->
                when (workflow) {
                    is AllScheduleWorkflow.LinkPage -> {
                        requireActivity().navigateOpenLink(workflow.link.value)
                        viewModel.completeWorkflow(workflow)
                    }
                }
            }
            .launchIn(viewLifecycleScope)

        viewModel.loadedUiState
            .map { uiState ->
                if (uiState.isCompletedScheduleShown) R.string.schedule_completed_hidden
                else R.string.schedule_completed_shown
            }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(binding.buttonCompletedScheduleShownToggle::setText)
            .launchIn(viewLifecycleScope)

        viewModel.loadedUiState
            .map { it.isSelectionMode }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { isSelectionMode ->
                binding.buttonSelectionModeOnOff.setText(
                    if (isSelectionMode) R.string.schedule_selection_mode_off
                    else R.string.schedule_selection_mode_on
                )
            }
            .onEach { isSelectionMode -> scheduleItemAdapter.selectionEnabled(isSelectionMode) }
            .map { it.not() }
            .onEach { isNotSelectionMode ->
                itemTouchCallback.isItemViewSwipeEnabled = isNotSelectionMode
                itemTouchCallback.isLongPressDragEnabled = isNotSelectionMode
            }
            .onEach { dragSelectionHelper.disableDragSelection() }
            .launchIn(viewLifecycleScope)

        merge(
            viewModel.loadedUiState,
            viewModel.uiState.loadingFlow<AllScheduleUiState.Empty>()
        )
            .flowWithLifecycle(viewLifecycle)
            .take(count = 1)
            .onEach { startPostponedEnterTransition() }
            .launchIn(viewLifecycleScope)*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}