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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.nlab.reminder.R
import com.nlab.reminder.core.android.fragment.viewLifecycleScope
import com.nlab.reminder.core.android.view.clicks
import com.nlab.reminder.core.android.view.textChanged
import com.nlab.reminder.databinding.FragmentHomeTagRenameDialogBinding
import com.nlab.reminder.domain.common.android.fragment.sendResultAndDismiss
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.feature.home.tag.rename.*
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

        binding.renameEdittext.textChanged()
            .map { it.text?.toString() ?: "" }
            .distinctUntilChanged()
            .onEach { viewModel.onRenameTextInput(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.clearButton.clicks()
            .onEach { viewModel.onRenameTextClearClicked() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.cancelButton.clicks()
            .onEach { viewModel.onCancelClicked() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.okButton.clicks()
            .onEach { viewModel.onConfirmClicked() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.homeTagRenameSideEffect.flow
            .onEach { receiveSideEffect(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach { render(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun render(homeTagRenameState: HomeTagRenameState) {
        binding.renameEdittext
            .apply {
                setText(homeTagRenameState.currentText)
                setSelection(text.length)
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

    private fun receiveSideEffect(message: HomeTagRenameSideEffectMessage) {
        sendResultAndDismiss(args.requestKey,
            result = when (message) {
                is HomeTagRenameSideEffectMessage.Cancel -> bundleOf(
                    RESULT_TYPE to RESULT_TYPE_CANCEL_REQUEST,
                    RESULT_TAG to args.tag
                )

                is HomeTagRenameSideEffectMessage.Complete -> bundleOf(
                    RESULT_TYPE to RESULT_TYPE_CONFIRM_REQUEST,
                    RESULT_TAG to args.tag,
                    RESULT_RENAME to message.rename
                )
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RESULT_TAG = "homeTagRenameDialogResultTag"
        private const val RESULT_RENAME = "homeTagRenameDialogResultRename"
        private const val RESULT_TYPE = "homeTagRenameDialogResultType"
        private const val RESULT_TYPE_CANCEL_REQUEST = "cancelRequest"
        private const val RESULT_TYPE_CONFIRM_REQUEST = "confirmRequest"

        fun tagOf(bundle: Bundle): Tag = requireNotNull(bundle.getParcelable(RESULT_TAG))
        fun renameOf(bundle: Bundle): String = bundle.getString(RESULT_RENAME) ?: ""
        fun isConfirmedOf(bundle: Bundle): Boolean =
            requireNotNull(bundle.getString(RESULT_TYPE)) == RESULT_TYPE_CONFIRM_REQUEST
        fun isCancelledOf(bundle: Bundle): Boolean =
            requireNotNull(bundle.getString(RESULT_TYPE)) == RESULT_TYPE_CANCEL_REQUEST
    }
}