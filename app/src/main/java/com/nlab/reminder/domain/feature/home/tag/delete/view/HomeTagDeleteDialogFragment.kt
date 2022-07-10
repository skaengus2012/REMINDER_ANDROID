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

package com.nlab.reminder.domain.feature.home.tag.delete.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nlab.reminder.R
import com.nlab.reminder.core.android.view.clicks
import com.nlab.reminder.databinding.FragmentHomeTagDeleteDialogBinding
import com.nlab.reminder.domain.common.android.view.fragment.sendResultAndDismiss
import com.nlab.reminder.domain.common.tag.Tag
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author Doohyun
 */
class HomeTagDeleteDialogFragment : BottomSheetDialogFragment() {
    private val args: HomeTagDeleteDialogFragmentArgs by navArgs()

    private val binding: FragmentHomeTagDeleteDialogBinding get() = checkNotNull(_binding)
    private var _binding: FragmentHomeTagDeleteDialogBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentHomeTagDeleteDialogBinding.inflate(inflater, container, false)
            .also { _binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.descriptionTextview.apply {
            text = args.tag.let { tag ->
                resources.getQuantityString(
                    R.plurals.home_tag_delete_text_label, tag.usageCount, tag.text, tag.usageCount
                )
            }
        }

        binding.cancelButton.clicks()
            .onEach {
                sendResultAndDismiss(
                    requestKey = args.requestKey,
                    result = bundleOf(
                        RESULT_TYPE to RESULT_TYPE_DISMISS_REQUEST,
                        RESULT_TAG to args.tag
                    )
                )
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.confirmButton.clicks()
            .onEach {
                sendResultAndDismiss(
                    requestKey = args.requestKey,
                    result = bundleOf(
                        RESULT_TYPE to RESULT_TYPE_CONFIRM_REQUEST,
                        RESULT_TAG to args.tag
                    )
                )
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val RESULT_TAG = "homeTagDeleteDialogResultTag"
        private const val RESULT_TYPE = "homeTagDeleteDialogResultType"
        private const val RESULT_TYPE_DISMISS_REQUEST = "dismissRequest"
        private const val RESULT_TYPE_CONFIRM_REQUEST = "confirmRequest"

        fun resultListenerOf(
            onDeleteClicked: (Tag) -> Unit,
            onCancelClicked: (Tag) -> Unit = {}
        ) = { _: String, bundle: Bundle ->
            val resultTag: Tag = requireNotNull(bundle.getParcelable(RESULT_TAG))
            when (requireNotNull(bundle.getString(RESULT_TYPE))) {
                RESULT_TYPE_DISMISS_REQUEST -> onCancelClicked(resultTag)
                RESULT_TYPE_CONFIRM_REQUEST -> onDeleteClicked(resultTag)
            }
        }
    }
}