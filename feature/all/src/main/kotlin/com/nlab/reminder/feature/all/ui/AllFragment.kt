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
import com.nlab.reminder.core.androidx.fragment.compose.ComposableFragment
import com.nlab.reminder.core.androidx.fragment.compose.ComposableInject
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import com.nlab.reminder.core.androix.recyclerview.verticalScrollRange
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleAdapterItem
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListAdapter
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListAnimator
import com.nlab.reminder.core.component.schedule.ui.view.list.ScheduleListTheme
import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.model.Schedule
import com.nlab.reminder.core.data.model.ScheduleContent
import com.nlab.reminder.core.data.model.ScheduleDetail
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.all.databinding.FragmentAllBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Doohyun
 */
internal class AllFragment : ComposableFragment() {
    private var _binding: FragmentAllBinding? = null
    private val binding: FragmentAllBinding get() = checkNotNull(_binding)

    @ComposableInject
    lateinit var fragmentStateBridge: AllFragmentStateBridge

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FragmentAllBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onComposed() {
        binding.recyclerviewSchedule
            .verticalScrollRange()
            .distinctUntilChanged()
            .onEach { fragmentStateBridge.verticalScrollRange = it }
            .launchIn(viewLifecycleScope)

        val scheduleListAdapter = ScheduleListAdapter(theme = ScheduleListTheme.Point3)
        binding.recyclerviewSchedule.apply {
            adapter = scheduleListAdapter
            itemAnimator = ScheduleListAnimator()
        }

        viewLifecycleScope.launch {
            delay(500)
            val items = withContext(Dispatchers.Default) {
                val imageSource = listOf(
                    "https://i.namu.wiki/i/RyUyEbJKhi1iuG8y26lKjvMqjX8VzFUsk82z-9gqjV3KuIGg0krkOtcoZ69nvFREm9cuPbqQA7LSTt-LEfRjKA.webp",
                    "https://img.kbs.co.kr/kbs/620/news.kbs.co.kr/data/fckeditor/new/image/2023/01/13/299931673597441715.png",
                    "https://img.sbs.co.kr/newimg/news/20240726/201963680.jpg",
                    null
                )

                buildList {
                    this += ScheduleAdapterItem.Headline(StringIds.label_all)
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
                            )
                        )
                    }
                }
            }
            scheduleListAdapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}