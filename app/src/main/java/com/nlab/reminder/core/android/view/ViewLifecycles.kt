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

package com.nlab.reminder.core.android.view

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.viewbinding.ViewBinding

/**
 * @author Doohyun
 */
fun View.initWithLifecycleOwner(
    action: (lifecycleOwner: LifecycleOwner) -> Unit
) = apply {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            action(checkNotNull(view.findViewTreeLifecycleOwner()))
        }

        override fun onViewDetachedFromWindow(view: View) = Unit
    })
}

inline fun <T : ViewBinding> T.initWithLifecycleOwner(
    crossinline action: T.(lifecycleOwner: LifecycleOwner) -> Unit
) = apply { root.initWithLifecycleOwner { lifecycleOwner -> action(lifecycleOwner) } }