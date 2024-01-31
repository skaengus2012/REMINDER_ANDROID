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

package com.nlab.reminder.core.domain

import dagger.Reusable
import javax.inject.Inject

/**
 * @author Doohyun
 */
@Reusable
class CalculateItemSwapResultUseCase @Inject constructor() {
    /**
     * Returns only the part of the swap of items from [fromPosition] and [toPosition] in [items].
     *
     * ex)
     * items = [0, 1, 2, 3, 4, 5]
     * fromPosition = 1
     * toPosition = 4
     * result = [2, 3, 4, 1]
     * [0], [5] are not included in the result, because they are not swapped.
     *
     * @param items         The list of items to be swapped.
     * @param fromPosition  The position of the item to be swapped.
     * @param toPosition    The position of the item to be swapped.
     */
    operator fun <T> invoke(items: List<T>, fromPosition: Int, toPosition: Int): List<T> {
        if (fromPosition == toPosition) return emptyList()
        if (fromPosition !in items.indices) return emptyList()
        if (toPosition !in items.indices) return emptyList()

        val minPosition = minOf(fromPosition, toPosition)
        val maxPosition = maxOf(fromPosition, toPosition)
        val size = maxPosition - minPosition + 1
        val ret = ArrayList<T>(size)
        if (fromPosition < toPosition) {
            // When using for-loop, a missing branch occurs in Jacoco.
            var i = fromPosition; while (i < toPosition) ret += items[++i]
            ret += items[fromPosition]
        } else {
            ret += items[fromPosition]
            for (i in toPosition until fromPosition) ret += items[i]
        }
        return ret
    }
}