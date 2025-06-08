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

package com.nlab.reminder.core.text.ui

import android.content.Context
import android.content.res.Resources
import com.nlab.reminder.core.text.UiText
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * @author Thalys
 */
fun UiText.toText(context: Context): String = toText(resources = context.resources)

fun UiText.toText(resources: Resources): String = convertToText(
    initialUiText = this,
    getString = { resId, args ->
        if (args == null) resources.getString(resId)
        else resources.getString(resId, *args)
    },
    getQuantityString = { resId, count, args ->
        if (args == null) resources.getQuantityString(resId, count)
        else resources.getQuantityString(resId, count, *args)
    }
)

internal class UiTextDisplayNode(
    val current: UiText,
    val parent: UiTextDisplayNode?,
    val resolvedArgs: Array<Any>,
) {
    private var currentIndex: Int = 0

    fun isArgResolveNeeded(): Boolean {
        return currentIndex < resolvedArgs.size
    }

    fun resolveArgWith(args: String) {
        resolvedArgs[currentIndex++] = args
    }

    fun resolveArg() {
        ++currentIndex
    }

    fun currentArg(): Any {
        return resolvedArgs[currentIndex]
    }
}

internal inline fun convertToText(
    initialUiText: UiText,
    getString: (Int, Array<Any>?) -> String,
    getQuantityString: (Int, Int, Array<Any>?) -> String
): String {
    val nodeOrValue = createUiTextDisplayNodeOrValue(
        uiText = initialUiText,
        head = null,
        getString = getString,
        getQuantityString = getQuantityString
    )
    return when (nodeOrValue) {
        is String -> nodeOrValue
        else -> resolveUiTextNode(
            node = (nodeOrValue as UiTextDisplayNode).apply {
                resolveArgs(
                    initialNode = this,
                    getString,
                    getQuantityString
                )
            },
            getString,
            getQuantityString
        )
    }
}

private inline fun createUiTextDisplayNodeOrValue(
    uiText: UiText,
    head: UiTextDisplayNode?,
    getString: (Int, Array<Any>?) -> String,
    getQuantityString: (Int, Int, Array<Any>?) -> String
): Any = when (uiText) {
    is UiText.Direct -> {
        uiText.value
    }

    is UiText.ResId -> {
        if (uiText.args.containsUiText()) {
            UiTextDisplayNode(
                current = uiText,
                parent = head,
                resolvedArgs = uiText.args.copyOf(),
            )
        } else {
            getString(uiText.resId, uiText.args)
        }
    }

    is UiText.PluralsResId -> {
        if (uiText.args.containsUiText()) {
            UiTextDisplayNode(
                current = uiText,
                parent = head,
                resolvedArgs = uiText.args.copyOf()
            )
        } else {
            getQuantityString(uiText.resId, uiText.count, uiText.args)
        }
    }
}

private inline fun resolveArgs(
    initialNode: UiTextDisplayNode,
    getString: (Int, Array<Any>?) -> String,
    getQuantityString: (Int, Int, Array<Any>?) -> String
) {
    var currentNode = initialNode
    do {
        if (currentNode.isArgResolveNeeded()) {
            val arg = currentNode.currentArg()
            if (arg is UiText) {
                val childNodeOrValue = createUiTextDisplayNodeOrValue(
                    uiText = arg,
                    head = currentNode,
                    getString = getString,
                    getQuantityString = getQuantityString
                )
                when (childNodeOrValue) {
                    is String -> {
                        currentNode.resolveArgWith(childNodeOrValue)
                    }
                    is UiTextDisplayNode -> {
                        currentNode = childNodeOrValue
                    }
                }
            } else {
                currentNode.resolveArg()
            }
        } else {
            val parentNode = currentNode.parent
            if (parentNode != null) {
                parentNode.resolveArgWith(
                    resolveUiTextNode(
                        node = currentNode,
                        getString = getString,
                        getQuantityString = getQuantityString
                    )
                )
                currentNode = parentNode
            }
        }
        // The loop continues as long as the current node has a parent or requires argument resolution.
    } while (currentNode.parent != null || currentNode.isArgResolveNeeded())
}

private inline fun resolveUiTextNode(
    node: UiTextDisplayNode,
    getString: (Int, Array<Any>?) -> String,
    getQuantityString: (Int, Int, Array<Any>?) -> String
): String = when (val currentUiText = node.current) {
    is UiText.ResId -> {
        getString(currentUiText.resId, node.resolvedArgs)
    }
    is UiText.PluralsResId -> {
        getQuantityString(currentUiText.resId, currentUiText.count, node.resolvedArgs)
    }
    is UiText.Direct -> {
        error("UiText.Direct should be resolved immediately and never passed into node stack")
    }
}

@OptIn(ExperimentalContracts::class)
private fun Array<Any>?.containsUiText(): Boolean {
    contract {
        returns() implies (this@containsUiText != null)
    }
    return this?.any { it is UiText } == true
}