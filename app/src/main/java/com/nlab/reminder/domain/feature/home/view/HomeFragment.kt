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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.navigation.NavigationController
import com.nlab.reminder.databinding.FragmentHomeBinding
import com.nlab.reminder.domain.common.android.fragment.resultReceives
import com.nlab.reminder.domain.common.android.navigation.navigateToAllScheduleEnd
import com.nlab.reminder.domain.common.android.view.recyclerview.SimpleLayoutAdapter
import com.nlab.reminder.domain.feature.home.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = checkNotNull(_binding)

    @HomeScope
    @Inject
    lateinit var navigationController: NavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultReceives<HomeTagConfigResult>(REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG)
            .onEach { result ->
                when {
                    result.isRenameRequested -> viewModel.onTagRenameRequestClicked(result.tag)
                    result.isDeleteRequested -> viewModel.onTagDeleteRequestClicked(result.tag)
                }
            }
            .launchIn(lifecycleScope)

        resultReceives<HomeTagRenameResult>(REQUEST_KEY_HOME_TO_HOME_TAG_RENAME)
            .filter { it.isConfirmed }
            .onEach { result -> viewModel.onTagRenameConfirmClicked(result.tag, result.rename) }
            .launchIn(lifecycleScope)

        resultReceives<HomeTagDeleteResult>(REQUEST_KEY_HOME_TO_HOME_TAG_DELETE)
            .filter { it.isConfirmed }
            .map { it.tag }
            .onEach { tag -> viewModel.onTagDeleteConfirmClicked(tag) }
            .launchIn(lifecycleScope)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentHomeBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val logoAdapter = SimpleLayoutAdapter(R.layout.view_item_home_logo)
        val categoryAdapter = HomeCategoryAdapter(
            onTodayNavClicked = viewModel::onTodayCategoryClicked,
            onTimetableNavClicked = viewModel::onTimetableCategoryClicked,
            onAllNavClicked = viewModel::onAllCategoryClicked
        )
        val tagCardAdapter = HomeTagCardAdapter(
            onTagClicked = viewModel::onTagClicked,
            onTagLongClicked = viewModel::onTagLongClicked
        )

        val renderWhenLoaded = renderWhenLoadedFunc(categoryAdapter, tagCardAdapter)

        binding.recyclerviewContent
            .apply { itemAnimator = null }
            .apply { adapter = ConcatAdapter(logoAdapter, categoryAdapter, tagCardAdapter) }

        viewModel.homeSideEffectFlow
            .flowWithLifecycle(viewLifecycle)
            .onEach(this::handleSideEffect)
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<HomeState.Init>()
            .flowWithLifecycle(viewLifecycle)
            .onEach { renderWhenInit() }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<HomeState.Loading>()
            .flowWithLifecycle(viewLifecycle)
            .onEach { renderWhenLoading() }
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .filterIsInstance<HomeState.Loaded>()
            .flowWithLifecycle(viewLifecycle)
            .onEach { state -> renderWhenLoaded(state.snapshot) }
            .launchIn(viewLifecycleScope)
    }

    private fun handleSideEffect(sideEffect: HomeSideEffect) = when (sideEffect) {
        is HomeSideEffect.NavigateToday -> {

        }
        is HomeSideEffect.NavigateTimetable -> {

        }
        is HomeSideEffect.NavigateAllSchedule -> {
            navigationController.navigateToAllScheduleEnd()
        }
        is HomeSideEffect.NavigateTag -> {

        }
        is HomeSideEffect.NavigateTagConfig -> {
            navigationController.navigateToTagConfig(
                REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG, sideEffect.tag
            )
        }
        is HomeSideEffect.NavigateTagRename -> {
            navigationController.navigateToTagRename(
                REQUEST_KEY_HOME_TO_HOME_TAG_RENAME, sideEffect.tag, sideEffect.usageCount
            )
        }
        is HomeSideEffect.NavigateTagDelete -> {
            navigationController.navigateToTagDelete(
                REQUEST_KEY_HOME_TO_HOME_TAG_DELETE, sideEffect.tag, sideEffect.usageCount
            )
        }
    }

    private fun renderWhenInit() {
        binding.recyclerviewContent.visibility = View.GONE
    }

    private fun renderWhenLoading() {
        binding.recyclerviewContent.visibility = View.GONE
    }

    private fun renderWhenLoadedFunc(
        categoryAdapter: HomeCategoryAdapter,
        tagCardAdapter: HomeTagCardAdapter,
    ) = { snapshot: HomeSnapshot ->
        binding.recyclerviewContent.visibility = View.VISIBLE
        categoryAdapter.submitList(listOf(snapshot.notification))
        tagCardAdapter.submitList(listOf(snapshot.tags))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG = "requestKeyHomeToHomeTagConfig"
        private const val REQUEST_KEY_HOME_TO_HOME_TAG_RENAME = "requestKeyHomeToHomeTagRename"
        private const val REQUEST_KEY_HOME_TO_HOME_TAG_DELETE = "requestKeyHomeToHomeTagDelete"
    }
}