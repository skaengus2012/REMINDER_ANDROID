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
import android.view.MotionEvent
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
import com.nlab.reminder.core.androix.recyclerview.itemUpdatesSimplified
import com.nlab.reminder.core.androix.recyclerview.scrollEvent
import com.nlab.reminder.core.androix.recyclerview.scrollState
import com.nlab.reminder.core.androix.recyclerview.stickyheader.StickyHeaderHelper
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.core.component.schedulelist.databinding.FragmentScheduleListBinding
import com.nlab.reminder.core.component.schedulelist.toolbar.ui.ScheduleListToolbarState
import com.nlab.reminder.core.component.schedulelist.bottombar.ui.ScheduleListBottomAppbarState
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.time.Instant

/**
 * @author Thalys
 */
internal class ScheduleListFragment : Fragment() {
    private var _binding: FragmentScheduleListBinding? = null
    private val binding: FragmentScheduleListBinding get() = checkNotNull(_binding)

    private val scheduleListItemsAdaptationsFlow = MutableIdentityStateFlow<ScheduleListItemsAdaptation>()
    private val multiSelectionEnabledFlow = MutableIdentityStateFlow<Boolean>()
    private val triggerAtFormatPatternsFlow = MutableIdentityStateFlow<TriggerAtFormatPatterns>()
    private val themeFlow = MutableIdentityStateFlow<ScheduleListTheme>()
    private val timeZoneFlow = MutableIdentityStateFlow<TimeZone>()
    private val entryAtFlow = MutableIdentityStateFlow<Instant>()
    private val listBottomScrollPaddingFlow = MutableIdentityStateFlow<Int>()
    private val toolbarStateFlow = MutableIdentityStateFlow<ScheduleListToolbarState?>()
    private val bottomAppbarStateFlow = MutableIdentityStateFlow<ScheduleListBottomAppbarState?>()
    private val selectionUpdateConsumerFlow = MutableIdentityStateFlow<(SelectionUpdate) -> Unit>()
    private val completedSchedulesCleanupConsumerFlow = MutableIdentityStateFlow<() -> Unit>()
    private val completionUpdateConsumerFlow = MutableIdentityStateFlow<(CompletionUpdate) -> Unit>()
    private val deleteConsumerFlow = MutableIdentityStateFlow<(Delete) -> Unit>()
    private val itemPositionUpdateConsumerFlow = MutableIdentityStateFlow<(ItemPositionUpdate) -> Unit>()
    private val openDetailConsumerFlow = MutableIdentityStateFlow<(OpenDetail) -> Unit>()
    private val simpleAddConsumerFlow = MutableIdentityStateFlow<(SimpleAdd) -> Unit>()
    private val simpleEditConsumerFlow = MutableIdentityStateFlow<(SimpleEdit) -> Unit>()

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
        val scheduleListAdapter = ScheduleListAdapter(viewLifecycleScope).apply {
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
            dragScaleAnimateDuration = 250L,
            swipeCancelAnimateDuration = 350L,
            clampSwipeThreshold = 0.5f,
            maxClampSwipeWidthMultiplier = 1.75f,
            itemMoveListener = object : ScheduleListItemTouchCallback.ItemMoveListener {
                override fun onMove(fromPosition: Int, toPosition: Int): Boolean {
                    val firstPos = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                    val lastPos = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    if (firstPos != RecyclerView.NO_POSITION
                        && lastPos != RecyclerView.NO_POSITION
                        && toPosition !in firstPos..lastPos
                    ) return false

                    return scheduleListAdapter.submitMoving(
                        fromPosition = fromPosition,
                        toPosition = toPosition
                    )
                }

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

        val scrollEvents = binding.recyclerviewSchedule
            .scrollEvent()
            .shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        val scrollStates = binding.recyclerviewSchedule
            .scrollState()
            .shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        val verticalScrollRanges = binding.recyclerviewSchedule
            .verticalScrollRange()
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
        val focusChanges = scheduleListAdapter.focusChanges
            .receiveAsFlow()
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
                toolbarStateFlow.unwrap().collectLatest { toolbarState ->
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
                toolbarStateFlow.unwrap().collectLatest { toolbarValues ->
                    if (toolbarValues == null) return@collectLatest
                    toolbarBackgroundAlphaState.collect { backgroundAlpha ->
                        if (backgroundAlpha != null) {
                            toolbarValues.backgroundAlpha = backgroundAlpha
                        }
                    }
                }
            }
        }

        val backgroundAlphaFlow = merge(
            scrollEvents,
            scheduleListAdapter.itemUpdatesSimplified().mapLatest {
                // await next frame
                binding.recyclerviewSchedule.awaitPost()
            }
        ).map {
            val rvHeight = binding.recyclerviewSchedule.height
            if (rvHeight == 0) {
                return@map 0f
            }

            val itemCount = scheduleListAdapter.itemCount
            if (itemCount <= 1) {
                // Empty or no footer
                return@map 0f
            }

            val firstView = linearLayoutManager.findViewByPosition(0)
            val lastView = linearLayoutManager.findViewByPosition(itemCount - 1)

            // Content does not fill the screen completely
            if (firstView != null && lastView != null && firstView.top >= 0 && lastView.bottom <= rvHeight) {
                return@map 0f
            }

            // The footer is not visible
            if (lastView == null || lastView.height <= 0) {
                return@map 1f
            }

            val visibleHeight = (rvHeight - lastView.top).coerceIn(0, lastView.height)
            1f - (visibleHeight.toFloat() / lastView.height)
        }.distinctUntilChanged()

        viewLifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bottomAppbarStateFlow.unwrap().collectLatest { bottomAppbarState ->
                    if (bottomAppbarState == null) return@collectLatest
                    backgroundAlphaFlow.collectLatest { backgroundAlpha ->
                        bottomAppbarState.backgroundAlpha = backgroundAlpha
                    }
                }
            }
        }


        val userInteractionSyncFlow = createUserInteractionSyncFlow()
        withSync(
            syncSourceFlow = userInteractionSyncFlow.unwrap().map { it.selectedIds },
            sync = scheduleListAdapter::syncSelected
        ) { syncJob ->
            viewLifecycleScope.launch {
                syncJob.join()
                viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    selectionUpdateConsumerFlow.unwrap().collectLatest { consumer ->
                        scheduleListAdapter.selectionUpdateRequests.unwrap().collect(consumer)
                    }
                }
            }
        }

        viewLifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                itemPositionUpdateConsumerFlow.unwrap().collectLatest { consumer ->
                    scheduleListAdapter.itemPositionUpdateRequests.unwrap().collect(consumer)
                }
            }
        }

        withSync(
            syncSourceFlow = userInteractionSyncFlow.unwrap().map { it.completionCheckedIds },
            sync = scheduleListAdapter::syncCompletionChecked,
        ) { syncJob ->
            forwardEventToConsumer(
                eventSource = scheduleListAdapter.completionUpdateRequests,
                eventConsumerFlow = completionUpdateConsumerFlow.unwrap(),
                awaitConsumerReady = { syncJob.join() }
            )
        }

        forwardEventToConsumer(
            eventSource = scheduleListAdapter.clearCompletedSchedulesRequests,
            eventConsumerFlow = completedSchedulesCleanupConsumerFlow
                .unwrap()
                .map { consumer -> { consumer() } }
        )

        forwardEventToConsumer(
            eventSource = scheduleListAdapter.deleteRequests,
            eventConsumerFlow = deleteConsumerFlow.unwrap()
        )

        forwardEventToConsumer(
            eventSource = scheduleListAdapter.openDetailRequests,
            eventConsumerFlow = openDetailConsumerFlow.unwrap()
        )

        forwardEventToConsumer(
            eventSource = scheduleListAdapter.addRequests,
            eventConsumerFlow = simpleAddConsumerFlow.unwrap()
        )

        forwardEventToConsumer(
            eventSource = scheduleListAdapter.editRequests,
            eventConsumerFlow = simpleEditConsumerFlow.unwrap()
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
            multiSelectionEnabledFlow.unwrap()
                .filter { enabled -> enabled }
                .flowWithLifecycle(viewLifecycle),
            scrollStates
                .distinctUntilChanged()
                .withPrev(RecyclerView.SCROLL_STATE_IDLE)
                .filter { (prev, cur) ->
                    prev == RecyclerView.SCROLL_STATE_IDLE && cur == RecyclerView.SCROLL_STATE_DRAGGING
                },
        ).onEach { itemTouchCallback.removeSwipeClamp() }
            .launchIn(viewLifecycleScope)

        recyclerViewItemTouches
            .filter { it.action == MotionEvent.ACTION_DOWN }
            .onEach { event ->
                val viewHolder = binding.recyclerviewSchedule
                    .findChildViewUnder(event.x, event.y)
                    ?.let { binding.recyclerviewSchedule.getChildViewHolder(it) }
                    ?: return@onEach
                itemTouchCallback.removeSwipeClampWith(viewHolder, touchX = event.x)
            }
            .launchIn(viewLifecycleScope)

        callbackFlow {
            val recyclerView = binding.recyclerviewSchedule
            val listener = object : RecyclerView.OnChildAttachStateChangeListener {
                fun dispatchViewHolderEvents(v: View) {
                    if (v.isLaidOut.not()) return
                    recyclerView.findContainingViewHolder(v)?.let { trySend(it) }
                }

                override fun onChildViewAttachedToWindow(v: View) = dispatchViewHolderEvents(v)
                override fun onChildViewDetachedFromWindow(v: View) = dispatchViewHolderEvents(v)
            }
            recyclerView.addOnChildAttachStateChangeListener(listener)
            awaitClose { recyclerView.removeOnChildAttachStateChangeListener(listener) }
        }.onEach { viewHolder -> itemTouchCallback.resetSwipeClamp(viewHolder) }
            .launchIn(viewLifecycleScope)

        multiSelectionEnabledFlow.unwrap()
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

        focusChanges
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
            focusChanges.map { it.viewHolder.bindingAdapterPosition to it.focused },
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

        viewLifecycleScope.launch {
            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                toolbarStateFlow.unwrap().collectLatest { toolbarState ->
                    if (toolbarState == null) return@collectLatest
                    focusChanges.collectLatest { event ->
                        if (event.focused) toolbarState.editCompleteVisible = true
                        else {
                            // Prevents the keyboard from lowering and then coming up immediately due to changing the input field.
                            delay(100)
                            toolbarState.editCompleteVisible = false
                        }
                    }
                }
            }
        }

        viewLifecycleScope.launch {
            for (viewHolder in scheduleListAdapter.dragHandleTouches) itemTouchHelper.startDrag(viewHolder)
        }

        viewLifecycleScope.launch {
            for (viewHolder in scheduleListAdapter.selectButtonTouches) multiSelectionHelper.enable(viewHolder)
        }

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

        val adjustPosEvents = Channel<Int>(
            capacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        viewLifecycleScope.launch {
            adjustPosEvents.receiveAsFlow().collectLatest { pos ->
                if (pos == RecyclerView.NO_POSITION) return@collectLatest

                binding.recyclerviewSchedule.run {
                    awaitPost()
                    if (linearLayoutManager.findFirstVisibleItemPosition() != pos) {
                       scrollToPosition(min(scheduleListAdapter.itemCount, pos))
                    }
                }
            }
        }

        scheduleListItemsAdaptationsFlow.unwrap()
            .distinctUntilChanged { prev, next ->
                if (prev is ScheduleListItemsAdaptation.Exist && next is ScheduleListItemsAdaptation.Exist) {
                    prev.replayStamp == next.replayStamp && prev.items.value == next.items.value
                } else {
                    prev == next
                }
            }
            .mapLatest { adaptation ->
                if (adaptation is ScheduleListItemsAdaptation.Exist && adaptation.replayStamp > 0) {
                    // A short delay for a smooth revert.
                    delay(500)
                }
                adaptation
            }
            .flowOn(Dispatchers.Default)
            .flowWithLifecycle(viewLifecycle)
            .onEach { adaptation ->
                val recyclerViewVisible: Boolean
                val newItems: List<ScheduleListItem>
                val replayStamp: Long
                when (adaptation) {
                    is ScheduleListItemsAdaptation.Absent -> {
                        recyclerViewVisible = false
                        newItems = emptyList()
                        replayStamp = 0
                    }

                    is ScheduleListItemsAdaptation.Exist -> {
                        recyclerViewVisible = true
                        newItems = adaptation.items
                        replayStamp = adaptation.replayStamp
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

                val firstVisiblePosBeforeUpdate =
                    if (replayStamp == 0L) RecyclerView.NO_POSITION
                    else linearLayoutManager.findFirstVisibleItemPosition()
                awaitCompleteWith {
                    scheduleListAdapter.submitList(
                        items = newItems,
                        commitCallback = { complete(Unit) }
                    )
                }
                adjustPosEvents.trySend(firstVisiblePosBeforeUpdate)
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

        triggerAtFormatPatternsFlow.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTriggerAtFormatPatterns)
            .launchIn(viewLifecycleScope)

        themeFlow.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTheme)
            .launchIn(viewLifecycleScope)

        timeZoneFlow.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateTimeZone)
            .launchIn(viewLifecycleScope)

        entryAtFlow.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach(scheduleListAdapter::updateEntryAt)
            .launchIn(viewLifecycleScope)

        listBottomScrollPaddingFlow.unwrap()
            .flowWithLifecycle(viewLifecycle)
            .onEach { value ->
                binding.recyclerviewSchedule.updatePadding(bottom = value)
                // TODO restore scroll position after updatePadding
            }
            .launchIn(viewLifecycleScope)
    }

    private fun createUserInteractionSyncFlow(): IdentityStateFlow<UserInteraction> {
        val resultFlow = MutableIdentityStateFlow<UserInteraction>()
        scheduleListItemsAdaptationsFlow.unwrap()
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
            .onEach(resultFlow::update)
            .launchIn(viewLifecycleScope)
        return resultFlow
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

    private fun <T> forwardEventToConsumer(
        eventSource: ReceiveChannel<T>,
        eventConsumerFlow: Flow<(T) -> Unit>,
        awaitConsumerReady: suspend () -> Unit = {}
    ) {
        viewLifecycleScope.launch {
            awaitConsumerReady()

            var currentConsumer: ((T) -> Unit)? = null
            launch { eventConsumerFlow.collect { currentConsumer = it } }

            viewLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                for (event in eventSource) currentConsumer?.invoke(event)
            }
        }
    }

    fun onScheduleListItemsAdaptationUpdated(scheduleListItemsAdaptation: ScheduleListItemsAdaptation) {
        scheduleListItemsAdaptationsFlow.update(scheduleListItemsAdaptation)
    }

    fun onMultiSelectionEnabledChanged(enabled: Boolean) {
        multiSelectionEnabledFlow.update(enabled)
    }

    fun onTriggerAtFormatPatternsUpdated(patterns: TriggerAtFormatPatterns) {
        triggerAtFormatPatternsFlow.update(patterns)
    }

    fun onThemeUpdated(theme: ScheduleListTheme) {
        themeFlow.update(theme)
    }

    fun onTimeZoneUpdated(timeZone: TimeZone) {
        timeZoneFlow.update(timeZone)
    }

    fun onEntryAtUpdated(entryAt: Instant) {
        entryAtFlow.update(entryAt)
    }

    fun onToolbarStateUpdated(toolbarState: ScheduleListToolbarState?) {
        toolbarStateFlow.update(toolbarState)
    }

    fun onBottomAppbarStateUpdated(state: ScheduleListBottomAppbarState?) {
        bottomAppbarStateFlow.update(state)
    }

    fun onListBottomScrollPaddingUpdated(value: Int) {
        listBottomScrollPaddingFlow.update(value)
    }

    fun onSelectionUpdateConsumerChanged(observer: (SelectionUpdate) -> Unit) {
        selectionUpdateConsumerFlow.update(observer)
    }

    fun onCompletedSchedulesCleanupConsumerChanged(consumer: ()-> Unit) {
        completedSchedulesCleanupConsumerFlow.update(consumer)
    }

    fun onCompletionUpdateConsumerChanged(consumer: (CompletionUpdate) -> Unit) {
        completionUpdateConsumerFlow.update(consumer)
    }

    fun onDeleteConsumerChanged(consumer: (Delete) -> Unit) {
        deleteConsumerFlow.update(consumer)
    }

    fun onItemPositionUpdateConsumerChanged(consumer: (ItemPositionUpdate) -> Unit) {
        itemPositionUpdateConsumerFlow.update(consumer)
    }

    fun onOpenDetailConsumerChanged(consumer: (OpenDetail) -> Unit) {
        openDetailConsumerFlow.update(consumer)
    }

    fun onSimpleAddConsumerChanged(consumer: (SimpleAdd) -> Unit) {
        simpleAddConsumerFlow.update(consumer)
    }

    fun onSimpleEditConsumerChanged(consumer: (SimpleEdit) -> Unit) {
        simpleEditConsumerFlow.update(consumer)
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