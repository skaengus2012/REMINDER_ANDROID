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
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.databinding.FragmentScheduleListBinding
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarState
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
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

    private val scheduleListItemsAdaptationStream = mutableLatestEventFlow<ScheduleListItemsAdaptation>()
    private val multiSelectionEnabledStream = mutableLatestEventFlow<Boolean>()
    private val triggerAtFormatPatternsStream = mutableLatestEventFlow<TriggerAtFormatPatterns>()
    private val themeStream = mutableLatestEventFlow<ScheduleListTheme>()
    private val timeZoneStream = mutableLatestEventFlow<TimeZone>()
    private val entryAtStream = mutableLatestEventFlow<Instant>()
    private val listBottomScrollPaddingStream = mutableLatestEventFlow<Int>()
    private val toolbarStateStream = mutableLatestEventFlow<ScheduleListToolbarState?>()
    private val itemSelectionChangeConsumerFlow = mutableLatestEventFlow<(Set<ScheduleId>) -> Unit>()
    private val completionUpdateConsumerStream = mutableLatestEventFlow<(CompletionUpdated) -> Unit>()
    private val simpleAddConsumerStream = mutableLatestEventFlow<(SimpleAdd) -> Unit>()
    private val simpleEditConsumerStream = mutableLatestEventFlow<(SimpleEdit) -> Unit>()

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
                    return scheduleId in scheduleListAdapter.selectedScheduleIds.value
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
                toolbarStateStream.collectLatest { toolbarState ->
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
                toolbarStateStream.collectLatest { toolbarValues ->
                    if (toolbarValues == null) return@collectLatest
                    toolbarBackgroundAlphaState.collect { backgroundAlpha ->
                        if (backgroundAlpha != null) {
                            toolbarValues.backgroundAlpha = backgroundAlpha
                        }
                    }
                }
            }
        }

        syncAndModifyScheduleListState(
            syncDebounce = 200, // selection animation duration
            syncSource = { adaptation ->
                adaptation.toScheduleIdsBy { it.selected }
            },
            applySource = scheduleListAdapter::syncSelected,
            forwardChangedStateToConsumer = {
                itemSelectionChangeConsumerFlow.collectLatest { consumer ->
                    scheduleListAdapter.selectedScheduleIds.collect(consumer::invoke)
                }
            }
        )

        syncAndModifyScheduleListState(
            syncDebounce = 1000, // completion debounce time with buffer
            syncSource = { adaptation ->
                adaptation.toScheduleIdsBy { it.completionChecked }
            },
            applySource = scheduleListAdapter::syncCompletion,
            forwardChangedStateToConsumer = {
                completionUpdateConsumerStream.collectLatest { consumer ->
                    scheduleListAdapter.completionUpdateRequests.collect(consumer::invoke)
                }
            }
        )

        forwardScheduleListEventToConsumer(
            scheduleListViewEventFlow = scheduleListAdapter.addRequests,
            viewEventConsumerFlow = simpleAddConsumerStream
        )

        forwardScheduleListEventToConsumer(
            scheduleListViewEventFlow = scheduleListAdapter.editRequests,
            viewEventConsumerFlow = simpleEditConsumerStream
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
            multiSelectionEnabledStream
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

        multiSelectionEnabledStream
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

        scheduleListItemsAdaptationStream
            .distinctUntilChanged { prev, next ->
                if (prev is ScheduleListItemsAdaptation.Exist && next is ScheduleListItemsAdaptation.Exist) {
                    prev.items.value == next.items.value
                } else {
                    prev == next
                }
            }
            .flowOn(Dispatchers.Default)
            .conflate()
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

        triggerAtFormatPatternsStream
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTriggerAtFormatPatterns)
            .launchIn(viewLifecycleScope)

        themeStream
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTheme)
            .launchIn(viewLifecycleScope)

        timeZoneStream
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTimeZone)
            .launchIn(viewLifecycleScope)

        entryAtStream
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateEntryAt)
            .launchIn(viewLifecycleScope)

        listBottomScrollPaddingStream
            .flowWithLifecycle(viewLifecycle)
            .onEach { value ->
                binding.recyclerviewSchedule.updatePadding(bottom = value)
                // TODO restore scroll position after updatePadding
            }
            .launchIn(viewLifecycleScope)
    }

    private fun <T> syncAndModifyScheduleListState(
        syncDebounce: Long,
        syncSource: (ScheduleListItemsAdaptation.Exist) -> T,
        applySource: (T) -> Unit,
        forwardChangedStateToConsumer: suspend () -> Unit
    ) {
        val awaitUserInteractionSyncJob = CompletableDeferred<Unit>()
        scheduleListItemsAdaptationStream
            .transformLatest { adaptation ->
                when (adaptation) {
                    is ScheduleListItemsAdaptation.Absent -> {
                        // If the adaptation is not 'Exist', cancel any ongoing delay/work and wait for the next item.
                        return@transformLatest
                    }

                    is ScheduleListItemsAdaptation.Exist -> {
                        // no-op
                    }
                }
                if (awaitUserInteractionSyncJob.isCompleted) {
                    // debounce time
                    // It's not need to sync all data.
                    //
                    // This is because it is only necessary in the following cases.
                    // case1. configuration should be synced at the time of the change.
                    // case2.
                    //      When the selection is canceled, it must be a sync. (case. multi selection)
                    delay(syncDebounce)
                }

                emit(syncSource(adaptation))
            }
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach { source ->
                applySource(source)
                if (awaitUserInteractionSyncJob.isActive) {
                    awaitUserInteractionSyncJob.complete(Unit)
                }
            }
            .launchIn(viewLifecycleScope)
        viewLifecycleScope.launch {
            awaitUserInteractionSyncJob.await()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                forwardChangedStateToConsumer()
            }
        }
    }

    private fun <T> forwardScheduleListEventToConsumer(
        scheduleListViewEventFlow: SharedFlow<T>,
        viewEventConsumerFlow: MutableSharedFlow<(T) -> Unit>
    ) {
        val viewEventQueue = MutableStateFlow<List<T>>(emptyList())
        scheduleListViewEventFlow
            .onEach { event -> viewEventQueue.update { it + event } }
            .launchIn(viewLifecycleScope)
        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewEventConsumerFlow.collectLatest { consumer ->
                    viewEventQueue.collect { data ->
                        val event = data.firstOrNull()
                        if (event != null) {
                            consumer.invoke(event)
                            viewEventQueue.update { it - event }
                        }
                    }
                }
            }
        }
    }

    fun onScheduleListItemsAdaptationUpdated(scheduleListItemsAdaptation: ScheduleListItemsAdaptation) {
        scheduleListItemsAdaptationStream.tryEmit(scheduleListItemsAdaptation)
    }

    fun onMultiSelectionEnabledChanged(enabled: Boolean) {
        multiSelectionEnabledStream.tryEmit(enabled)
    }

    fun onTriggerAtFormatPatternsUpdated(patterns: TriggerAtFormatPatterns) {
        triggerAtFormatPatternsStream.tryEmit(patterns)
    }

    fun onThemeUpdated(theme: ScheduleListTheme) {
        themeStream.tryEmit(theme)
    }

    fun onTimeZoneUpdated(timeZone: TimeZone) {
        timeZoneStream.tryEmit(timeZone)
    }

    fun onEntryAtUpdated(entryAt: Instant) {
        entryAtStream.tryEmit(entryAt)
    }

    fun onToolbarStateUpdated(toolbarState: ScheduleListToolbarState?) {
        toolbarStateStream.tryEmit(toolbarState)
    }

    fun onListBottomScrollPaddingUpdated(value: Int) {
        listBottomScrollPaddingStream.tryEmit(value)
    }

    fun onItemSelectionChangedConsumerChanged(observer: (Set<ScheduleId>) -> Unit) {
        itemSelectionChangeConsumerFlow.tryEmit(observer)
    }

    fun onCompletionUpdateConsumerChanged(consumer: (CompletionUpdated) -> Unit) {
        completionUpdateConsumerStream.tryEmit(consumer)
    }

    fun onSimpleAddConsumerChanged(consumer: (SimpleAdd) -> Unit) {
        simpleAddConsumerStream.tryEmit(consumer)
    }

    fun onSimpleEditConsumerChanged(consumer: (SimpleEdit) -> Unit) {
        simpleEditConsumerStream.tryEmit(consumer)
    }
}

/**
 * Emit is only executed when the value is changed to LaunchedEffect outside of the Fragment.
 * Therefore, control external changes with a single buffer without distinct function.
 */
private fun <T> mutableLatestEventFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)

private suspend inline fun awaitCompleteWith(block: CompletableDeferred<Unit>.() -> Unit) {
    CompletableDeferred<Unit>()
        .also { it.block() }
        .await()
}

private fun ScheduleListItemsAdaptation.Exist.toScheduleIdsBy(
    predicate: (UserScheduleListResource) -> Boolean
): PersistentSet<ScheduleId> {
    val selectedSchedulesIds = hashSetOf<ScheduleId>()
    for (item in items) {
        if (item !is ScheduleListItem.Content) continue
        val userScheduleListResource = item.resource
        if (predicate(userScheduleListResource)) {
            selectedSchedulesIds += item.resource.schedule.id
        }
    }
    return selectedSchedulesIds.toPersistentHashSet()
}