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

package com.nlab.reminder.core.android.content

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes

/**
 * @author thalys
 */
inline fun Context.getThemeResources(attrs: IntArray, transform: (TypedArray, position: Int) -> Any): Array<Any> {
    val typedArray = theme.obtainStyledAttributes(attrs)
    val result = Array(attrs.size) { position -> transform(typedArray, position) }

    typedArray.recycle()
    return result
}

@ColorInt
fun Context.getThemeColor(@AttrRes attrRes: Int, @ColorInt defaultColor: Int = Color.TRANSPARENT): Int {
    return getThemeResources(
        intArrayOf(attrRes),
        transform = { typedArray, position -> typedArray.getColor(position, defaultColor) }
    )[0] as Int
}

fun Context.getDimension(@DimenRes id: Int): Float {
    return resources.getDimension(id)
}