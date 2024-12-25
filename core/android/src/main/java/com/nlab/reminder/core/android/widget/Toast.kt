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

package com.nlab.reminder.core.android.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast as ToastOrigin
import androidx.annotation.StringRes

/**
 * @author thalys
 */
class Toast(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var curToast: ToastOrigin? = null

    fun showToast(@StringRes resId: Int) {
        showToast { ToastOrigin.makeText(context, resId, ToastOrigin.LENGTH_SHORT) }
    }

    fun showToast(text: String) {
        showToast { ToastOrigin.makeText(context, text, ToastOrigin.LENGTH_SHORT) }
    }

    private inline fun showToast(crossinline getToast: () -> Toast) {
        handler.post {
            curToast?.cancel()
            curToast = getToast().also { it.show() }
        }
    }
}