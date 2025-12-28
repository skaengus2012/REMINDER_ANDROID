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
import androidx.core.view.updatePadding
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
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarState
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
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

    private val scheduleListItemsAdaptationState = MutableIdentityStateFlow<ScheduleListItemsAdaptation>()
    private val multiSelectionEnabledState = MutableIdentityStateFlow<Boolean>()
    private val triggerAtFormatPatternsState = MutableIdentityStateFlow<TriggerAtFormatPatterns>()
    private val themeState = MutableIdentityStateFlow<ScheduleListTheme>()
    private val timeZoneState = MutableIdentityStateFlow<TimeZone>()
    private val entryAtState = MutableIdentityStateFlow<Instant>()
    private val listBottomScrollPaddingState = MutableIdentityStateFlow<Int>()
    private val toolbarStateState = MutableIdentityStateFlow<ScheduleListToolbarState?>()
    private val selectionUpdateConsumerState = MutableIdentityStateFlow<(SelectionUpdate) -> Unit>()
    private val completionUpdateConsumerState = MutableIdentityStateFlow<(CompletionUpdate) -> Unit>()
    private val simpleAddConsumerState = MutableIdentityStateFlow<(SimpleAdd) -> Unit>()
    private val simpleEditConsumerState = MutableIdentityStateFlow<(SimpleEdit) -> Unit>()

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

                    return item.resource.schedule.id
                }

                override fun findSelected(scheduleId: ScheduleId): Boolean {
                    return scheduleId in scheduleListAdapter.getCurrentSelectedIds()
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
                toolbarStateState.unwrap().collectLatest { toolbarState ->
                    if (toolbarState == null) return@collectLatest
                    toolbarVisibilityState.collect { titleVisible ->
                        if (titleVisible != null) {
                            toolbarState.titleVisible = titleVisible
                        }
                    }
                }
            }
        }

        val toolbarBackgroundAlphaState = combine(
            firstVisiblePositions.map { pos ->
                when (pos) {
                    0 -> 0f
                    // This is for HeadlinePadding
                    1 -> checkNotNull(linearLayoutManager.findViewByPosition(/*position =*/ 1))
                    else -> 1f
                }
            }.distinctUntilChanged(),
            scrollEvents
        ) { params, _ ->
            if (params is Float) params
            else {
                val itemView = params as View
                itemView.top.absoluteValue.toFloat() / itemView.height
            }
        }.stateIn(scope = viewLifecycleScope, started = SharingStarted.Eagerly, null)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toolbarStateState.unwrap().collectLatest { toolbarValues ->
                    if (toolbarValues == null) return@collectLatest
                    toolbarBackgroundAlphaState.collect { backgroundAlpha ->
                        if (backgroundAlpha != null) {
                            toolbarValues.backgroundAlpha = backgroundAlpha
                        }
                    }
                }
            }
        }

        val userInteractionSyncFlow = MutableIdentityStateFlow<UserInteraction>()
        scheduleListItemsAdaptationState.unwrap()
            .filterIsInstance<ScheduleListItemsAdaptation.Exist>()
            .map { adaptation ->
                val completionCheckedSchedulesIds = hashSetOf<ScheduleId>()
                val selectedSchedulesIds = hashSetOf<ScheduleId>()
                for (item in adaptation.items) {
                    if (item !is ScheduleListItem.Content) continue
                    val userScheduleListResource = item.resource
                    val scheduleId = userScheduleListResource.schedule.id
                    if (userScheduleListResource.completionChecked) {
                        completionCheckedSchedulesIds += scheduleId
                    }
                    if (userScheduleListResource.selected) {
                        selectedSchedulesIds += scheduleId
                    }
                }
                UserInteraction(
                    completionCheckedIds = completionCheckedSchedulesIds.toPersistentSet(),
                    selectedIds = selectedSchedulesIds.toPersistentSet()
                )
            }
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach(userInteractionSyncFlow::update)
            .launchIn(viewLifecycleScope)

        withSync(
            syncSourceFlow = userInteractionSyncFlow.unwrap()
                .debounce(200)
                .map { it.selectedIds },
            sync = scheduleListAdapter::syncSelected
        ) { syncJob ->
            viewLifecycleScope.launch {
                syncJob.join()
                viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    selectionUpdateConsumerState.unwrap().collectLatest { consumer ->
                        scheduleListAdapter.selectionUpdateRequests.unwrap().collect(consumer)
                    }
                }
            }
        }

        withSync(
            syncSourceFlow = userInteractionSyncFlow.unwrap()
                .debounce(750)
                .map { it.completionCheckedIds },
            sync = scheduleListAdapter::syncCompletionChecked,
        ) { syncJob ->
            forwardEventToConsumer(
                eventSourceFlow = scheduleListAdapter.completionUpdateRequests,
                eventConsumerFlow = completionUpdateConsumerState.unwrap(),
                awaitConsumerReady = { syncJob.join() }
            )
        }

        forwardEventToConsumer(
            eventSourceFlow = scheduleListAdapter.addRequests,
            eventConsumerFlow = simpleAddConsumerState.unwrap(),
            awaitConsumerReady = {}
        )

        forwardEventToConsumer(
            eventSourceFlow = scheduleListAdapter.editRequests,
            eventConsumerFlow = simpleEditConsumerState.unwrap(),
            awaitConsumerReady = {}
        )

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
            multiSelectionEnabledState.unwrap()
                .filter { enabled -> enabled }
                .flowWithLifecycle(viewLifecycle),
            scrollStates
                .distinctUntilChanged()
                .withPrev(RecyclerView.SCROLL_STATE_IDLE)
                .filter { (prev, cur) ->
                    prev == RecyclerView.SCROLL_STATE_IDLE && cur == RecyclerView.SCROLL_STATE_DRAGGING
                },
        ).onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewSchedule) }
            .launchIn(viewLifecycleScope)

        multiSelectionEnabledState.unwrap()
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
            .conflate()
            .onEach { itemTouchHelper.startDrag(it) }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.selectButtonTouches
            .conflate()
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

        scheduleListItemsAdaptationState.unwrap()
            .distinctUntilChanged { prev, next ->
                if (prev is ScheduleListItemsAdaptation.Exist && next is ScheduleListItemsAdaptation.Exist) {
                    prev.items.value == next.items.value
                } else {
                    prev == next
                }
            }
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach { adaptation ->
                val recyclerViewVisible: Boolean
                val newItems: List<ScheduleListItem>
                when (adaptation) {
                    is ScheduleListItemsAdaptation.Absent -> {
                        recyclerViewVisible = false
                        newItems = emptyList()
                    }

                    is ScheduleListItemsAdaptation.Exist -> {
                        recyclerViewVisible = true
                        newItems = adaptation.items
                    }
                }
                binding.recyclerviewSchedule.setVisible(
                    isVisible = recyclerViewVisible,
                    goneIfNotVisible = false
                )
                // If a drag is in progress, the list should be updated after the drag operation is completed
                // to ensure the ViewHolder is stable.
                awaitCompleteWith {
                    itemTouchCallback.stopDragging(
                        recyclerView = binding.recyclerviewSchedule,
                        commitCallback = { complete(Unit) }
                    )
                }
                awaitCompleteWith {
                    scheduleListAdapter.submitList(
                        items = newItems,
                        commitCallback = { complete(Unit) }
                    )
                }
            }
            .launchIn(viewLifecycleScope)

        viewLifecycle.eventFlow
            .filter { event -> event == Lifecycle.Event.ON_DESTROY }
            .onEach {
                stickyHeaderHelper.detach()
                itemTouchCallback.clearResource()
                multiSelectionHelper.clearResource()
            }
            .launchIn(viewLifecycleScope)

        triggerAtFormatPatternsState.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTriggerAtFormatPatterns)
            .launchIn(viewLifecycleScope)

        themeState.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTheme)
            .launchIn(viewLifecycleScope)

        timeZoneState.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTimeZone)
            .launchIn(viewLifecycleScope)

        entryAtState.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateEntryAt)
            .launchIn(viewLifecycleScope)

        listBottomScrollPaddingState.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach { value ->
                binding.recyclerviewSchedule.updatePadding(bottom = value)
                // TODO restore scroll position after updatePadding
            }
            .launchIn(viewLifecycleScope)
    }

    private inline fun <T> withSync(
        syncSourceFlow: Flow<T>,
        crossinline sync: (T) -> Unit,
        block: (Job) -> Unit
    ) {
        val firstSyncJob = CompletableDeferred<Unit>()
        syncSourceFlow
            .flowWithLifecycle(viewLifecycle)
            .onEach { sync(it); firstSyncJob.complete(Unit) }
            .launchIn(viewLifecycleScope)
        block(firstSyncJob)
    }

    private inline fun <T> forwardEventToConsumer(
        eventSourceFlow: SharedFlow<T>,
        eventConsumerFlow: Flow<(T) -> Unit>,
        crossinline awaitConsumerReady: suspend () -> Unit
    ) {
        val eventQueueState = MutableStateFlow<List<T>>(emptyList())
        eventSourceFlow
            .onEach { event -> eventQueueState.update { it + event } }
            .launchIn(viewLifecycleScope)
        viewLifecycleScope.launch {
            awaitConsumerReady()
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventConsumerFlow.collectLatest { consumer ->
                    eventQueueState.collect { data ->
                        val event = data.firstOrNull()
                        if (event != null) {
                            consumer.invoke(event)
                            eventQueueState.update { it - event }
                        }
                    }
                }
            }
        }
    }

    fun onScheduleListItemsAdaptationUpdated(scheduleListItemsAdaptation: ScheduleListItemsAdaptation) {
        scheduleListItemsAdaptationState.update(scheduleListItemsAdaptation)
    }

    fun onMultiSelectionEnabledChanged(enabled: Boolean) {
        multiSelectionEnabledState.update(enabled)
    }

    fun onTriggerAtFormatPatternsUpdated(patterns: TriggerAtFormatPatterns) {
        triggerAtFormatPatternsState.update(patterns)
    }

    fun onThemeUpdated(theme: ScheduleListTheme) {
        themeState.update(theme)
    }

    fun onTimeZoneUpdated(timeZone: TimeZone) {
        timeZoneState.update(timeZone)
    }

    fun onEntryAtUpdated(entryAt: Instant) {
        entryAtState.update(entryAt)
    }

    fun onToolbarStateUpdated(toolbarState: ScheduleListToolbarState?) {
        toolbarStateState.update(toolbarState)
    }

    fun onListBottomScrollPaddingUpdated(value: Int) {
        listBottomScrollPaddingState.update(value)
    }

    fun onSelectionUpdateConsumerChanged(observer: (SelectionUpdate) -> Unit) {
        selectionUpdateConsumerState.update(observer)
    }

    fun onCompletionUpdateConsumerChanged(consumer: (CompletionUpdate) -> Unit) {
        completionUpdateConsumerState.update(consumer)
    }

    fun onSimpleAddConsumerChanged(consumer: (SimpleAdd) -> Unit) {
        simpleAddConsumerState.update(consumer)
    }

    fun onSimpleEditConsumerChanged(consumer: (SimpleEdit) -> Unit) {
        simpleEditConsumerState.update(consumer)
    }
}

private suspend inline fun awaitCompleteWith(block: CompletableDeferred<Unit>.() -> Unit) {
    CompletableDeferred<Unit>()
        .also { it.block() }
        .await()
}

private data class UserInteraction(
    val completionCheckedIds: PersistentSet<ScheduleId>,
    val selectedIds: PersistentSet<ScheduleId>
)