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
import androidx.annotation.StringRes

/**
 * @author thalys
 */
class ToastHandle(private val context: Context) {
    private var curToast: Toast? = null

    fun showToast(@StringRes stringResource: Int) {
        showToast { Toast.makeText(context, stringResource, Toast.LENGTH_SHORT) }
    }

    private inline fun showToast(crossinline getToast: () -> Toast) {
        Handler(Looper.getMainLooper()).post {
            curToast?.cancel()
            curToast = getToast().also { it.show() }
        }
    }
}