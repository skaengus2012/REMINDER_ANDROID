/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.feature.all.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.awaitPost
import com.nlab.reminder.core.android.view.inputmethod.hideSoftInputFromWindow
import com.nlab.reminder.core.android.view.setVisible
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.androidx.fragment.compose.ComposableFragment
import com.nlab.reminder.core.androidx.fragment.compose.ComposableInject
import com.nlab.reminder.core.androidx.fragment.viewLifecycle
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androix.recyclerview.itemTouches
import com.nlab.reminder.core.androix.recyclerview.scrollEvent
import com.nlab.reminder.core.androix.recyclerview.scrollState
import com.nlab.reminder.core.androix.recyclerview.stickyheader.StickyHeaderHelper
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.core.component.schedulelist.ScheduleListResource
import com.nlab.reminder.core.component.schedulelist.UserScheduleListResource
import com.nlab.reminder.core.component.schedulelist.internal.ui.AddLine
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleAdapterItem
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListAdapter
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListAnimator
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListHolderActivity
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListItemTouchCallback
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListSelectionHelper
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListSelectionSource
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListStickyHeaderAdapter
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScheduleListTheme
import com.nlab.reminder.core.component.schedulelist.internal.ui.ScrollGuard
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Repeat
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.ScheduleTiming
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.kotlinx.coroutines.flow.withPrev
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.all.databinding.FragmentAllBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.time.Clock

/**
 * @author Doohyun
 */
internal class AllFragment : ComposableFragment() {
    private var _binding: FragmentAllBinding? = null
    private val binding: FragmentAllBinding get() = checkNotNull(_binding)

