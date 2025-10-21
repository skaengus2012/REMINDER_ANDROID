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
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.awaitPost
import com.nlab.reminder.core.android.view.inputmethod.hideSoftInputFromWindow
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.androidx.fragment.viewLifecycle
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androix.recyclerview.itemTouches
import com.nlab.reminder.core.androix.recyclerview.scrollEvent
import com.nlab.reminder.core.androix.recyclerview.scrollState
import com.nlab.reminder.core.androix.recyclerview.stickyheader.StickyHeaderHelper
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.core.component.schedulelist.databinding.FragmentScheduleListBinding
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.math.absoluteValue
import kotlin.time.Instant

/**
 * @author Thalys
 */
internal class ScheduleListFragment : Fragment() {
    private var _binding: FragmentScheduleListBinding? = null
    private val binding: FragmentScheduleListBinding get() = checkNotNull(_binding)

    private val itemSelectionEnabledState = MutableStateFlow<Boolean?>(null)
    private val triggerAtFormatPatternsState = MutableStateFlow<TriggerAtFormatPatterns?>(null)
    private val themeState = MutableStateFlow<ScheduleListTheme?>(null)
    private val timeZoneState = MutableStateFlow<TimeZone?>(null)
    private val entryAtState = MutableStateFlow<Instant?>(null)
    private val toolbarVisibleChangedObserverState = MutableStateFlow<((Boolean) -> Unit)?>(null)
    private val toolbarBackgroundAlphaObserverState = MutableStateFlow<((Float) -> Unit)?>(null)
    private val simpleAddCommandObserverState = MutableStateFlow<((SimpleAdd) -> Unit)?>(null)
    private val simpleEditCommandObserverState = MutableStateFlow<((SimpleEdit) -> Unit)?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentScheduleListBinding
        .inflate(inflater, /* parent = */ container, /* attachToParent = */ false)
        .also { _binding = it }
        .root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scheduleListAdapter = ScheduleListAdapter().apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        val linearLayoutManager = LinearLayoutManager(/*context = */ requireContext())
        val scheduleListDragAnchorOverlay = run {
            val scheduleListHolderActivity = checkNotNull(activity as? ScheduleListHolderActivity) {
                "The hosting Activity is expected to be a ScheduleListHolderActivity but it's not."
            }
            scheduleListHolderActivity.requireScheduleListDragAnchorOverlay()
        }
        val itemTouchCallback = ScheduleListItemTouchCallback(
            scrollGuard = ScrollGuard()
                .also { binding.recyclerviewSchedule.addOnScrollListener(/*listener=*/ it) },
            scrollGuardMargin = dpToPx(/* dpValue =*/ 24f, resources.displayMetrics),
            dragAnchorOverlay = scheduleListDragAnchorOverlay,
            dragToScaleTargetHeight = dpToPx(/* dpValue =*/ 150f, resources.displayMetrics),
            animateDuration = 250L,
            clampSwipeThreshold = 0.5f,
            maxClampSwipeWidthMultiplier = 1.75f,
            itemMoveListener = object : ScheduleListItemTouchCallback.ItemMoveListener {
                override fun onMove(fromPosition: Int, toPosition: Int): Boolean = scheduleListAdapter.submitMoving(
                    fromPosition = fromPosition,
                    toPosition = toPosition
                )

                override fun onMoveEnded() {
                    scheduleListAdapter.submitMoveDone()
                }
            }
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        val stickyHeaderHelper = StickyHeaderHelper()
        val multiSelectionHelper = ScheduleListSelectionHelper(
            selectionSource = object : ScheduleListSelectionSource {
                override fun findScheduleId(position: Int): ScheduleId? {
                    val item = scheduleListAdapter.getCurrentList().getOrNull(position)
                    if (item !is ScheduleListItem.Content) return null

                    return item.schedule.resource.id
                }

                override fun findSelected(scheduleId: ScheduleId): Boolean {
                    return scheduleId in scheduleListAdapter.getCurrentSelected()
                }
            },
            onSelectedStateChanged = { scheduleId, selected ->
                scheduleListAdapter.setSelected(scheduleId, selected)
            },
        )
        with(binding) {
            recyclerviewSchedule.layoutManager = linearLayoutManager
            recyclerviewSchedule.adapter = scheduleListAdapter
            recyclerviewSchedule.itemAnimator = ScheduleListAnimator()
            stickyHeaderHelper.attach(
                recyclerView = recyclerviewSchedule,
                stickyHeaderContainer = containerStickyHeader,
                stickyHeaderAdapter = ScheduleListStickyHeaderAdapter(
                    getCurrentList = scheduleListAdapter::getCurrentList
                ).also { scheduleListAdapter.registerAdapterDataObserver(/*observer = */ it) }
            )
            itemTouchHelper.attachToRecyclerView(/* recyclerView=*/ recyclerviewSchedule)
            multiSelectionHelper.attachToRecyclerView(recyclerView = recyclerviewSchedule)
        }

        val scrollStates = binding.recyclerviewSchedule
            .scrollState()
            .shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        val verticalScrollRanges = binding.recyclerviewSchedule
            .verticalScrollRange()
            .shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        val scrollEvents = binding.recyclerviewSchedule
            .scrollEvent()
            .shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        val recyclerViewItemTouches = binding.recyclerviewSchedule
            .itemTouches()
            .shareIn(viewLifecycleScope, SharingStarted.Eagerly)
        val firstVisiblePositions = scrollEvents
            .map { linearLayoutManager.findFirstVisibleItemPosition() }
            .distinctUntilChanged()
            .shareIn(viewLifecycleScope, SharingStarted.Eagerly)
        val lastVisiblePositions = scrollEvents
            .map { linearLayoutManager.findLastVisibleItemPosition() }
            .distinctUntilChanged()
            .shareIn(viewLifecycleScope, SharingStarted.Eagerly)

        val toolbarVisibilityState = combine(
            verticalScrollRanges
                .map { it > 0 }
                .distinctUntilChanged(),
            firstVisiblePositions
                .map { it > 0 }
                .distinctUntilChanged()
        ) { hasScrollRange, isHeadlineNotVisible -> hasScrollRange && isHeadlineNotVisible }
            .stateIn(scope = viewLifecycleScope, started = SharingStarted.Eagerly, initialValue = null)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toolbarVisibleChangedObserverState.filterNotNull().collectLatest { observer ->
                    toolbarVisibilityState.filterNotNull().collect(collector = observer)
                }
            }
        }

        val toolbarBackgroundAlphaState = firstVisiblePositions
            .map { position ->
                when (position) {
                    0 -> 0f
                    1 -> {
                        val itemView = checkNotNull(linearLayoutManager.findViewByPosition(/*position =*/ 1))
                        itemView.top.absoluteValue.toFloat() / itemView.height
                    }

                    else -> 1f
                }
            }
            .stateIn(scope = viewLifecycleScope, started = SharingStarted.Eagerly, initialValue = null)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toolbarBackgroundAlphaObserverState.filterNotNull().collectLatest { observer ->
                    toolbarBackgroundAlphaState.filterNotNull().collect(collector = observer)
                }
            }
        }

        val simpleAddCommandsState = MutableStateFlow<List<SimpleAdd>>(emptyList())
        scheduleListAdapter.addRequests
            .onEach { simpleAdd -> simpleAddCommandsState.update { it + simpleAdd } }
            .launchIn(viewLifecycleScope)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                simpleAddCommandObserverState.filterNotNull().collectLatest { observer ->
                    simpleAddCommandsState.collect { simpleAdds ->
                        val command = simpleAdds.firstOrNull()
                        if (command != null) {
                            observer.invoke(command)
                            simpleAddCommandsState.update { it - command }
                        }
                    }
                }
            }
        }

        val simpleEditCommandState = MutableStateFlow<List<SimpleEdit>>(emptyList())
        scheduleListAdapter.editRequests
            .distinctUntilChanged()
            .onEach { simpleEdit -> simpleEditCommandState.update { it + simpleEdit } }
            .launchIn(viewLifecycleScope)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                simpleEditCommandObserverState.filterNotNull().collectLatest { observer ->
                    simpleEditCommandState.collect { simpleEdits ->
                        val command = simpleEdits.firstOrNull()
                        if (command != null) {
                            observer.invoke(command)
                            simpleEditCommandState.update { it - command }
                        }
                    }
                }
            }
        }

        merge(
            // When ItemTouchHelper doesn't work, feed x from itemTouches
            recyclerViewItemTouches.map { it.x },
            // When ItemTouchHelper is in drag operation, the touches are supplied from x
            binding.recyclerviewSchedule.touches().map { it.x }
        ).distinctUntilChanged()
            .onEach { itemTouchCallback.setContainerTouchX(it) }
            .launchIn(viewLifecycleScope)

        merge(
            recyclerViewItemTouches,
            itemSelectionEnabledState
                .filter { enabled -> enabled == true }
                .distinctUntilChanged()
                .flowWithLifecycle(viewLifecycle),
            scrollStates
                .distinctUntilChanged()
                .withPrev(RecyclerView.SCROLL_STATE_IDLE)
                .filter { (prev, cur) ->
                    prev == RecyclerView.SCROLL_STATE_IDLE && cur == RecyclerView.SCROLL_STATE_DRAGGING
                },
        ).onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewSchedule) }
            .launchIn(viewLifecycleScope)

        itemSelectionEnabledState
            .filterNotNull()
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach { enabled ->
                scheduleListAdapter.setSelectionEnabled(enabled)
                itemTouchCallback.isItemViewSwipeEnabled = enabled.not()
                itemTouchCallback.isLongPressDragEnabled = enabled.not()

                if (enabled.not()) {
                    multiSelectionHelper.disable()
                }
            }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.focusChanges
            .onEach { focusChanged ->
                if (focusChanged.focused) {
                    val receivedPosition = focusChanged.viewHolder.bindingAdapterPosition
                    view.awaitPost()
                    // Prevents position from changing due to UI size change depending on focus change.
                    if (receivedPosition == focusChanged.viewHolder.bindingAdapterPosition) {
                        binding.recyclerviewSchedule.scrollToPosition(receivedPosition)
                    }
                }
            }
            .launchIn(viewLifecycleScope)

        combine(
            scheduleListAdapter.focusChanges
                .map { it.viewHolder.bindingAdapterPosition to it.focused },
            firstVisiblePositions,
            lastVisiblePositions
        ) { (viewHolderPosition, focused), firstVisiblePosition, lastVisiblePosition ->
            when {
                focused -> 0 // focused
                viewHolderPosition in firstVisiblePosition..lastVisiblePosition -> 1 // not focused
                else -> 2 // focused by scrolling
            }
        }.distinctUntilChanged()
            .mapLatest { flag ->
                if (flag == 0) true
                else {
                    // Prevents the keyboard from lowering and then coming up immediately due to changing the input field.
                    if (flag == 1) {
                        delay(100)
                    }
                    false
                }
            }
            .withPrev()
            .distinctUntilChanged()
            .onEach { (prevFocused, curFocused) ->
                if (prevFocused && curFocused.not()) {
                    view.hideSoftInputFromWindow()
                }
            }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.dragHandleTouches
            .onEach { itemTouchHelper.startDrag(it) }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.selectButtonTouches
            .onEach { multiSelectionHelper.enable(it) }
            .launchIn(viewLifecycleScope)

        viewLifecycle.eventFlow
            .mapNotNull { event ->
                when (event) {
                    Lifecycle.Event.ON_START -> true
                    Lifecycle.Event.ON_STOP -> false
                    else -> null
                }
            }
            .distinctUntilChanged()
            .onEach { isVisible -> scheduleListDragAnchorOverlay.setVisible(isVisible) }
            .launchIn(viewLifecycleScope)

        viewLifecycle.eventFlow
            .filter { event -> event == Lifecycle.Event.ON_DESTROY }
            .onEach {
                stickyHeaderHelper.detach()
                itemTouchCallback.clearResource()
                multiSelectionHelper.clearResource()
            }
            .launchIn(viewLifecycleScope)

        triggerAtFormatPatternsState.filterNotNull()
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTriggerAtFormatPatterns)
            .launchIn(viewLifecycleScope)

        themeState.filterNotNull()
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTheme)
            .launchIn(viewLifecycleScope)

        timeZoneState.filterNotNull()
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTimeZone)
            .launchIn(viewLifecycleScope)

        entryAtState.filterNotNull()
            .distinctUntilChanged()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateEntryAt)
            .launchIn(viewLifecycleScope)
    }

    fun onItemSelectionEnabledChanged(enabled: Boolean) {
        itemSelectionEnabledState.value = enabled
    }

    fun onTriggerAtFormatPatternsUpdated(patterns: TriggerAtFormatPatterns) {
        triggerAtFormatPatternsState.value = patterns
    }

    fun onThemeUpdated(theme: ScheduleListTheme?) {
        themeState.value = theme
    }

    fun onTimeZoneUpdated(timeZone: TimeZone) {
        timeZoneState.value = timeZone
    }

    fun onEntryAtUpdated(entryAt: Instant) {
        entryAtState.value = entryAt
    }

    fun onToolbarVisibleChangedObserverChanged(observer: (Boolean) -> Unit) {
        toolbarVisibleChangedObserverState.value = observer
    }

    fun onToolbarBackgroundAlphaChangedObserverChanged(observer: (Float) -> Unit) {
        toolbarBackgroundAlphaObserverState.value = observer
    }

    fun onSimpleAddCommandObserverChanged(observer: (SimpleAdd) -> Unit) {
        simpleAddCommandObserverState.value = observer
    }

    fun onSimpleEditCommandObserverChanged(observer: (SimpleEdit) -> Unit) {
        simpleEditCommandObserverState.value = observer
    }
}