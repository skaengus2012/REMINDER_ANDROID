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
import androidx.annotation.StringDef
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.nlab.reminder.R
import com.nlab.reminder.core.android.view.throttleClicks
import com.nlab.reminder.databinding.FragmentHomeTagConfigDialogBinding
import com.nlab.reminder.domain.common.tag.Tag
import kotlinx.coroutines.flow.launchIn
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
        with(binding.tagTextview) {
            text = context.getString(R.string.tag_format, args.tag.text)
        }

        binding.renameButton.throttleClicks()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { sendResultAndDismiss(args.requestKey, args.tag, RESULT_TYPE_RENAME_REQUEST) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.deleteButton.throttleClicks()
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { sendResultAndDismiss(args.requestKey, args.tag, RESULT_TYPE_DELETE_REQUEST) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @StringDef(value = [RESULT_TYPE_RENAME_REQUEST, RESULT_TYPE_DELETE_REQUEST])
    private annotation class ResultType

    companion object {
        private const val RESULT_TAG = "homeConfigDialogResultTag"
        private const val RESULT_TYPE = "homeConfigDialogResultType"
        private const val RESULT_TYPE_RENAME_REQUEST = "renameRequest"
        private const val RESULT_TYPE_DELETE_REQUEST = "deleteRequest"

        private fun DialogFragment.sendResultAndDismiss(requestKey: String, tag: Tag, @ResultType resultType: String) {
            setFragmentResult(
                requestKey,
                result = bundleOf(
                    RESULT_TAG to tag,
                    RESULT_TYPE to resultType
                )
            )
            dismiss()
        }

        fun resultListenerOf(
            onRenameClicked: (Tag) -> Unit,
            onDeleteClicked: (Tag) -> Unit
        ) = { _: String, bundle: Bundle ->
            val resultTag: Tag = requireNotNull(bundle.getParcelable(RESULT_TAG))
            when (requireNotNull(bundle.getString(RESULT_TYPE))) {
                RESULT_TYPE_RENAME_REQUEST -> onRenameClicked(resultTag)
                RESULT_TYPE_DELETE_REQUEST -> onDeleteClicked(resultTag)
            }
        }
    }
}