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

package com.nlab.reminder.core.android.widget

import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.load
import coil.request.Disposable
import com.nlab.reminder.core.android.R

/**
 * If [url] is different from the existing source, change it and return whether or not it has changed.
 *
 * If [url] is null, it is replaced with placeHolder.
 *
 *  @author Thalys
 *  @param url value to change
 *  @param placeHolder image to use while loading
 *  @param error image to use if loading fails
 *  @return whether to change
 */
fun ImageView.bindImageAsync(
    url: String?,
    placeHolder: Drawable? = null,
    error: Drawable? = null
): Boolean {
    if (url == null) {
        val isChanged = asyncImageSource?.disposable
            ?.dispose()
            ?.let { true }
            ?: false
        if (isChanged) {
            asyncImageSource = null
        }
        setImageDrawable(placeHolder)
        return isChanged
    } else {
        if (asyncImageSource?.source == url) return false
        asyncImageSource = AsyncImageSource(
            source = url,
            disposable = load(url) {
                placeholder(placeHolder)
                error(error)
            }
        )
        return true
    }
}

private var ImageView.asyncImageSource: AsyncImageSource?
    get() = getTag(R.id.imageview_async_image_source) as? AsyncImageSource
    set(value) {
        setTag(R.id.imageview_async_image_source, value)
    }

private data class AsyncImageSource(
    val source: Any,
    val disposable: Disposable
)