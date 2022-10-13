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

package com.nlab.reminder.domain.feature.home.tag.rename.view

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.fragment.navArgs
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycle
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.view.clicks
import com.nlab.reminder.core.android.view.textChanged
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.FragmentHomeTagRenameDialogBinding
import com.nlab.reminder.domain.common.android.fragment.popBackStackWithResult
import com.nlab.reminder.domain.feature.home.tag.rename.*
import com.nlab.reminder.domain.feature.home.view.HomeTagRenameResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
@AndroidEntryPoint
class HomeTagRenameDialogFragment : DialogFragment() {
    private val args: HomeTagRenameDialogFragmentArgs by navArgs()
    private val viewModel: HomeTagRenameViewModel by viewModels()

    private var _binding: FragmentHomeTagRenameDialogBinding? = null
    private val binding: FragmentHomeTagRenameDialogBinding get() = checkNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentHomeTagRenameDialogBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.usageCountTextview.apply {
            text = resources.getQuantityString(
                R.plurals.home_tag_rename_text_label,
                args.tagUsageCount.toInt(),
                args.tag.name,
                args.tagUsageCount
            )
        }

        binding.renameEdittext
            .textChanged()
            .map { it.text?.toString() ?: "" }
            .distinctUntilChanged()
            .onEach { viewModel.onRenameTextInput(it) }
            .launchIn(viewLifecycleScope)

        binding.clearButton
            .throttleClicks()
            .onEach { viewModel.onRenameTextClearClicked() }
            .launchIn(viewLifecycleScope)

        binding.cancelButton
            .throttleClicks()
            .onEach { viewModel.onCancelClicked() }
            .launchIn(viewLifecycleScope)

        binding.okButton
            .throttleClicks()
            .onEach { viewModel.onConfirmClicked() }
            .launchIn(viewLifecycleScope)

        viewModel.homeTagRenameSideEffectFlow
            .flowWithLifecycle(viewLifecycle)
            .onEach(this::handleSideEffect)
            .launchIn(viewLifecycleScope)

        viewModel.stateFlow
            .flowWithLifecycle(viewLifecycle)
            .onEach(this::render)
            .launchIn(viewLifecycleScope)
    }

    private fun handleSideEffect(sideEffect: HomeTagRenameSideEffect) {
        popBackStackWithResult(
            args.requestKey, result = when (sideEffect) {
                is HomeTagRenameSideEffect.Cancel -> HomeTagRenameResult(
                    args.tag,
                    rename = "",
                    isConfirmed = false
                )

                is HomeTagRenameSideEffect.Complete -> HomeTagRenameResult(
                    args.tag,
                    rename = sideEffect.rename,
                    isConfirmed = true
                )
            }
        )
    }

    private fun render(homeTagRenameState: HomeTagRenameState) {
        binding.renameEdittext
            .apply {
                if (TextUtils.equals(text, homeTagRenameState.currentText).not()) {
                    setText(homeTagRenameState.currentText)
                    setSelection(text.length)
                }
            }
            .also { editText ->
                if (homeTagRenameState.isKeyboardShowWhenViewCreated.not()) return@also

                viewLifecycleScope.launchWhenResumed {
                    delay(100) // keyboard not showing without delay...
                    viewModel.onKeyboardShownWhenViewCreated()
                    if (editText.requestFocus()) {
                        requireContext().getSystemService<InputMethodManager>()?.showSoftInput(
                            editText,
                            InputMethodManager.SHOW_IMPLICIT
                        )
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}