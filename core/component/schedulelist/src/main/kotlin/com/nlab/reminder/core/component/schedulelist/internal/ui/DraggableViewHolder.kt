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

package com.nlab.reminder.core.component.schedulelist.internal.ui

import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

/**
 * @author Thalys
 */
sealed interface DraggableViewHolder : MovableViewHolder {
    val draggingDelegate: DraggingDelegate
}

abstract class DraggingDelegate {
    internal abstract fun userDraggable(): Boolean
    internal abstract fun isScaleOnDraggingNeeded(): Boolean
    internal abstract fun onDragStateChanged(isActive: Boolean)
    internal abstract fun mirrorView(parent: ViewGroup, viewPool: DraggingMirrorViewPool): View
}

internal class DraggingMirrorViewPool {
    private val cache = hashMapOf<KClass<out DraggableViewHolder>, View>()

    fun get(key: KClass<out DraggableViewHolder>): View? {
        return cache[key]
    }

    fun put(key: KClass<out DraggableViewHolder>, view: View) {
        cache[key] = view
    }

    fun clear() {
        cache.clear()
    }
}