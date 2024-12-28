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
import androidx.fragment.app.viewModels
import com.nlab.reminder.core.androidx.fragment.compose.ComposableFragment
import com.nlab.reminder.core.androidx.fragment.compose.ComposableInject
import com.nlab.reminder.core.translation.StringIds
import com.nlab.reminder.feature.all.AllViewModel
import com.nlab.reminder.feature.all.databinding.FragmentAllBinding

/**
 * @author Doohyun
 */
class AllFragment : ComposableFragment() {
    private var _binding: FragmentAllBinding? = null
    private val binding: FragmentAllBinding get() = checkNotNull(_binding)

    private val viewModel: AllViewModel by viewModels()

    @ComposableInject
    lateinit var onBackClicked: () -> Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FragmentAllBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onComposed() {
        binding.toolbar.titleText = requireContext().getString(StringIds.home_category_all)
    }

    override fun onResume() {
        super.onResume()
        println("Hello onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("Hello onDestroyView")
        binding.toolbar.titleText = ""
        _binding = null
    }
}