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
import com.nlab.reminder.core.entrypoint.fragment.FragmentEntryPointInit
import com.nlab.reminder.databinding.FragmentHomeBinding
import com.nlab.reminder.domain.common.android.view.recyclerview.simple.SimpleLayoutAdapter
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
    lateinit var entryPointInit: FragmentEntryPointInit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentHomeBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryPointInit.initialize(
            navigationEffect = viewModel.navigationEffect
        )

        val categoryAdapter = CategoryAdapter(viewLifecycleOwner)
        val tagAdapter = TagAdapter(viewLifecycleOwner)
        val renderWhenLoaded = renderWhenLoadedFunc(categoryAdapter, tagAdapter)

        with(binding.categoryRecyclerview) {
            itemAnimator = null
            adapter = ConcatAdapter(
                categoryAdapter,
                SimpleLayoutAdapter(layoutResource = R.layout.view_item_home_space_between_category_and_tags),
                tagAdapter
            )
        }

        viewModel.state
            .filterIsInstance<HomeState.Init>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach { renderWhenInit() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state
            .filterIsInstance<HomeState.Loading>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach { renderWhenLoading() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state
            .filterIsInstance<HomeState.Loaded>()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .map { state ->
                state.toListItem(
                    onTodayCategoryClicked = viewModel::onTodayCategoryClicked,
                    onTimetableCategoryClicked = viewModel::onTimetableCategoryClicked,
                    onAllCategoryClicked = viewModel::onAllCategoryClicked,
                    onTagClicked = viewModel::onTagClicked,
                    onTagLongClicked = viewModel::onTagLongClicked
                )
            }
            .flowOn(Dispatchers.Default)
            .onEach { homeListItem -> renderWhenLoaded(homeListItem) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun renderWhenInit() {
        binding.categoryRecyclerview.visibility = View.GONE
    }

    private fun renderWhenLoading() {
        binding.categoryRecyclerview.visibility = View.GONE
    }

    private fun renderWhenLoadedFunc(
        categoryAdapter: CategoryAdapter,
        tagAdapter: TagAdapter
    ): (HomeListItem) -> Unit = { (categoryItems, tagItems) ->
        binding.categoryRecyclerview.visibility = View.VISIBLE

        categoryAdapter.submitList(categoryItems)
        tagAdapter.submitList(listOf(tagItems))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}