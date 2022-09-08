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

package com.nlab.reminder.domain.feature.home.tag.config.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.FragmentHomeTagConfigDialogBinding
import com.nlab.reminder.domain.common.android.fragment.sendResultAndDismiss
import com.nlab.reminder.domain.feature.home.view.HomeTagConfigResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class HomeTagConfigDialogFragment : DialogFragment() {
    private val args: HomeTagConfigDialogFragmentArgs by navArgs()

    private var _binding: FragmentHomeTagConfigDialogBinding? = null
    private val binding: FragmentHomeTagConfigDialogBinding get() = checkNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentHomeTagConfigDialogBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tagTextview.apply {
            text = context.getString(R.string.tag_format, args.tag.name)
        }

        binding.renameButton
            .throttleClicks()
            .map { HomeTagConfigResult(args.tag, isRenameRequested = true, isDeleteRequested = false) }
            .onEach { result -> sendResultAndDismiss(args.requestKey, result) }
            .launchIn(viewLifecycleScope)

        binding.deleteButton
            .throttleClicks()
            .map { HomeTagConfigResult(args.tag, isRenameRequested = false, isDeleteRequested = true) }
            .onEach { result -> sendResultAndDismiss(args.requestKey, result) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}