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

import android.os.Parcelable
import androidx.navigation.NavController
import com.nlab.reminder.domain.common.tag.Tag
import kotlinx.parcelize.Parcelize

/**
 * @author Doohyun
 */
@Parcelize
data class HomeTagConfigResult(
    val tag: Tag,
    val isRenameRequested: Boolean,
    val isDeleteRequested: Boolean
) : Parcelable

@Parcelize
data class HomeTagRenameResult(
    val tag: Tag,
    val rename: String,
    val isConfirmed: Boolean
) : Parcelable

@Parcelize
data class HomeTagDeleteResult(
    val tag: Tag,
    val isConfirmed: Boolean
) : Parcelable

internal fun NavController.navigateToTagConfig(requestKey: String, tag: Tag) {
    HomeFragmentDirections
        .actionHomeFragmentToHomeConfigDialogFragment(requestKey, tag)
        .run(this::navigate)
}

internal fun NavController.navigateToTagDelete(requestKey: String, tag: Tag, usageCount: Long) {
    HomeFragmentDirections
        .actionHomeFragmentToHomeTagRenameDialogFragment(
            requestKey,
            tag,
            usageCount
        )
        .run(this::navigate)
}

internal fun NavController.navigateToTagRename(requestKey: String, tag: Tag, usageCount: Long) {
    HomeFragmentDirections
        .actionHomeFragmentToHomeTagDeleteDialogFragment(
            requestKey,
            tag,
            usageCount
        )
        .run(this::navigate)
}
