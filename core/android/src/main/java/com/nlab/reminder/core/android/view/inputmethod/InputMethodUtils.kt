/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.android.view.inputmethod

import android.content.Context
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService

/**
 * @author Doohyun
 */
fun View.hideSoftInputFromWindow(flags: Int = 0) {
    context.hideSoftInputFromWindow(windowToken, flags = flags)
}

fun Context.hideSoftInputFromWindow(windowToken: IBinder?, flags: Int = 0) {
    getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, flags)
}