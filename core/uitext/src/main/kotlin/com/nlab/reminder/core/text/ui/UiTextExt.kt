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
import androidx.annotation.IntRange
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
    @IntRange(from = 0) var processedIndex: Int,
) {
    fun isProcessing(): Boolean {
        return processedIndex < resolvedArgs.size
    }

    fun resolveArgWith(args: String) {
        resolvedArgs[processedIndex++] = args
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
    if (nodeOrValue is String) return nodeOrValue

    var result = ""
    var currentNode = nodeOrValue as UiTextDisplayNode
    while (currentNode.isProcessing()) {
        val arg = currentNode.resolvedArgs[currentNode.processedIndex]
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
            ++currentNode.processedIndex
        }

        if (currentNode.isProcessing().not()) {
            val value = when (val currentUiState = currentNode.current) {
                is UiText.ResId -> {
                    getString(currentUiState.resId, currentNode.resolvedArgs)
                }
                is UiText.PluralsResId -> {
                    getQuantityString(currentUiState.resId, currentUiState.count, currentNode.resolvedArgs)
                }
                is UiText.Direct -> {
                    error("UiText.Direct should be resolved immediately and never passed into node stack")
                }
            }
            val headNode = currentNode.parent
            if (headNode == null) {
                result = value
            } else {
                headNode.resolveArgWith(value)
                currentNode = headNode
            }
        }
    }
    return result
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
                processedIndex = 0
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
                resolvedArgs = uiText.args.copyOf(),
                processedIndex = 0
            )
        } else {
            getQuantityString(uiText.resId, uiText.count, uiText.args)
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun Array<Any>?.containsUiText(): Boolean {
    contract {
        returns() implies (this@containsUiText != null)
    }
    return this?.any { it is UiText } == true
}