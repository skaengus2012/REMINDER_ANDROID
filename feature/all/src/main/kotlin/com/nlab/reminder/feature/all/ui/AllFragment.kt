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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.eventFlow
import androidx.lifecycle.flowWithLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.awaitPost
import com.nlab.reminder.core.android.view.inputmethod.hideSoftInputFromWindow
import com.nlab.reminder.core.android.view.touches
import com.nlab.reminder.core.androidx.fragment.compose.ComposableFragment
import com.nlab.reminder.core.androidx.fragment.compose.ComposableInject
import com.nlab.reminder.core.androidx.fragment.viewLifecycle
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androix.recyclerview.scrollEvent
import com.nlab.reminder.core.androix.recyclerview.scrollState
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.core.component.schedule.ui.view.list.AddLine
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleAdapterItem
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListAdapter
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListAnimator
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListItemTouchCallback
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListSelectionHelper
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListSelectionSource
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListTheme
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleContent
import com.nlab.reminder.core.data.model.ScheduleDetail
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlinx.coroutine.flow.withPrev
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.all.databinding.FragmentAllBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

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
        val scheduleListAdapter = ScheduleListAdapter(theme = ScheduleListTheme.Point3).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        val itemTouchCallback = ScheduleListItemTouchCallback(
            context = requireContext(),
            itemMoveListener = object : ScheduleListItemTouchCallback.ItemMoveListener {
                override fun onMove(
                    fromViewHolder: RecyclerView.ViewHolder,
                    toViewHolder: RecyclerView.ViewHolder
                ): Boolean = scheduleListAdapter.submitMoving(
                    fromPosition = fromViewHolder.bindingAdapterPosition,
                    toPosition = toViewHolder.bindingAdapterPosition
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
            itemTouchHelper.attachToRecyclerView(/* recyclerView=*/ this)
            multiSelectionHelper.attachToRecyclerView(this)
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

        binding.recyclerviewSchedule.touches()
            .map { it.x }
            .distinctUntilChanged()
            .onEach { itemTouchCallback.setContainerTouchX(it) }
            .launchIn(viewLifecycleScope)

        merge(
            fragmentStateBridge.itemSelectionEnabled
                .filter { it }
                .flowWithLifecycle(viewLifecycle),
            scrollStateFlow
                .distinctUntilChanged()
                .withPrev(RecyclerView.SCROLL_STATE_IDLE)
                .filter { (prev, cur) ->
                    prev == RecyclerView.SCROLL_STATE_IDLE && cur == RecyclerView.SCROLL_STATE_DRAGGING
                }
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
                    val receivedPosition = focusChanged.viewHolder.absoluteAdapterPosition
                    view.awaitPost()
                    // Prevents position from changing due to UI size change depending on focus change.
                    if (receivedPosition == focusChanged.viewHolder.absoluteAdapterPosition) {
                        binding.recyclerviewSchedule.scrollToPosition(receivedPosition)
                    }
                }
            }
            .launchIn(viewLifecycleScope)

        combine(
            scheduleListAdapter.focusChange
                .map { it.viewHolder.absoluteAdapterPosition to it.focused },
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
            .filter { event -> event == Lifecycle.Event.ON_DESTROY }
            .onEach {
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
                repeat(times = 10) {
                    this += ScheduleAdapterItem.Content(
                        scheduleDetail = ScheduleDetail(
                            schedule = Schedule(
                                id = ScheduleId(it.toLong()),
                                content = ScheduleContent(
                                    title = "Title $it",
                                    note = "note $it".toNonBlankString(),
                                    link = Link(
                                        "https://www.naver.com/".toNonBlankString()
                                    )
                                ),
                                isComplete = true,
                                visiblePriority = it.toLong().toNonNegativeLong()
                            ),
                            tags = emptySet(),
                            linkMetadata = imageSource.shuffled().first()?.let {
                                LinkMetadata(
                                    title = "네이버",
                                    imageUrl = it
                                )
                            }
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