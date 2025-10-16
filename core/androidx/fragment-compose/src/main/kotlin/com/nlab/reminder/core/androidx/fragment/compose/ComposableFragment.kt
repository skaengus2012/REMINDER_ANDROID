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

package com.nlab.reminder.core.androidx.fragment.compose

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nlab.reminder.core.androidx.fragment.viewLifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
abstract class ComposableFragment : Fragment() {
    private val isComposeCompleteJob = CompletableDeferred<Unit>()

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleScope.launch {
            isComposeCompleteJob.await()
            onViewReady(view, savedInstanceState)
        }
    }

    internal fun completeComposition() {
        isComposeCompleteJob.complete(Unit)
    }

    protected open fun onViewReady(view: View, savedInstanceState: Bundle?) = Unit
}