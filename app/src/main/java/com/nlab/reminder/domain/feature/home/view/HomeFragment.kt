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
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.navigation.NavigationController
import com.nlab.reminder.databinding.FragmentHomeBinding
import com.nlab.reminder.domain.common.android.fragment.handleSideEffect
import com.nlab.reminder.domain.common.android.fragment.resultReceives
import com.nlab.reminder.domain.common.android.navigation.navigateToAllScheduleEnd
import com.nlab.reminder.domain.feature.home.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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
        val homeItemAdapter = HomeItemAdapter()
        val renderWhenLoaded = renderWhenLoadedFunc(homeItemAdapter)

        binding.categoryRecyclerview
            .apply { itemAnimator = null }
            .apply { adapter = homeItemAdapter }

        handleSideEffect(viewModel.homeSideEffect) { sideEffect ->
            when (sideEffect) {
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
        }

        viewModel.state
            .filterIsInstance<HomeState.Init>()
            .flowWithLifecycle(viewLifecycle)
            .distinctUntilChanged()
            .onEach { renderWhenInit() }
            .launchIn(viewLifecycleScope)

        viewModel.state
            .filterIsInstance<HomeState.Loading>()
            .flowWithLifecycle(viewLifecycle)
            .distinctUntilChanged()
            .onEach { renderWhenLoading() }
            .launchIn(viewLifecycleScope)

        viewModel.state
            .filterIsInstance<HomeState.Loaded>()
            .flowWithLifecycle(viewLifecycle)
            .distinctUntilChanged()
            .map { state ->
                state.toHomeItems(
                    onTodayCategoryClicked = viewModel::onTodayCategoryClicked,
                    onTimetableCategoryClicked = viewModel::onTimetableCategoryClicked,
                    onAllCategoryClicked = viewModel::onAllCategoryClicked,
                    onTagClicked = viewModel::onTagClicked,
                    onTagLongClicked = viewModel::onTagLongClicked
                )
            }
            .flowOn(Dispatchers.Default)
            .onEach { homeListItem -> renderWhenLoaded(homeListItem) }
            .launchIn(viewLifecycleScope)
    }

    private fun renderWhenInit() {
        binding.categoryRecyclerview.visibility = View.GONE
    }

    private fun renderWhenLoading() {
        binding.categoryRecyclerview.visibility = View.GONE
    }

    private fun renderWhenLoadedFunc(homeItemAdapter: HomeItemAdapter) = { homeItems: List<HomeItem> ->
        binding.categoryRecyclerview.visibility = View.VISIBLE
        homeItemAdapter.submitList(homeItems)
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