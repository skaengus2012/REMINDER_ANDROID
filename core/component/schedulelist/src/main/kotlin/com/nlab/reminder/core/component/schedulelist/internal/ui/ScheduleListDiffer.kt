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

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback

/**
 * @author Thalys
 */
class ScheduleListDiffer(listUpdateCallback: ListUpdateCallback) {
    private val listUpdateCallbackProxy = ListUpdateCallbackProxy(listUpdateCallback)
    private val differ = AsyncListDiffer(
        listUpdateCallbackProxy,
        AsyncDifferConfig.Builder(ScheduleAdapterItemDiffCallback()).build()
    )
    private var state: ScheduleListDifferState = ScheduleListDifferState.Default()

    fun getCurrentList(): List<ScheduleAdapterItem> = when (val current = state) {
        is ScheduleListDifferState.Default -> differ.currentList
        is ScheduleListDifferState.Move -> current.movingList
        is ScheduleListDifferState.Sync -> current.movingList
    }

    fun tryMove(fromPosition: Int, toPosition: Int): Boolean {
        val movingList: MutableList<ScheduleAdapterItem>? = when (val current = state) {
            is ScheduleListDifferState.Default -> {
                if (current.hasSubmitting()) null
                else ScheduleListDifferState.Move(origin = differ.currentList)
                    .also { state = it }
                    .movingList
            }

            is ScheduleListDifferState.Move -> {
                current.movingList
            }

            is ScheduleListDifferState.Sync -> {
                null
            }
        }
        if (movingList == null) return false

        movingList[fromPosition] = movingList.set(toPosition, movingList[fromPosition])
        listUpdateCallbackProxy.onMoved(fromPosition, toPosition)
        return true
    }

    fun syncMoving(commitCallback: () -> Unit) {
        val sync = (state as? ScheduleListDifferState.Move)
            ?.let { ScheduleListDifferState.Sync(movingList = it.movingList, pendingSubmit = it.pendingSubmit) }
            ?.also { state = it }
            ?: run { commitCallback(); return }
        listUpdateCallbackProxy.canUpdate = false
        differ.submitList(sync.movingList) {
            listUpdateCallbackProxy.canUpdate = true
            state = ScheduleListDifferState.Default()
            val pendingSubmit = sync.pendingSubmit
            if (pendingSubmit == null) {
                commitCallback()
            } else {
                submitList(items = pendingSubmit.items, commitCallback = { commitCallback() })
            }
        }
    }

    fun submitList(items: List<ScheduleAdapterItem>?, commitCallback: (isImmediatelySubmitted: Boolean) -> Unit) {
        val default = when (val current = state) {
            is ScheduleListDifferState.Default -> {
                current
            }

            is ScheduleListDifferState.Move -> {
                current.pendingSubmit = PendingSubmit(items)
                commitCallback(false)
                return
            }

            is ScheduleListDifferState.Sync -> {
                current.pendingSubmit = PendingSubmit(items)
                commitCallback(false)
                return
            }
        }
        default.notifySubmitting()
        differ.submitList(items) {
            default.notifySubmitDone()
            commitCallback(true)
        }
    }
}

private class ListUpdateCallbackProxy(
    private val originUpdateCallback: ListUpdateCallback
) : ListUpdateCallback {
    var canUpdate = true

    override fun onInserted(position: Int, count: Int) {
        if (canUpdate) {
            originUpdateCallback.onInserted(position, count)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        if (canUpdate) {
            originUpdateCallback.onRemoved(position, count)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        if (canUpdate) {
            originUpdateCallback.onMoved(fromPosition, toPosition)
        }
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        if (canUpdate) {
            originUpdateCallback.onChanged(position, count, payload)
        }
    }
}

private sealed class ScheduleListDifferState private constructor() {
    class Default : ScheduleListDifferState() {
        private var submittingCount: Int = 0

        fun hasSubmitting(): Boolean = submittingCount > 0

        fun notifySubmitting() {
            ++submittingCount
        }

        fun notifySubmitDone() {
            if (submittingCount == 0) return
            --submittingCount
        }
    }

    class Move(
        origin: List<ScheduleAdapterItem>
    ) : ScheduleListDifferState() {
        var pendingSubmit: PendingSubmit? = null
        var movingList = mutableListOf<ScheduleAdapterItem>().apply { addAll(origin) }
    }

    class Sync(
        val movingList: List<ScheduleAdapterItem>,
        var pendingSubmit: PendingSubmit?
    ) : ScheduleListDifferState()
}

@JvmInline
private value class PendingSubmit(val items: List<ScheduleAdapterItem>?)