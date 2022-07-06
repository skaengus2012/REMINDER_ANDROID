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
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.nlab.reminder.core.android.view.clicks
import com.nlab.reminder.databinding.FragmentHomeTagRenameDialogBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class HomeTagRenameDialogFragment : DialogFragment() {
    private val navArgs: HomeTagRenameDialogFragmentArgs by navArgs()

    private var _binding: FragmentHomeTagRenameDialogBinding? = null
    private val binding: FragmentHomeTagRenameDialogBinding get() = checkNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentHomeTagRenameDialogBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.renameEdittext.setText(navArgs.tag.text)

        binding.clearButton.clicks()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { binding.renameEdittext.setText("") }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.cancelButton.clicks()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {

            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.okButton.clicks()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {

            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}