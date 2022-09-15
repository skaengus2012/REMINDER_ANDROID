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

package com.nlab.reminder.domain.feature.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nlab.reminder.core.android.view.initWithLifecycleOwner
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.ViewItemHomeCategoriesBinding
import com.nlab.reminder.domain.feature.home.NotificationUiState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author thalys
 */
class HomeCategoryAdapter(
    private val onTodayNavClicked: () -> Unit,
    private val onTimetableNavClicked: () -> Unit,
    private val onAllNavClicked: () -> Unit
) : ListAdapter<NotificationUiState, HomeCategoryAdapter.ViewHolder>(NotificationSnapshotDiffItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewItemHomeCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onTodayNavClicked,
            onTimetableNavClicked,
            onAllNavClicked
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(
        private val binding: ViewItemHomeCategoriesBinding,
        onTodayNavClicked: () -> Unit,
        onTimetableNavClicked: () -> Unit,
        onAllNavClicked: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.initWithLifecycleOwner { lifecycleOwner ->
                btnNavToday
                    .throttleClicks()
                    .onEach { onTodayNavClicked() }
                    .launchIn(lifecycleOwner.lifecycleScope)

                btnNavTimetable
                    .throttleClicks()
                    .onEach { onTimetableNavClicked() }
                    .launchIn(lifecycleOwner.lifecycleScope)

                btnNavAll
                    .throttleClicks()
                    .onEach { onAllNavClicked() }
                    .launchIn(lifecycleOwner.lifecycleScope)
            }
        }

        fun onBind(snapshot: NotificationUiState) = with(binding) {
            textviewCountToday.text = snapshot.todayCount
            textviewCountTimetable.text = snapshot.timetableCount
            textviewCountAll.text = snapshot.allCount
        }
    }
}