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
package com.nlab.reminder.core.component.usermessage

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * @author Doohyun
 */
sealed class UserMessage private constructor() {
    data class Default(val value: String) : UserMessage()

    data class ResId(@StringRes val resId: Int, val args: Array<Any>?) : UserMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResId

            if (resId != other.resId) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }

    data class PluralsResId(@PluralsRes val resId: Int, val count: Int, val args: Array<Any>?) : UserMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PluralsResId

            if (resId != other.resId) return false
            if (count != other.count) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + count
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
}

fun UserMessage(value: String): UserMessage = UserMessage.Default(value)

fun UserMessage(@StringRes resId: Int): UserMessage = UserMessage.ResId(resId, args = null)

fun UserMessage(
    @StringRes resId: Int,
    arg: Any,
): UserMessage = UserMessage.ResId(resId, arrayOf(arg))

fun UserMessage(
    @StringRes resId: Int,
    firstArg: Any,
    secondArg: Any,
    vararg etcArgs: Any,
): UserMessage = UserMessage.ResId(resId, mergeArgs(firstArg, secondArg, etcArgs))

fun PluralsUserMessage(
    @PluralsRes resId: Int,
    count: Int
): UserMessage = UserMessage.PluralsResId(resId, count, args = null)

fun PluralsUserMessage(
    @PluralsRes resId: Int,
    count: Int,
    args: Any,
): UserMessage = UserMessage.PluralsResId(resId, count, arrayOf(args))

fun PluralsUserMessage(
    @PluralsRes resId: Int,
    count: Int,
    firstArg: Any,
    secondArg: Any,
    vararg etcArgs: Any,
): UserMessage = UserMessage.PluralsResId(resId, count, mergeArgs(firstArg, secondArg, etcArgs))

private fun mergeArgs(
    firstArg: Any,
    secondArg: Any,
    vararg etcArgs: Any
): Array<Any> = Array(size = etcArgs.size + 2) { index ->
    when (index) {
        0 -> firstArg
        1 -> secondArg
        else -> etcArgs[index - 2]
    }
}