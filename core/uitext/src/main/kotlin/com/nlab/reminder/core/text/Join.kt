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

package com.nlab.reminder.core.text

import androidx.annotation.StringRes

/**
 * Converts this list into a nested [UiText] structure using the given string resource as a separator.
 *
 * This function is designed for building localized UI strings like "A, B, C", where each element can be
 * either a plain string or another [UiText], and the final result maintains full localization support
 * through resource-based composition.
 *
 * The joining happens in **reverse order**, with the last element forming the base of the structure and
 * each previous element wrapped around it using the provided [separatorRes] string resource.
 *
 * For example, given the list `["A", "B", "C"]` and a separator resource:
 *
 * ```xml
 * <string name="comma_separator">%1$s, %2$s</string>
 * ```
 *
 * The result will be:
 *
 * ```
 * UiText.Res(
 *   resId = R.string.comma_separator,
 *   args = arrayOf(
 *      UiText.Direct("A"),
 *      UiText.Res(
 *         resId = R.string.comma_separator,
 *         UiText.Direct("B"),
 *         UiText.Direct"C")
 *      )
 *   )
 * )
 * ```
 *
 * @param separatorRes String resource ID (e.g. R.string.comma_separator) used to join two elements.
 *                     The resource should have two placeholders like "%1\$s, %2\$s".
 * @param transform Optional function to convert each item of type [T] into a value consumable by
 *                  [transformToUiText]. If null, elements are passed directly.
 *
 * @return A nested [UiText] structure representing the joined list, or [EmptyUiText] if the list is empty.
 * @author Doohyun
 */
fun <T> List<T>.joinToUiText(@StringRes separatorRes: Int, transform: ((T) -> Any?)? = null): UiText {
    var result: UiText? = null
    for (i in size - 1 downTo 0) {
        val arg = transformToUiText(element = get(i), transform = transform)
        if (result == null) {
            result = arg
        } else {
            result = UiText(
                resId = separatorRes,
                firstArg = arg,
                secondArg = result
            )
        }
    }
    return result ?: EmptyUiText()
}

private fun <T> transformToUiText(
    element: T,
    transform: ((T) -> Any?)?
): UiText {
    val arg = if (transform == null) element else transform(element)
    return if (arg is UiText) arg else UiText(value = arg.toString())
}