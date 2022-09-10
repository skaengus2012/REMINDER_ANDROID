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

package com.nlab.reminder.domain.common.android.fragment

import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.nlab.reminder.core.android.fragment.resultReceives
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * @author Doohyun
 */
internal const val RESULT_VALUE = "fragmentResult"

fun Fragment.popBackStackWithResult(requestKey: String, result: Parcelable) {
    setFragmentResult(requestKey, bundleOf(RESULT_VALUE to result))
    findNavController().popBackStack()
}

fun <T : Parcelable> Fragment.resultReceives(requestKey: String): Flow<T> =
    resultReceives(requestKey).mapNotNull { bundle -> bundle.getParcelable(RESULT_VALUE) }