    @ComposableInject
    lateinit var fragmentStateBridge: AllFragmentStateBridge

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentAllBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewReady(view: View, savedInstanceState: Bundle?) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val scheduleListAdapter = ScheduleListAdapter(
            theme = ScheduleListTheme.Point3,  // TODO extract
            timeZone = fragmentStateBridge.timeZoneState,
            entryAt = MutableStateFlow(Clock.System.now()), // TODO THIS IS FAKE, Minute Precision..
            triggerAtFormatPatterns = AllScheduleTriggerAtFormatPatterns(),  // TODO extract
        ).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        val scrollGuard = ScrollGuard()
            .also { binding.recyclerviewSchedule.addOnScrollListener(/*listener=*/ it) }
        val stickyHeaderAdapter = ScheduleListStickyHeaderAdapter(getCurrentList = scheduleListAdapter::getCurrentList)
            .also { scheduleListAdapter.registerAdapterDataObserver(it) }
        val scheduleListDragAnchorOverlay = run {
            val scheduleListHolderActivity = checkNotNull(activity as? ScheduleListHolderActivity) {
                "The hosting Activity is expected to be a ScheduleListHolderActivity but it's not."
            }
            scheduleListHolderActivity.requireScheduleListDragAnchorOverlay()
        }
        val stickyHeaderHelper = StickyHeaderHelper()
        val itemTouchCallback = ScheduleListItemTouchCallback(
            scrollGuard = scrollGuard,
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
        val multiSelectionHelper = ScheduleListSelectionHelper(
            selectionSource = ScheduleListSelectionSource(scheduleListAdapter),
            onSelectedStateChanged = { scheduleId, selected ->
                scheduleListAdapter.setSelected(scheduleId, selected)
            },
        )
        binding.recyclerviewSchedule.apply {
            adapter = scheduleListAdapter
            itemAnimator = ScheduleListAnimator()
            layoutManager = linearLayoutManager
            stickyHeaderHelper.attach(
                recyclerView = this,
                stickyHeaderContainer = binding.containerStickyHeader,
                stickyHeaderAdapter = stickyHeaderAdapter
            )
            itemTouchHelper.attachToRecyclerView(/* recyclerView=*/ this)
            multiSelectionHelper.attachToRecyclerView(recyclerView = this)
        }

        val scrollStateFlow = binding.recyclerviewSchedule.run {
            scrollState().shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        }
        val verticalScrollRangeFlow = binding.recyclerviewSchedule.run {
            verticalScrollRange().shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        }
        val scrollEventFlow = binding.recyclerviewSchedule.run {
            scrollEvent().shareIn(viewLifecycleScope, started = SharingStarted.Eagerly)
        }
        val recyclerViewItemTouchesFlow = binding.recyclerviewSchedule.run {
            itemTouches().shareIn(viewLifecycleScope, SharingStarted.Eagerly)
        }
        val firstVisiblePositionFlow = scrollEventFlow
            .map { linearLayoutManager.findFirstVisibleItemPosition() }
            .distinctUntilChanged()
            .shareIn(viewLifecycleScope, SharingStarted.Eagerly)
        val lastVisiblePositionFlow = scrollEventFlow
            .map { linearLayoutManager.findLastVisibleItemPosition() }
            .distinctUntilChanged()
            .shareIn(viewLifecycleScope, SharingStarted.Eagerly)

        combine(
            verticalScrollRangeFlow
                .map { it > 0 }
                .distinctUntilChanged(),
            firstVisiblePositionFlow
                .map { it > 0 }
                .distinctUntilChanged()
        ) { hasScrollRange, isHeadlineNotVisible -> hasScrollRange && isHeadlineNotVisible }
            .distinctUntilChanged()
            .onEach { fragmentStateBridge.isToolbarTitleVisible = it }
            .launchIn(viewLifecycleScope)

        firstVisiblePositionFlow
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
            .distinctUntilChanged()
            .onEach { fragmentStateBridge.toolbarBackgroundAlpha = it }
            .launchIn(viewLifecycleScope)

        merge(
            // When ItemTouchHelper doesn't work, feed x from itemTouches
            recyclerViewItemTouchesFlow.map { it.x },
            // When ItemTouchHelper is in drag operation, the touches are supplied from x
            binding.recyclerviewSchedule.touches().map { it.x }
        ).distinctUntilChanged()
            .onEach { itemTouchCallback.setContainerTouchX(it) }
            .launchIn(viewLifecycleScope)

        merge(
            recyclerViewItemTouchesFlow,
            fragmentStateBridge.itemSelectionEnabled
                .filter { it }
                .flowWithLifecycle(viewLifecycle),
            scrollStateFlow
                .distinctUntilChanged()
                .withPrev(RecyclerView.SCROLL_STATE_IDLE)
                .filter { (prev, cur) ->
                    prev == RecyclerView.SCROLL_STATE_IDLE && cur == RecyclerView.SCROLL_STATE_DRAGGING
                },
        ).onEach { itemTouchCallback.removeSwipeClamp(binding.recyclerviewSchedule) }
            .launchIn(viewLifecycleScope)

        fragmentStateBridge.itemSelectionEnabled
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

        scheduleListAdapter.addRequest
            .onEach { fragmentStateBridge.onSimpleAdd(it) }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.editRequest
            .onEach { fragmentStateBridge.onSimpleEdited(it) }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.focusChange
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
            scheduleListAdapter.focusChange
                .map { it.viewHolder.bindingAdapterPosition to it.focused },
            firstVisiblePositionFlow,
            lastVisiblePositionFlow
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

        scheduleListAdapter.dragHandleTouch
            .onEach { itemTouchHelper.startDrag(it) }
            .launchIn(viewLifecycleScope)

        scheduleListAdapter.selectButtonTouch
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

        viewLifecycleScope.launch {
            scheduleListAdapter.submitList(withContext(Dispatchers.Default) { testItems })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val testItems: List<ScheduleAdapterItem> by lazy {
            val imageSource = listOf(
                "https://i.namu.wiki/i/RyUyEbJKhi1iuG8y26lKjvMqjX8VzFUsk82z-9gqjV3KuIGg0krkOtcoZ69nvFREm9cuPbqQA7LSTt-LEfRjKA.webp",
                "https://img.kbs.co.kr/kbs/620/news.kbs.co.kr/data/fckeditor/new/image/2023/01/13/299931673597441715.png",
                "https://img.sbs.co.kr/newimg/news/20240726/201963680.jpg",
                null
            )

            buildList {
                this += ScheduleAdapterItem.Headline(StringIds.label_all)
                this += ScheduleAdapterItem.HeadlinePadding
                this += ScheduleAdapterItem.GroupHeader(
                    title = "어제",
                    subTitle = "Hello 어제"
                )
                repeat(times = 10) {
                    this += ScheduleAdapterItem.Content(
                        schedule = UserScheduleListResource(
                            resource = ScheduleListResource(
                                id = ScheduleId(it.toLong()),
                                title = "Title $it".toNonBlankString(),
                                note = "note $it".toNonBlankString(),
                                link = Link("https://www.naver.com/".toNonBlankString()),
                                linkMetadata = imageSource.shuffled().first()?.let { uri ->
                                    LinkMetadata(
                                        title = "네이버".toNonBlankString(),
                                        imageUrl = uri.tryToNonBlankStringOrNull()
                                    )
                                },
                                timing = ScheduleTiming(
                                    triggerAt = Clock.System.now(),
                                    isTriggerAtDateOnly = false,
                                    repeat = Repeat.Hourly(interval = 5.toPositiveInt())
                                ),
                                defaultVisiblePriority = it.toNonNegativeLong(),
                                isComplete = false,
                                tags = listOf(
                                    Tag(
                                        id = TagId(1),
                                        name = "여행".toNonBlankString()
                                    ),
                                    Tag(
                                        id = TagId(2),
                                        name = "공부".toNonBlankString()
                                    ),
                                    Tag(
                                        id = TagId(3),
                                        name = "이것은태그입니다만".toNonBlankString()
                                    ),
                                )
                            )
                        ),
                        isLineVisible = true
                    )
                }
                this += ScheduleAdapterItem.SubGroupHeader(
                    title = "오늘"
                )
                repeat(times = 10) {
                    this += ScheduleAdapterItem.Content(
                        schedule = UserScheduleListResource(
                            resource = ScheduleListResource(
                                id = ScheduleId(it.toLong()),
                                title = "Title $it".toNonBlankString(),
                                note = "note $it".toNonBlankString(),
                                link = Link("https://www.naver.com/".toNonBlankString()),
                                linkMetadata = imageSource.shuffled().first()?.let { uri ->
                                    LinkMetadata(
                                        title = "네이버".toNonBlankString(),
                                        imageUrl = uri.tryToNonBlankStringOrNull()
                                    )
                                },
                                timing = ScheduleTiming(
                                    triggerAt = Clock.System.now(),
                                    isTriggerAtDateOnly = false,
                                    repeat = Repeat.Hourly(interval = 5.toPositiveInt())
                                ),
                                defaultVisiblePriority = it.toNonNegativeLong(),
                                isComplete = false,
                                tags = listOf(
                                    Tag(
                                        id = TagId(1),
                                        name = "여행".toNonBlankString()
                                    ),
                                    Tag(
                                        id = TagId(2),
                                        name = "공부".toNonBlankString()
                                    ),
                                    Tag(
                                        id = TagId(3),
                                        name = "이것은태그입니다만".toNonBlankString()
                                    ),
                                )
                            )
                        ),
                        isLineVisible = true
                    )
                }
                this += ScheduleAdapterItem.FooterAdd(
                    newScheduleSource = null,
                    line = AddLine.Type1
                )
            }
        }
    }
}