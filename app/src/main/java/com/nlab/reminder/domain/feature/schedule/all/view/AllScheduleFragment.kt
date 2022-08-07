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
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.recyclerview.suspendSubmitList
import com.nlab.reminder.databinding.FragmentAllScheduleBinding
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.view.ScheduleItem
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleState
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleViewModel
import com.nlab.reminder.domain.feature.schedule.all.onScheduleCompleteUpdateClicked
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
        val doingScheduleAdapter = DoingScheduleItemAdapter()
        val doneScheduleAdapter = DoneSchedulePagingDataAdapter()
        val scheduleAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build(),
            doingScheduleAdapter,
        )
        val renderWhenLoaded = renderWhenLoadedFunc(scheduleAdapter, doingScheduleAdapter, doneScheduleAdapter)

        binding.contentRecyclerview
            .apply { adapter = scheduleAdapter }

        viewModel.state
            .filterIsInstance<AllScheduleState.Loaded>()
            .flowWithLifecycle(viewLifecycle)
            .map { it.allSchedulesReport }
            .distinctUntilChanged()
            .map { report -> AllScheduleLoadedSnapshot(report, scheduleItemFactory = ::createScheduleItem) }
            .flowOn(Dispatchers.Default)
            .onEach { snapshot -> renderWhenLoaded(snapshot) }
            .launchIn(viewLifecycleScope)

        /**
        viewModel.state
            .filterIsInstance<AllScheduleState.Loaded>()
            .flowWithLifecycle(viewLifecycle)
            .map { it.allSchedulesReport.doneSchedules }
            .distinctUntilChanged()
            .onEach { pagingData -> doneScheduleAdapter.submitData(pagingData.map(::createScheduleItem)) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewLifecycleScope)*/
    }

    private fun createScheduleItem(schedule: Schedule): ScheduleItem = ScheduleItem(
        schedule,
        onCompleteToggleClicked = {
            viewModel.onScheduleCompleteUpdateClicked(schedule.id(), isComplete = schedule.isComplete.not())
        }
    )

    private fun renderWhenLoadedFunc(
        scheduleAdapter: ConcatAdapter,
        doingScheduleAdapter: DoingScheduleItemAdapter,
        doneScheduleAdapter: DoneSchedulePagingDataAdapter
    ): suspend (AllScheduleLoadedSnapshot) -> Unit = { snapshot ->
        doingScheduleAdapter.suspendSubmitList(snapshot.doingScheduleItems)
        startPostponedEnterTransition()

        if (snapshot.isDoneScheduleShown) scheduleAdapter.addAdapter(doneScheduleAdapter)
        else scheduleAdapter.removeAdapter(doneScheduleAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}