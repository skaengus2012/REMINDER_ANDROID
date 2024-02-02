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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.recyclerview.SingleItemAdapter
import com.nlab.reminder.core.android.recyclerview.scrollState
import com.nlab.reminder.core.android.recyclerview.suspendSubmitList
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.kotlin.coroutine.flow.withBefore
import com.nlab.reminder.core.schedule.view.ScheduleItemAdapter
import com.nlab.reminder.core.schedule.view.ScheduleItemTouchCallback
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.android.navigation.navigateOpenLink
import com.nlab.reminder.domain.common.android.view.loadingFlow
import com.nlab.reminder.domain.common.schedule.view.ScheduleItemAnimator
import com.nlab.reminder.domain.feature.schedule.all.*
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
        val scheduleItemAdapter = ScheduleItemAdapter()
        val itemTouchCallback = ScheduleItemTouchCallback(
            context = requireContext(),
            itemMoveListener = scheduleItemAdapter
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

        binding.recyclerviewContent.apply {
            itemAnimator = ScheduleItemAnimator()
            adapter = ConcatAdapter(
                SingleItemAdapter(R.layout.view_item_empty), // Removing the header causes scrolling problems when completing the first item.
                scheduleItemAdapter
            )
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(/* recyclerView=*/ this)
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

        viewModel.loadedUiState
            .map { it.scheduleElements }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach { scheduleElements -> scheduleItemAdapter.suspendSubmitList(scheduleElements) }
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
            .map { it.not() }
            .onEach { isNotSelectionMode ->
                itemTouchCallback.isItemViewSwipeEnabled = isNotSelectionMode
                itemTouchCallback.isLongPressDragEnabled = isNotSelectionMode
            }
            .launchIn(viewLifecycleScope)

        merge(
            viewModel.loadedUiState,
            viewModel.uiState.loadingFlow<AllScheduleUiState.Empty>()
        )
            .flowWithLifecycle(viewLifecycle)
            .take(count = 1)
            .onEach { startPostponedEnterTransition() }
            .launchIn(viewLifecycleScope)


/**
        val diffCallback = ScheduleUiStateDiffCallback()
        val scheduleAdapter = DefaultScheduleUiStateAdapter(diffCallback)
        val itemTouchCallback = ScheduleItemTouchCallback(
            context = requireContext(),
            onItemMoved = scheduleAdapter::onMove,
            onItemMoveEnded = {
                diffCallback.setDragMode(true)
                val snapshot = scheduleAdapter.calculateDraggedSnapshot()
                if (snapshot is DragSnapshot.Success) {
                    viewModel.onDragScheduleEnded(snapshot.items)
                }
            }
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        val dragSelectionHelper = DragSelectionHelper(
            binding.recyclerviewContent,
            onSelectChanged = viewModel::onScheduleSelected
        )

        binding.recyclerviewContent
            .apply { itemAnimator = ScheduleItemAnimator() }
            .apply {
                adapter = scheduleAdapter
            }
            .apply { itemTouchHelper.attachToRecyclerView(this) }
            .apply { addOnItemTouchListener(dragSelectionHelper.itemTouchListener) }

        scheduleAdapter.itemEvent
            .filterIsInstance<ItemEvent.OnDeleteClicked>()
            .map { it.uiState.id }
            .onEach(viewModel::onDeleteScheduleClicked)
            .launchIn(viewLifecycleScope)

        scheduleAdapter.itemEvent
            .filterIsInstance<ItemEvent.OnLinkClicked>()
            .map { it.uiState.id }
            .onEach(viewModel::onScheduleLinkClicked)
            .launchIn(viewLifecycleScope)

        scheduleAdapter.itemEvent
            .filterIsInstance<ItemEvent.OnSelectTouched>()
            .onEach { event ->
                dragSelectionHelper.enableDragSelection(event.absolutePosition, event.curSelected.not())
            }
            .launchIn(viewLifecycleScope)

        scheduleAdapter.itemEvent
            .filterIsInstance<ItemEvent.OnDragHandleClicked>()
            .map { it.viewHolder }
            .onEach(itemTouchHelper::startDrag)
            .launchIn(viewLifecycleScope)

        binding.recyclerviewContent.touches()
            .map { it.x }
            .distinctUntilChanged()
            .onEach { itemTouchCallback.setContainerX(it) }
            .launchIn(viewLifecycleScope)

        viewLifecycle.event()
            .filterLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            .onEach { itemTouchCallback.clearResource() }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectionModeOnOff
            .throttleClicks()
            .onEach { viewModel.onToggleSelectionModeEnableClicked() }
            .launchIn(viewLifecycleScope)

        binding.buttonDeleteAllIfCompleted
            .throttleClicks()
            .onEach { viewModel.onDeleteCompletedScheduleClicked() }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemDeleted
            .throttleClicks()
            .onEach { viewModel.onSelectedScheduleDeleteClicked() }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemComplete
            .throttleClicks()
            .onEach { viewModel.onSelectedScheduleCompleteClicked(isComplete = true) }
            .launchIn(viewLifecycleScope)

        binding.buttonSelectedItemIncomplete
            .throttleClicks()
            .onEach { viewModel.onSelectedScheduleCompleteClicked(isComplete = false) }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.isCompletedScheduleShown }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { isDoneScheduleShown ->
                binding.buttonCompletedScheduleShownToggle.setText(
                    if (isDoneScheduleShown) R.string.schedule_completed_hidden
                    else R.string.schedule_completed_shown
                )
            }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.isSelectionMode }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleAdapter::setSelectionEnabled)
            .onEach { isSelectionMode -> itemTouchCallback.isItemViewSwipeEnabled = isSelectionMode.not() }
            .onEach { isSelectionMode -> itemTouchCallback.isLongPressDragEnabled = isSelectionMode.not() }
            .onEach { isSelectionMode ->
                if (isSelectionMode.not()) {
                    dragSelectionHelper.disableDragSelection()
                }
            }
            .onEach { isSelectionMode ->
                binding.buttonSelectionModeOnOff.setText(
                    if (isSelectionMode) R.string.schedule_selection_mode_off
                    else R.string.schedule_selection_mode_on
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
                .distinctUntilChanged()
                .flowWithLifecycle(viewLifecycle)
        )
            .onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewContent) }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<AllScheduleState.Loaded>()
            .map { it.scheduleUiStates }
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { items ->
                scheduleAdapter.suspendSubmitList(items)
                scheduleAdapter.adjustRecentSwapPositions()
                diffCallback.setDragMode(false)
            }
            .launchIn(viewLifecycleScope)

        viewModel.allScheduleSideEffectFlow
            .onEach(this::handleSideEffect)
            .launchIn(viewLifecycleScope)*/
    }

    /**
    private fun handleSideEffect(sideEffect: AllScheduleSideEffect) = when (sideEffect) {
        is AllScheduleSideEffect.ShowErrorPopup -> {
            // TODO implement error popup
        }
        is AllScheduleSideEffect.NavigateScheduleLink -> {
            requireActivity().navigateOpenLink(sideEffect.link)
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private val AllScheduleViewModel.loadedUiState: Flow<AllScheduleUiState.Loaded> get() = uiState.filterIsInstance()